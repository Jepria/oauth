package org.jepria.oauth.token.rest;

import org.jepria.oauth.main.security.AllowAllOrigin;
import org.jepria.oauth.main.security.WithClientCredentials;
import org.jepria.oauth.token.TokenServerFactory;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.oauth.token.dto.TokenInfoDto;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.security.JepSecurityContext;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

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
  @WithClientCredentials
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response createToken(
    @FormParam("grant_type") String grantType,
    @FormParam("client_id") String clientId,
    @FormParam("redirect_uri") String redirectUri,
    @FormParam("code") String authCode,
    @FormParam("username") String username,
    @FormParam("password") String password,
    @FormParam("code_verifier") String codeVerifier) {
    TokenDto result = TokenServerFactory.getInstance().getService().create(grantType,
      getPrivateKey(),
      getHostContext(),
      authCode,
      clientId,
      redirectUri,
      username,
      password);
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
