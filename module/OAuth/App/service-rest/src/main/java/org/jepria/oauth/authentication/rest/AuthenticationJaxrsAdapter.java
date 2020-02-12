package org.jepria.oauth.authentication.rest;

import org.jepria.oauth.authentication.AuthenticationServerFactory;
import org.jepria.oauth.authentication.AuthenticationService;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.server.service.rest.JaxrsAdapterBase;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Base64;

import static org.jepria.oauth.sdk.OAuthConstants.*;

/**
 * Authentication Endpoint takes care of authentication business logic part for Authorization Code Flow and Implicit Flow.
 */
public class AuthenticationJaxrsAdapter extends JaxrsAdapterBase {
  @Context
  HttpServletRequest request;
  AuthenticationService service = AuthenticationServerFactory.getInstance().getService();

  private String getPublicKey() {
    return request.getServletContext().getInitParameter("org.jepria.auth.jwt.PublicKey");
  }

  private String getPrivateKey() {
    return request.getServletContext().getInitParameter("org.jepria.auth.jwt.PrivateKey");
  }

  private String getHostContext() {
    return URI.create(request.getRequestURL().toString()).resolve(request.getContextPath()).toString();
  }

  @POST
  @Path("/authenticate")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response authenticate(
    @QueryParam("response_type") String responseType,
    @QueryParam("code") String authCode,
    @QueryParam("redirect_uri") String redirectUriEncoded,
    @QueryParam("client_id") String clientId,
    @QueryParam("client_name") String clientName,
    @QueryParam("state") String state,
    @FormParam("username") String username,
    @FormParam("password") String password) {
    String redirectUri = new String(Base64.getUrlDecoder().decode(redirectUriEncoded));
    Response response;
    if (CODE.equalsIgnoreCase(responseType)) {
      String sessionToken = service
        .authenticate(authCode,
          redirectUri,
          clientId,
          username,
          password,
          getHostContext(),
          getPublicKey(),
          getPrivateKey());
      response = Response.
        status(302)
        .location(URI.create(redirectUri + getSeparator(redirectUri) + CODE + "=" + authCode + "&" + (state != null ? STATE + "=" + state : "")))
        .cookie(new NewCookie(SESSION_ID,
          sessionToken,
          null,
          null,
          null,
          NewCookie.DEFAULT_MAX_AGE,
          false,
          true))
        .build();
    } else if (TOKEN.equalsIgnoreCase(responseType)) {
      String sessionToken = service
        .authenticate(authCode,
          redirectUri,
          clientId,
          username,
          password,
          getHostContext(),
          getPublicKey(),
          getPrivateKey());
      TokenDto tokenDto = service.getToken(getPrivateKey(), getHostContext(), authCode, clientId, redirectUri);
      response = Response.status(302).location(URI.create(redirectUri
        + "#" + ACCESS_TOKEN_QUERY_PARAM + tokenDto.getAccessToken()
        + "&" + TOKEN_TYPE_QUERY_PARAM + tokenDto.getTokenType()
        + "&" + EXPIRES_IN_QUERY_PARAM + tokenDto.getExpiresIn()
        + "&" + STATE + "=" + state))
        .cookie(new NewCookie(SESSION_ID,
          sessionToken,
          null,
          null,
          null,
          NewCookie.DEFAULT_MAX_AGE,
          false,
          true))
        .build();
    } else {
      response = Response.status(302).location(URI.create(redirectUri + getSeparator(redirectUri) + ERROR_QUERY_PARAM + UNSUPPORTED_RESPONSE_TYPE)).build();
    }
    return response;
  }

  /**
   * Get next separator for URI
   *
   * @param uri
   * @return
   */
  private static String getSeparator(String uri) {
    String separator = "";
    if (uri != null) {
      if (uri.contains("?")) {
        separator = "&";
      } else if (uri.endsWith("/")) {
        separator = "?";
      } else {
        separator = "/?";
      }
    }
    return separator;
  }

}
