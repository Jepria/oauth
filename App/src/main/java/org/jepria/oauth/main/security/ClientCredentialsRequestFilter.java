package org.jepria.oauth.main.security;

import org.jepria.oauth.client.ClientServerFactory;
import org.jepria.oauth.client.dao.ClientDao;
import org.jepria.oauth.client.dto.ClientDto;
import org.jepria.oauth.client.dto.ClientSearchDto;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.Principal;
import java.util.Base64;

/**
 * Client credentials based authentication filter
 */
@Priority(Priorities.AUTHENTICATION)
@WithClientCredentials
public final class ClientCredentialsRequestFilter implements ContainerRequestFilter {

  @Context
  HttpServletRequest request;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    String authString = requestContext.getHeaderString("authorization");
    if (authString == null) {
      throw new WebApplicationException(
        Response.status(Response.Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic").build());
    }
    authString = authString.replaceFirst("[Bb]asic ", "");
    String[] credentials = new String(Base64.getUrlDecoder().decode(authString)).split(":");
    try {
      ClientDao clientDao = ClientServerFactory.getInstance().getDao();
      ClientSearchDto clientSearchTemplate = new ClientSearchDto();
      clientSearchTemplate.setClientCode(credentials[0]);
      ClientDto client = (ClientDto) clientDao.find(clientSearchTemplate, 1).get(0);
      if (!client.getClientSecret().equals(credentials[1])) {
        throw new IllegalArgumentException();
      }
      requestContext.setSecurityContext(new ClientSecurityContext(credentials[0]));
    } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
      throw new WebApplicationException(
        Response.status(Response.Status.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic").build());
    }
  }

  final class ClientSecurityContext implements javax.ws.rs.core.SecurityContext {

    private final String clientCode;

    public ClientSecurityContext(String clientCode) {
      this.clientCode = clientCode;
    }
    @Override
    public boolean isUserInRole(final String roleName) {
      return true;
    }

    @Override
    public Principal getUserPrincipal() {
      return new Principal() {
        @Override
        public String getName() {
          return clientCode;
        }
      };
    }

    @Override
    public String getAuthenticationScheme() {
      return "BASIC";
    }

    @Override
    public boolean isSecure() {
      return false;
    }
  }
}
