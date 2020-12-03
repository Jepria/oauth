package org.jepria.oauth.authentication.rest;

import org.jepria.oauth.authentication.AuthenticationServerFactory;
import org.jepria.oauth.authentication.dto.SessionTokenDto;
import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.main.rest.jersey.LoginAttempt;
import org.jepria.oauth.main.rest.jersey.LoginAttemptLimitFilter;
import org.jepria.oauth.token.TokenServerFactory;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.server.env.EnvironmentPropertySupport;
import org.jepria.server.service.rest.JaxrsAdapterBase;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static org.jepria.oauth.main.OAuthConstants.*;
import static org.jepria.oauth.main.rest.jersey.LoginAttemptLimitFilter.CURRENT_ATTEMPT_COUNT;
import static org.jepria.oauth.main.rest.jersey.LoginAttemptLimitFilter.DEFAULT_MAX_ATTEMPT_COUNT;
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
  @Context
  ContainerRequestContext containerRequestContext;

  private String getHostContext() {
    return URI.create(request.getRequestURL().toString()).resolve(request.getContextPath()).toString();
  }

  private Integer getAccessTokenLifeTime() {
    HttpSession session = request.getSession(false);
    String tokenLifeTime = null;
    if (session != null) {
      tokenLifeTime = (String) session.getAttribute(OAUTH_ACCESS_TOKEN_LIFE_TIME);
    }
    if (tokenLifeTime == null) {
      tokenLifeTime = EnvironmentPropertySupport.getInstance(request).getProperty(OAUTH_ACCESS_TOKEN_LIFE_TIME, OAUTH_ACCESS_TOKEN_LIFE_TIME_DEFAULT);
      if (session != null) {
        session.setAttribute(OAUTH_ACCESS_TOKEN_LIFE_TIME, tokenLifeTime);
      }
    }
    return Integer.valueOf(tokenLifeTime);
  }

  private Integer getSessionTokenLifeTime() {
    HttpSession session = request.getSession(false);
    String tokenLifeTime = null;
    if (session != null) {
      tokenLifeTime = (String) session.getAttribute(OAUTH_SSO_TOKEN_LIFE_TIME);
    }
    if (tokenLifeTime == null) {
      tokenLifeTime = EnvironmentPropertySupport.getInstance(request).getProperty(OAUTH_SSO_TOKEN_LIFE_TIME, OAUTH_SSO_TOKEN_LIFE_TIME_DEFAULT);
      if (session != null) {
        session.setAttribute(OAUTH_SSO_TOKEN_LIFE_TIME, tokenLifeTime);
      }
    }
    return Integer.valueOf(tokenLifeTime);
  }

  @POST
  @Path("/authenticate")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @LoginAttempt
  public Response authenticate(
      @QueryParam("response_type") String responseType,
      @QueryParam("authId") String authId,
      @QueryParam("redirect_uri") String redirectUriEncoded,
      @QueryParam("client_id") String clientId,
      @QueryParam("client_name") String clientName,
      @QueryParam("state") String state,
      @FormParam("username") String username,
      @FormParam("password") String password) throws UnsupportedEncodingException {
    String redirectUri = null;

    try {
      redirectUri = URLDecoder.decode(redirectUriEncoded.replaceAll("%20", "\\+"), StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    if (((Integer) containerRequestContext.getProperty(CURRENT_ATTEMPT_COUNT)).compareTo(LoginAttemptLimitFilter.getMaxAttemptCount(request)) > 0) {
      throw new OAuthRuntimeException(ACCESS_DENIED, "Превышено количество неуспешных попыток входа, обратитесь в службу технической поддержки для восстановления доступа.");
    }
    SessionTokenDto sessionToken;
    try {
      sessionToken = authenticationServerFactory.getService()
          .authenticate(authId,
              redirectUri,
              clientId,
              username,
              password,
              getHostContext(),
              getSessionTokenLifeTime());
    } catch (OAuthRuntimeException ex) {
      if (ex.getExceptionCode().equals(ACCESS_DENIED)) {
        return Response.status(302).location(URI.create("/oauth/login/?"
            + RESPONSE_TYPE + "=" + responseType
            + "&" + AUTH_ID + "=" + authId
            + "&" + REDIRECT_URI + "=" + redirectUriEncoded
            + "&" + CLIENT_ID + "=" + clientId
            + "&" + CLIENT_NAME + "=" + URLEncoder.encode(clientName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20")
            + "&" + STATE + "=" + state
            + "&" + ERROR_QUERY_PARAM + ACCESS_DENIED)).build();
      } else {
        throw ex;
      }
    }
    containerRequestContext.setProperty(CURRENT_ATTEMPT_COUNT, 0);
    Response response;
    if (CODE.equalsIgnoreCase(responseType)) {
      response = Response.status(302)
          .location(URI.create(redirectUri +
              getSeparator(redirectUri) +
              CODE + "=" + sessionToken.getAuthorizationCode() +
              "&" + (state != null ? STATE + "=" + state : "")))
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
      TokenDto tokenDto = tokenServerFactory.getService().create(responseType,
          getHostContext(),
          sessionToken.getAuthorizationCode(),
          clientId,
          URI.create(redirectUri),
          getAccessTokenLifeTime());
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
