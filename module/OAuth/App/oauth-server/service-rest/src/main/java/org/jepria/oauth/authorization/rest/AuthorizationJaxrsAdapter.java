package org.jepria.oauth.authorization.rest;

import org.jepria.oauth.authorization.AuthorizationServerFactory;
import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.main.rest.jersey.LoginAttemptLimitFilter;
import org.jepria.oauth.sdk.ResponseType;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.token.TokenServerFactory;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.server.env.EnvironmentPropertySupport;
import org.jepria.server.service.rest.JaxrsAdapterBase;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.*;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.jepria.oauth.main.OAuthConstants.*;
import static org.jepria.oauth.main.OAuthConstants.OAUTH_ACCESS_TOKEN_LIFE_TIME;
import static org.jepria.oauth.main.rest.jersey.LoginAttemptLimitFilter.CURRENT_ATTEMPT_COUNT;
import static org.jepria.oauth.sdk.OAuthConstants.*;

public class AuthorizationJaxrsAdapter extends JaxrsAdapterBase {
  @Inject
  AuthorizationServerFactory authorizationServerFactory;
  @Inject
  TokenServerFactory tokenServerFactory;
  @Context
  HttpServletRequest request;
  
  private String getHostContext() {
    return URI.create(request.getRequestURL().toString()).resolve(request.getContextPath()).toString();
  }
  
  private Long getAccessTokenLifeTime() {
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
    return Long.valueOf(tokenLifeTime);
  }
  
  @GET
  @Path("/authorize")
  @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.TEXT_HTML, "text/x-gwt-rpc"})
// TODO delete after getting off GWT (cause GWT send redirect query with 'content-type: text/x-gwt-rpc' in IE
  public Response authorize(@QueryParam("response_type") String responseType,
                            @QueryParam("client_id") String clientId,
                            @QueryParam("redirect_uri") String redirectUriEncoded,
                            @QueryParam("code_challenge") String codeChallenge,
                            @QueryParam("state") String state,
                            @CookieParam(SESSION_ID) String sessionToken,
                            @CookieParam(CURRENT_ATTEMPT_COUNT) String currentAttemptCount) {
    
    if (currentAttemptCount != null && currentAttemptCount.length() > 0) {
      if (Integer.valueOf(currentAttemptCount).compareTo(LoginAttemptLimitFilter.getMaxAttemptCount(request)) > 0) {
        throw new OAuthRuntimeException(ACCESS_DENIED, "Превышено количество неуспешных попыток входа, обратитесь в службу технической поддержки для восстановления доступа.");
      }
    }
    
    String redirectUri = null;
    if (redirectUriEncoded == null) {
      throw new OAuthRuntimeException(INVALID_REQUEST, "redirect_uri is null");
    }
    try {
      redirectUri = URLDecoder.decode(redirectUriEncoded.replaceAll("%20", "\\+"), StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    if (!isValidUri(redirectUri)) {
      throw new OAuthRuntimeException(INVALID_REQUEST, "redirect_uri is invalid");
    }
    Response response = null;
    SessionDto sessionDto;
    
    if (sessionToken != null) {
      sessionDto = authorizationServerFactory
        .getService()
        .authorize(responseType,
          clientId,
          redirectUri,
          codeChallenge,
          sessionToken,
          getHostContext());
    } else {
      sessionDto = authorizationServerFactory
        .getService()
        .authorize(responseType,
          clientId,
          redirectUri,
          codeChallenge);
    }
    if (sessionDto.getSessionTokenId() != null
      && new Date().before(sessionDto.getSessionTokenDateFinish())
      && sessionDto.getOperator() != null) {
      if (ResponseType.CODE.equals(responseType)) {
        try {
          response = Response
            .status(302)
            .location(URI.create(redirectUri +
              getSeparator(redirectUri) + CODE + "=" + sessionDto.getAuthorizationCode() +
              (state != null ? "&" + STATE + "=" + URLEncoder.encode(state, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20") : "")))
            .build();
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      } else {
        TokenDto tokenDto = tokenServerFactory
          .getService()
          .create(responseType,
            clientId,
            getHostContext(),
            sessionDto.getAuthorizationCode(),
            URI.create(redirectUri),
            getAccessTokenLifeTime());
        try {
          response = Response.status(302).location(URI.create(redirectUri
            + "#" + ACCESS_TOKEN_QUERY_PARAM + tokenDto.getAccessToken()
            + "&" + TOKEN_TYPE_QUERY_PARAM + tokenDto.getTokenType()
            + "&" + EXPIRES_IN_QUERY_PARAM + tokenDto.getExpiresIn()
            + "&" + STATE + "=" + URLEncoder.encode(state, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20")))
            .cookie(new NewCookie(SESSION_ID,
              sessionToken,
              null,
              null,
              null,
              NewCookie.DEFAULT_MAX_AGE,
              false,
              true))
            .build();
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      }
    } else {
      try {
        response = Response.status(302).location(URI.create("/oauth/login/?"
          + RESPONSE_TYPE + "=" + responseType
          + "&" + AUTH_ID + "=" + sessionDto.getSessionId()
          + "&" + REDIRECT_URI + "=" + redirectUriEncoded
          + "&" + CLIENT_ID + "=" + sessionDto.getClient().getValue()
          + "&" + CLIENT_NAME + "=" + URLEncoder.encode(sessionDto.getClient().getName(), StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20")
          + "&" + STATE + "=" + URLEncoder.encode(state, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20"))).build();
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
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
      } else {
        separator = "?";
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
