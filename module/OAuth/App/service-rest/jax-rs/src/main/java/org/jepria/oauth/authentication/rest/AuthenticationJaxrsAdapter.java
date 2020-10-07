package org.jepria.oauth.authentication.rest;

import org.jepria.oauth.authentication.AuthenticationServerFactory;
import org.jepria.oauth.authentication.dto.SessionTokenDto;
import org.jepria.oauth.token.TokenServerFactory;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.server.service.rest.JaxrsAdapterBase;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static org.jepria.oauth.sdk.OAuthConstants.*;

/**
 * Authentication Endpoint takes care of authentication business logic part for Authorization Code Flow and Implicit Flow.
 */
public class AuthenticationJaxrsAdapter extends JaxrsAdapterBase {
  @Inject
  AuthenticationServerFactory authenticationServerFactory;
  @Inject
  TokenServerFactory tokenServerFactory;
  @Context
  HttpServletRequest request;

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
    String redirectUri = null;

    try {
      redirectUri = URLDecoder.decode(redirectUriEncoded.replaceAll("%20", "\\+"), StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    SessionTokenDto sessionToken = authenticationServerFactory.getService()
        .authenticate(authCode,
            redirectUri,
            clientId,
            username,
            password,
            getHostContext());
    Response response;
    if (CODE.equalsIgnoreCase(responseType)) {
      response = Response.status(302)
          .location(URI.create(redirectUri + getSeparator(redirectUri) + CODE + "=" + authCode + "&" + (state != null ? STATE + "=" + state : "")))
          .cookie(new NewCookie(SESSION_ID,
              sessionToken.getToken(),
              null,
              null,
              1,
              null,
              -1,
              sessionToken.getExpirationDate(),
              false,
              true))
          .build();
    } else if (TOKEN.equalsIgnoreCase(responseType)) {
      TokenDto tokenDto = tokenServerFactory.getService().create(responseType, getHostContext(), authCode, clientId, URI.create(redirectUri));
      response = Response.status(302).location(URI.create(redirectUri
          + "#" + ACCESS_TOKEN_QUERY_PARAM + tokenDto.getAccessToken()
          + "&" + TOKEN_TYPE_QUERY_PARAM + tokenDto.getTokenType()
          + "&" + EXPIRES_IN_QUERY_PARAM + tokenDto.getExpiresIn()
          + "&" + STATE + "=" + state))
          .cookie(new NewCookie(SESSION_ID,
              sessionToken.getToken(),
              null,
              null,
              1,
              null,
              -1,
              sessionToken.getExpirationDate(),
              false,
              true))
          .build();
    } else {
      response = Response.status(302).location(URI.create(redirectUri + getSeparator(redirectUri) + ERROR_QUERY_PARAM + UNSUPPORTED_RESPONSE_TYPE)).build();
    }
    return response;
  }

  @GET
  @Path("/logout")
  public Response logout(
      @QueryParam("client_id") String clientId,
      @QueryParam("redirect_uri") String redirectUriEncoded,
      @QueryParam("state") String state,
      @CookieParam(SESSION_ID) String sessionToken
  ) {
    String redirectUri = null;
    try {
      redirectUri = URLDecoder.decode(redirectUriEncoded.replaceAll("%20", "\\+"), StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    if (sessionToken != null) {
      authenticationServerFactory
          .getService()
          .logout(clientId,
              redirectUri,
              sessionToken,
              getHostContext());
    }
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    return Response.status(302)
        .location(URI.create(redirectUri + getSeparator(redirectUri) + STATE + "=" + state))
        .cookie(new NewCookie(SESSION_ID, "", null, null, NewCookie.DEFAULT_VERSION, null, 0, new Date(), false, true))
        .build();
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
