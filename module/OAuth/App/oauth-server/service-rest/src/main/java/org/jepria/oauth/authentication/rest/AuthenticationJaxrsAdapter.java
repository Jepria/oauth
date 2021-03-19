package org.jepria.oauth.authentication.rest;

import org.jepria.oauth.authentication.AuthenticationServerFactory;
import org.jepria.oauth.authentication.AuthenticationService;
import org.jepria.oauth.authentication.dto.SessionTokenDto;
import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.main.rest.jersey.LoginAttempt;
import org.jepria.oauth.main.rest.jersey.LoginAttemptLimitFilter;
import org.jepria.oauth.token.TokenServerFactory;
import org.jepria.oauth.token.TokenService;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.server.service.rest.JaxrsAdapterBase;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.*;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.jepria.oauth.main.OAuthConstants.ERROR_QUERY_PARAM;
import static org.jepria.oauth.main.OAuthConstants.SID;
import static org.jepria.oauth.main.Utils.*;
import static org.jepria.oauth.main.rest.jersey.LoginAttemptLimitFilter.CURRENT_ATTEMPT_COUNT;
import static org.jepria.oauth.sdk.OAuthConstants.*;

/**
 * Authentication Endpoint takes care of authentication business logic part for Authorization Code Flow and Implicit Flow.
 */
public class AuthenticationJaxrsAdapter extends JaxrsAdapterBase {
  
  private final AuthenticationService authenticationService;
  private final TokenService tokenService;
  @Context
  ContainerRequestContext containerRequestContext;
  
  @Inject
  public AuthenticationJaxrsAdapter(AuthenticationServerFactory authenticationServerFactory, TokenServerFactory tokenServerFactory) {
    this.authenticationService = authenticationServerFactory.getService();
    this.tokenService = tokenServerFactory.getService();
  }
  
  @POST
  @Path("/authenticate")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @LoginAttempt
  public Response authenticate(
    @QueryParam("response_type") String responseType,
    @QueryParam("session_id") String sessionId,
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
      /*
       * authenticate user and create encrypted session token
       */
      sessionToken = authenticationService
        .authenticate(sessionId,
          redirectUri,
          clientId,
          username,
          password,
          getHostContextPath(request),
          getSessionTokenLifeTime(request));
    } catch (OAuthRuntimeException ex) {
      if (ex.getExceptionCode().equals(ACCESS_DENIED)) {
        return Response.status(302).location(UriBuilder.fromUri("/oauth/login")
          .queryParam(RESPONSE_TYPE, responseType)
          .queryParam(SID, sessionId)
          .queryParam(REDIRECT_URI, redirectUriEncoded)
          .queryParam(CLIENT_ID, clientId)
          .queryParam(CLIENT_NAME, URLEncoder.encode(clientName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20"))
          .queryParam(STATE, URLEncoder.encode(state, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20"))
          .queryParam(ERROR_QUERY_PARAM, ACCESS_DENIED).build()).build();
      } else {
        throw ex;
      }
    }
    containerRequestContext.setProperty(CURRENT_ATTEMPT_COUNT, 0);
    Response response;
    if (CODE.equalsIgnoreCase(responseType)) {
      /*
       * redirect to client with authorization code
       */
      response = Response.status(302)
        .location(UriBuilder.fromUri(redirectUri)
          .queryParam(CODE, sessionToken.getAuthorizationCode())
          .queryParam(STATE, URLEncoder.encode(state, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20")).build())
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
      /*
       * redirect to client with token in hash fragment
       */
      TokenDto tokenDto = tokenService.create(
        getHostContextPath(request),
        sessionToken.getAuthorizationCode(),
        clientId,
        URI.create(redirectUri),
        getAccessTokenLifeTime(request),
        getRefreshTokenLifeTime(request));
      response = Response.status(302)
        .location(UriBuilder.fromUri(redirectUri).fragment(ACCESS_TOKEN_QUERY_PARAM + tokenDto.getAccessToken()
          + "&" + TOKEN_TYPE_QUERY_PARAM + tokenDto.getTokenType()
          + "&" + EXPIRES_IN_QUERY_PARAM + tokenDto.getExpiresIn()
          + "&" + STATE + "=" + URLEncoder.encode(state, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20")
        ).build())
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
      response =
        Response.status(302).location(UriBuilder.fromUri(redirectUri).queryParam(ERROR_QUERY_PARAM,
          UNSUPPORTED_RESPONSE_TYPE).build()).build();
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
  ) throws UnsupportedEncodingException {
    String redirectUri = null;
    redirectUri = URLDecoder.decode(redirectUriEncoded.replaceAll("%20", "\\+"), StandardCharsets.UTF_8.name());
    if (sessionToken != null) {
      authenticationService.logout(clientId,
        redirectUri,
        sessionToken,
        getHostContextPath(request));
    }
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    return Response.status(302)
      .location(UriBuilder.fromUri(redirectUri).queryParam(STATE, URLEncoder.encode(state,
        StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20")).build())
      .cookie(new NewCookie(SESSION_ID, "", null, null, NewCookie.DEFAULT_VERSION, null, 0, new Date(), false, true))
      .build();
  }
  
}
