package org.jepria.oauth.token.rest;

import org.jepria.oauth.authentication.AuthenticationServerFactory;
import org.jepria.oauth.authentication.AuthenticationService;
import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.main.security.ClientCredentials;
import org.jepria.oauth.sdk.GrantType;
import org.jepria.oauth.token.TokenServerFactory;
import org.jepria.oauth.token.TokenService;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.oauth.token.dto.TokenInfoDto;
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

import static org.jepria.oauth.main.Utils.*;
import static org.jepria.oauth.sdk.OAuthConstants.*;

/**
 * The token endpoint is used by the client to obtain an access token by
 * presenting its authorization grant or refresh token.  The token
 * endpoint is used with every authorization grant except for the
 * implicit grant type (since an access token is issued directly).
 */
@Path("/token")
public class TokenJaxrsAdapter extends JaxrsAdapterBase {
  
  private final AuthenticationService authenticationService;
  private final TokenService tokenService;
  
  @Inject
  public TokenJaxrsAdapter(AuthenticationServerFactory authenticationServerFactory, TokenServerFactory tokenServerFactory) {
    this.authenticationService = authenticationServerFactory.getService();
    this.tokenService = tokenServerFactory.getService();
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
          authenticationService.loginByClientSecret(clientId, clientSecret);
        } else if (clientId != null && clientSecret == null && codeVerifier != null) {
          authenticationService.loginByAuthorizationCode(authCode, clientId, codeVerifier);
        } else {
          throw new OAuthRuntimeException(ACCESS_DENIED, "Client authorization failed");
        }
        
        String redirectUriDecoded = null;
        try {
          redirectUriDecoded = URLDecoder.decode(redirectUriEncoded.replaceAll("%20", "\\+"),
            StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
        
        URI redirectUri = URI.create(redirectUriDecoded);
        result = tokenService.create(clientId, authCode, getHostContextPath(request), redirectUri,
          getAccessTokenLifeTime(request), getRefreshTokenLifeTime(request));
        break;
      }
      case GrantType.CLIENT_CREDENTIALS: {
        Integer userId;
        if (clientId != null && clientSecret != null) {
          userId = authenticationService.loginByClientSecret(clientId, clientSecret);
        } else {
          throw new OAuthRuntimeException(ACCESS_DENIED, "Client authorization failed");
        }
        result = tokenService.create(clientId, userId, getHostContextPath(request),
          getAccessTokenLifeTime(request), getRefreshTokenLifeTime(request));
        break;
      }
      case GrantType.PASSWORD: {
        if (clientId != null && clientSecret != null) {
          authenticationService.loginByClientSecret(clientId, clientSecret);
        } else {
          throw new OAuthRuntimeException(ACCESS_DENIED, "Client authorization failed");
        }
        Integer userId = null;
        if (password != null) {
          userId = authenticationService.loginByPassword(username, password);
        } else if (passwordHash != null) {
          userId = authenticationService.loginByPasswordHash(username, passwordHash);
        }
        result = tokenService.create(clientId,
          username, userId, getHostContextPath(request),
          getAccessTokenLifeTime(request), getRefreshTokenLifeTime(request));
        break;
      }
      case GrantType.REFRESH_TOKEN: {
        if (clientId != null && clientSecret != null) {
          authenticationService.loginByClientSecret(clientId, clientSecret);
        } else {
          authenticationService.loginByClientId(clientId);
        }
        result = tokenService.create(clientId,
          refreshToken, getHostContextPath(request),
          getAccessTokenLifeTime(request), getRefreshTokenLifeTime(request));
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
  @ClientCredentials
  public Response getTokenInfo(@FormParam("token") String token) {
    TokenInfoDto result = tokenService.getTokenInfo(getHostContextPath(request), token);
    return Response.ok(result).build();
  }
  
  @POST
  @Path("/revoke")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @ClientCredentials
  public Response deleteToken(
    @HeaderParam("Authorization") String authHeader,
    @FormParam("token") String token) {
    tokenService.delete(
      new String(Base64.getUrlDecoder().decode(authHeader.replaceFirst("[Bb]asic ", ""))).split(":")[0],
      token);
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    return Response.ok().build();
  }
  
}
