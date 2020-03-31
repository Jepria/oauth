package org.jepria.oauth.authorization.rest;

import org.jepria.oauth.authorization.AuthorizationServerFactory;
import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.model.session.dto.SessionDto;
import org.jepria.oauth.model.token.dto.TokenDto;
import org.jepria.oauth.sdk.ResponseType;
import org.jepria.oauth.token.TokenServerFactory;
import org.jepria.server.service.rest.ErrorDto;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.rest.jersey.ExceptionManager;
import org.jepria.server.service.rest.jersey.ExceptionManagerImpl;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Date;

import static org.jepria.oauth.sdk.OAuthConstants.*;

public class AuthorizationJaxrsAdapter extends JaxrsAdapterBase {
  @Context
  HttpServletRequest request;
  ExceptionManager exceptionManager = new ExceptionManagerImpl();

  private String getHostContext() {
    return URI.create(request.getRequestURL().toString()).resolve(request.getContextPath()).toString();
  }

  @GET
  @Path("/authorize")
  @Consumes({MediaType.TEXT_HTML, "text/x-gwt-rpc"})
// TODO delete after getting off GWT (cause GWT send redirect query with 'content-type: text/x-gwt-rpc' in IE
  public Response authorize(@QueryParam("response_type") String responseType,
                            @QueryParam("client_id") String clientId,
                            @QueryParam("redirect_uri") String redirectUriEncoded,
                            @QueryParam("code_challenge") String codeChallenge,
                            @QueryParam("state") String state,
                            @CookieParam(SESSION_ID) String sessionToken) {
    String redirectUri;
    try {
      redirectUri = new String(Base64.getUrlDecoder().decode(redirectUriEncoded));
      if (!isValidUri(redirectUri)) {
        ErrorDto errorDto = exceptionManager.registerExceptionAndPrepareErrorDto(new IllegalArgumentException("redirect_uri is invalid"));
        return Response.status(Response.Status.BAD_REQUEST).entity(errorDto).build();
      }
    } catch (Throwable th) {
      throw new OAuthRuntimeException(INVALID_REQUEST, "redirect_uri is null or invalid");
    }
    Response response;
    SessionDto session;

    if (sessionToken != null) {
      session = AuthorizationServerFactory
        .getInstance()
        .getService()
        .authorize(responseType,
          clientId,
          redirectUri,
          codeChallenge,
          sessionToken,
          getHostContext());
      if (!session.getBlocked() && session.getSessionTokenDateFinish().before(new Date()) && session.getOperator() != null) {
        if (ResponseType.CODE.equals(responseType)) {
          response = Response
              .status(302)
              .location(URI.create(redirectUri + getSeparator(redirectUri) + CODE + "=" + session.getAuthorizationCode() + "&" + (state != null ? STATE + "=" + state : "")))
              .build();
        } else {
          TokenDto tokenDto = TokenServerFactory.getInstance().getService().create(responseType, clientId, getHostContext(), session.getAuthorizationCode(), URI.create(redirectUri));
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
        }
      } else {
        response = Response.status(302).location(URI.create("/oauth/login/?"
          + RESPONSE_TYPE + "=" + responseType
          + "&" + CODE + "=" + session.getAuthorizationCode()
          + "&" + REDIRECT_URI + "=" + redirectUriEncoded
          + "&" + CLIENT_ID + "=" + session.getClient().getValue()
          + "&" + CLIENT_NAME + "=" + session.getClient().getName()
          + "&" + STATE + "=" + state)).build();
      }
    } else {
      session = AuthorizationServerFactory
        .getInstance()
        .getService()
        .authorize(responseType,
          clientId,
          redirectUri,
          codeChallenge);
      response = Response.status(302).location(URI.create("/oauth/login/?"
        + RESPONSE_TYPE + "=" + responseType
        + "&" + CODE + "=" + session.getAuthorizationCode()
        + "&" + REDIRECT_URI + "=" + redirectUriEncoded
        + "&" + CLIENT_ID + "=" + session.getClient().getValue()
        + "&" + CLIENT_NAME + "=" + session.getClient().getName()
        + "&" + STATE + "=" + state)).build();
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

  /**
   * @param redirectUri
   * @return
   */
  private boolean isValidUri(String redirectUri) {
    if (redirectUri == null) {
      return false;
    }
    try {
      new URI(redirectUri);
      return true;
    } catch (URISyntaxException | NullPointerException e) {
      e.printStackTrace();
      return false;
    }
  }
}
