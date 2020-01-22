package org.jepria.oauth.authentication.rest;

import org.jepria.oauth.authentication.AuthenticationServerFactory;
import org.jepria.oauth.authentication.AuthenticationService;
import org.jepria.oauth.main.dto.ErrorDto;
import org.jepria.server.service.rest.JaxrsAdapterBase;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

/**
 * Authentication Endpoint takes care of authentication business logic part for Authorization Code Flow and Implicit Flow.
 */
public class AuthenticationJaxrsAdapter extends JaxrsAdapterBase {
  @Context
  HttpServletRequest request;

  private String getPrivateKey() {
    return request.getServletContext().getInitParameter("org.jepria.auth.jwt.PrivateKey");
  }

  private String getHostContext() {
    return URI.create(request.getRequestURL().toString()).resolve(request.getContextPath()).toString();
  }

  @POST
  @Path("/authenticate")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response authenticate(
    @QueryParam("response_type") String responseType,
    @QueryParam("code") String authCode,
    @QueryParam("redirect_uri") String redirectUri,
    @QueryParam("client_id") String clientId,
    @QueryParam("client_name") String clientName,
    @QueryParam("state") String state,
    @FormParam("username") String username,
    @FormParam("password") String password) {
    return AuthenticationServerFactory.getInstance().getService().authenticate(getPrivateKey(), getHostContext(), responseType, authCode, state, redirectUri, clientId, clientName, username, password);
  }

}
