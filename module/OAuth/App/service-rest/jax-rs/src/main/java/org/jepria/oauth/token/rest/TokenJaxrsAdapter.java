package org.jepria.oauth.token.rest;

import org.jepria.oauth.authentication.AuthenticationServerFactory;
import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.sdk.GrantType;
import org.jepria.oauth.token.TokenServerFactory;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.oauth.token.dto.TokenInfoDto;
import org.jepria.server.env.EnvironmentPropertySupport;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.security.JepSecurityContext;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.jepria.oauth.main.OAuthConstants.*;
import static org.jepria.oauth.sdk.OAuthConstants.*;

/**
 * The token endpoint is used by the client to obtain an access token by
 * presenting its authorization grant or refresh token.  The token
 * endpoint is used with every authorization grant except for the
 * implicit grant type (since an access token is issued directly).
 */
@Path("/token")
public class TokenJaxrsAdapter extends JaxrsAdapterBase {

  @Context
  HttpServletRequest request;
  @Context
  JepSecurityContext securityContext;
  @Inject
  AuthenticationServerFactory authenticationServerFactory;
  @Inject
  TokenServerFactory tokenServerFactory;

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

  private Integer getRefreshTokenLifeTime() {
    HttpSession session = request.getSession(false);
    String tokenLifeTime = null;
    if (session != null) {
      tokenLifeTime = (String) session.getAttribute(OAUTH_REFRESH_TOKEN_LIFE_TIME);
    }
    if (tokenLifeTime == null) {
      tokenLifeTime = EnvironmentPropertySupport.getInstance(request).getProperty(OAUTH_REFRESH_TOKEN_LIFE_TIME, OAUTH_REFRESH_TOKEN_LIFE_TIME_DEFAULT);
      if (session != null) {
        session.setAttribute(OAUTH_REFRESH_TOKEN_LIFE_TIME, tokenLifeTime);
      }
    }
    return Integer.valueOf(tokenLifeTime);
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response createToken(
      @HeaderParam("Authorization") String authHeader,
      @FormParam("grant_type") String grantType,
      @FormParam("client_id") String clientId,
      @FormParam("client_secret") String clientSecret,
      @FormParam("redirect_uri") String redirectUriEncoded,
      @FormParam("code") String authCode,
      @FormParam("username") String username,
      @FormParam("password") String password,
      @FormParam("password_hash") String passwordHash,
      @FormParam("code_verifier") String codeVerifier,
      @FormParam("refresh_token") String refreshToken) {
    if (authHeader != null) {
      authHeader = authHeader.replaceFirst("[Bb]asic ", "");
      String[] clientCredentials = new String(Base64.getUrlDecoder().decode(authHeader)).split(":");
      clientId = clientCredentials[0];
      clientSecret = clientCredentials[1];
    }
    if (grantType == null) {
      throw new OAuthRuntimeException(INVALID_REQUEST, "Grant type must be not null");
    }
    TokenDto result;
    switch (grantType) {
      case GrantType.AUTHORIZATION_CODE: {
        if (clientId != null && clientSecret != null) {
          authenticationServerFactory.getService().loginByClientSecret(clientId, clientSecret);
        } else if (clientId != null && clientSecret == null && codeVerifier != null) {
          authenticationServerFactory.getService().loginByAuthorizationCode(authCode, clientId, codeVerifier);
        } else {
          throw new OAuthRuntimeException(ACCESS_DENIED, "Client authorization failed");
        }

        String redirectUriDecoded = null;
        try {
          redirectUriDecoded = URLDecoder.decode(redirectUriEncoded.replaceAll("%20", "\\+"), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }

        URI redirectUri = URI.create(redirectUriDecoded);
        result = tokenServerFactory.getService().create(clientId, authCode, getHostContext(), redirectUri, getAccessTokenLifeTime());
        break;
      }
      case GrantType.CLIENT_CREDENTIALS: {
        Integer userId;
        if (clientId != null && clientSecret != null) {
          userId = authenticationServerFactory.getService().loginByClientSecret(clientId, clientSecret);
        } else {
          throw new OAuthRuntimeException(ACCESS_DENIED, "Client authorization failed");
        }
        result = tokenServerFactory.getService().create(clientId, userId, getHostContext(), getAccessTokenLifeTime(), getRefreshTokenLifeTime());
        break;
      }
      case GrantType.PASSWORD: {
        if (clientId != null && clientSecret != null) {
          authenticationServerFactory.getService().loginByClientSecret(clientId, clientSecret);
        } else {
          throw new OAuthRuntimeException(ACCESS_DENIED, "Client authorization failed");
        }
        Integer userId = null;
        if (password != null) {
          userId = authenticationServerFactory.getService().loginByPassword(username, password);
        } else if (passwordHash != null) {
          userId = authenticationServerFactory.getService().loginByPasswordHash(username, passwordHash);
        }
        result = tokenServerFactory.getService().create(clientId, username, userId, getHostContext(), getAccessTokenLifeTime(), getRefreshTokenLifeTime());
        break;
      }
      case GrantType.REFRESH_TOKEN: {
        if (clientId != null && clientSecret != null) {
          authenticationServerFactory.getService().loginByClientSecret(clientId, clientSecret);
        } else {
          authenticationServerFactory.getService().loginByClientId(clientId);
        }
        result = tokenServerFactory.getService().create(clientId, refreshToken, getHostContext(), getAccessTokenLifeTime(), getRefreshTokenLifeTime());
        break;
      }
      default: {
        throw new OAuthRuntimeException(UNSUPPORTED_GRANT_TYPE, "Grant type '" + grantType + "' is not supported");
      }
    }
    return Response.ok(result).build();
  }

  @POST
  @Path("/introspect")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response getTokenInfo(
      @HeaderParam("Authorization") String authHeader,
      @FormParam("client_id") String clientId,
      @FormParam("client_secret") String clientSecret,
      @FormParam("token") String token) {
    if (authHeader != null) {
      authHeader = authHeader.replaceFirst("[Bb]asic ", "");
      String[] clientCredentials = new String(Base64.getUrlDecoder().decode(authHeader)).split(":");
      clientId = clientCredentials[0];
      clientSecret = clientCredentials[1];
    }
    authenticationServerFactory.getService().loginByClientSecret(clientId, clientSecret);
    TokenInfoDto result = tokenServerFactory.getService().getTokenInfo(getHostContext(), token);
    return Response.ok(result).build();
  }

  @POST
  @Path("/revoke")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response deleteToken(
      @HeaderParam("Authorization") String authHeader,
      @FormParam("client_id") String clientId,
      @FormParam("client_secret") String clientSecret,
      @FormParam("token") String token) {
    if (authHeader != null) {
      authHeader = authHeader.replaceFirst("[Bb]asic ", "");
      String[] clientCredentials = new String(Base64.getUrlDecoder().decode(authHeader)).split(":");
      clientId = clientCredentials[0];
      clientSecret = clientCredentials[1];
    }
    authenticationServerFactory.getService().loginByClientSecret(clientId, clientSecret);

    tokenServerFactory.getService().delete(
        clientId,
        token);
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    return Response.ok().build();
  }

}
