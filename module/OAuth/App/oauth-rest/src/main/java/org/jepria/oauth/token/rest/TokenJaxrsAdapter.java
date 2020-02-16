package org.jepria.oauth.token.rest;

import org.jepria.oauth.main.security.AllowAllOrigin;
import org.jepria.oauth.main.security.WithClientCredentials;
import org.jepria.oauth.model.token.dto.TokenDto;
import org.jepria.oauth.model.token.dto.TokenInfoDto;
import org.jepria.oauth.token.TokenServerFactory;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.security.JepSecurityContext;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Base64;

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

  //TODO Убрать после перехода на модуль Option???
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
  @AllowAllOrigin
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response createToken(
    @HeaderParam("Authorization") String authHeader,
    @FormParam("grant_type") String grantType,
    @FormParam("client_id") String clientId,
    @FormParam("client_secret") String clientSecret,
    @FormParam("redirect_uri") String redirectUri,
    @FormParam("code") String authCode,
    @FormParam("username") String username,
    @FormParam("password") String password,
    @FormParam("code_verifier") String codeVerifier,
    @FormParam("refresh_token") String refreshToken) {
    TokenDto result;
    if (authHeader != null) {
      authHeader = authHeader.replaceFirst("[Bb]asic ", "");
      String[] clientCredentials = new String(Base64.getUrlDecoder().decode(authHeader)).split(":");
      result = TokenServerFactory.getInstance().getService().create(grantType,
        getPublicKey(),
        getPrivateKey(),
        getHostContext(),
        authCode,
        clientCredentials[0],
        clientCredentials[1],
        codeVerifier,
        redirectUri,
        username,
        password,
        refreshToken);
    } else {
      result = TokenServerFactory.getInstance().getService().create(grantType,
        getPublicKey(),
        getPrivateKey(),
        getHostContext(),
        authCode,
        clientId,
        clientSecret,
        codeVerifier,
        redirectUri,
        username,
        password,
        refreshToken);
    }
    return Response.ok(result).build();
  }

  @POST
  @Path("/introspect")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @AllowAllOrigin
  @WithClientCredentials
  public TokenInfoDto getTokenInfo(@FormParam("token") String token) {
    return TokenServerFactory.getInstance().getService().getTokenInfo(getPublicKey(),
      getHostContext(),
      token,
      securityContext.getCredential());
  }

  @POST
  @Path("/revoke")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @AllowAllOrigin
  @WithClientCredentials
  public Response deleteToken(@FormParam("token") String token) {
    TokenServerFactory.getInstance().getService().deleteToken(
      securityContext.getUserPrincipal().getName(),
      token,
      securityContext.getCredential());
    return Response.ok().build();
  }

}
