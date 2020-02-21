package org.jepria.oauth.main.security;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ContainerException;
import org.jepria.oauth.authentication.AuthenticationServerFactory;
import org.jepria.oauth.model.authentication.AuthenticationService;
import org.jepria.oauth.sdk.util.URIUtil;
import org.jepria.server.service.security.PrincipalImpl;

import javax.annotation.Priority;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Base64;
import java.util.Map;

import static org.jepria.oauth.sdk.OAuthConstants.*;

/**
 * Client credentials based authentication filter
 */
@WithClientCredentials
@Priority(Priorities.AUTHENTICATION)
public final class ClientCredentialsRequestFilter implements ContainerRequestFilter {

  @Context
  HttpServletRequest request;
  AuthenticationService authenticationService = AuthenticationServerFactory.getInstance().getService();

  private void login(ContainerRequestContext requestContext) throws LoginException {
    String authString = requestContext.getHeaderString("authorization");
    if (authString != null) {
      authString = authString.replaceFirst("[Bb]asic ", "");
      String[] credentials = new String(Base64.getUrlDecoder().decode(authString)).split(":");
      Integer clientIdentifier = authenticationService.loginByClientSecret(credentials[0], credentials[1]);
      requestContext.setSecurityContext(new ClientSecurityContext(clientIdentifier, credentials[0]));
    } else {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try (InputStream in = requestContext.getEntityStream()) {
        ReaderWriter.writeTo(in, out);
        byte[] requestEntity = out.toByteArray();
        Map<String, String> parameters = URIUtil.parseParameters(new String(requestEntity), null);
        requestContext.setEntityStream(new ByteArrayInputStream(requestEntity));
        if (parameters.get(CLIENT_ID) != null && parameters.get(CLIENT_SECRET) != null) {
          Integer clientIdentifier = authenticationService.loginByClientSecret(parameters.get(CLIENT_ID), parameters.get(CLIENT_SECRET));
          requestContext.setSecurityContext(new ClientSecurityContext(clientIdentifier, parameters.get(CLIENT_ID)));
        } else {
          throw new LoginException("Invalid credentials");
        }
      } catch (IOException ex) {
        throw new ContainerException(ex);
      }
    }
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    try {
      login(requestContext);
    } catch (LoginException e) {
      throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
    }
  }

  final class ClientSecurityContext implements javax.ws.rs.core.SecurityContext {

    private final Integer clientId;
    private final String clientCode;

    public ClientSecurityContext(Integer clientId, String clientCode) {
      this.clientId = clientId;
      this.clientCode = clientCode;
    }

    @Override
    public boolean isUserInRole(final String roleName) {
      return true;
    }

    @Override
    public Principal getUserPrincipal() {
      return new PrincipalImpl(clientCode, clientId);
    }

    @Override
    public String getAuthenticationScheme() {
      return BASIC_AUTH;
    }

    @Override
    public boolean isSecure() {
      return false;
    }
  }
}
