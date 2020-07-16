package org.jepria.oauth.token.rest;

import org.jepria.oauth.authentication.AuthenticationServerFactory;
import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.authentication.AuthenticationService;
import org.jepria.oauth.token.TokenService;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.oauth.token.dto.TokenInfoDto;
import org.jepria.oauth.sdk.GrantType;
import org.jepria.oauth.token.TokenServerFactory;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.security.JepSecurityContext;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Base64;

import static org.jepria.oauth.sdk.OAuthConstants.*;

/**
 * The token endpoint is used by the client to obtain an access token by
 *    presenting its authorization grant or refresh token.  The token
 *    endpoint is used with every authorization grant except for the
 *    implicit grant type (since an access token is issued directly).
 */
@Path("/token")
public class TokenJaxrsAdapter extends JaxrsAdapterBase {
  @Context
  HttpServletRequest request;
  @Context
  JepSecurityContext securityContext;
  AuthenticationService authenticationService = AuthenticationServerFactory.getInstance().getService();
  TokenService tokenService = TokenServerFactory.getInstance().getService();

  private String getHostContext() {
    return URI.create(request.getRequestURL().toString()).resolve(request.getContextPath()).toString();
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
    TokenDto result = null;
    switch (grantType) {
      case GrantType.AUTHORIZATION_CODE: {
        if (clientId != null && clientSecret != null) {
          authenticationService.loginByClientSecret(clientId, clientSecret);
        } else if (clientId != null && clientSecret == null && codeVerifier != null) {
          authenticationService.loginByAuthorizationCode(authCode, clientId, codeVerifier);
        } else {
          throw new OAuthRuntimeException(ACCESS_DENIED, "Request authorization failed");
        }
        URI redirectUri = URI.create(new String(Base64.getUrlDecoder().decode(redirectUriEncoded)));
        result = tokenService.create(clientId, authCode, getHostContext(), redirectUri);
        break;
      }
      case GrantType.CLIENT_CREDENTIALS: {
        Integer userId;
        if (clientId != null && clientSecret != null) {
          userId = authenticationService.loginByClientSecret(clientId, clientSecret);
        } else {
          throw new OAuthRuntimeException(ACCESS_DENIED, "Request authorization failed");
        }
        result = tokenService.create(clientId, userId, getHostContext());
        break;
      }
      case GrantType.PASSWORD: {
        if (clientId != null && clientSecret != null) {
          authenticationService.loginByClientSecret(clientId, clientSecret);
        } else if (clientId != null && clientSecret == null) {
          authenticationService.loginByClientId(clientId);
        }
        Integer userId = authenticationService.loginByPassword(username, password);
        result = tokenService.create(clientId, username, userId, getHostContext());
        break;
      }
      case GrantType.REFRESH_TOKEN: {
        if (clientId != null && clientSecret != null) {
          authenticationService.loginByClientSecret(clientId, clientSecret);
        } else if (clientId != null && clientSecret == null) {
          authenticationService.loginByClientId(clientId);
        }
        result = tokenService.create(clientId, refreshToken, getHostContext());
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
    authenticationService.loginByClientSecret(clientId, clientSecret);
    TokenInfoDto result = TokenServerFactory.getInstance().getService().getTokenInfo( getHostContext(), token);
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
    authenticationService.loginByClientSecret(clientId, clientSecret);
    TokenServerFactory.getInstance().getService().delete(
      clientId,
      token);
    return Response.ok().build();
  }

}
