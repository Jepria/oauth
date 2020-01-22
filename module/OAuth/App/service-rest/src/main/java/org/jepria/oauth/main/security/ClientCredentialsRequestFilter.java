package org.jepria.oauth.main.security;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ContainerException;
import org.jepria.oauth.client.ClientServerFactory;
import org.jepria.oauth.client.dao.ClientDao;
import org.jepria.oauth.client.dto.ClientDto;
import org.jepria.oauth.client.dto.ClientSearchDto;
import org.jepria.oauth.sdk.util.URIUtil;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Client credentials based authentication filter
 */
@Priority(Priorities.AUTHENTICATION)
@WithClientCredentials
public final class ClientCredentialsRequestFilter implements ContainerRequestFilter {

  @Context
  HttpServletRequest request;

  private static final String NONE = "none";
  private static final String HEADER = "client_secret_basic";
  private static final String BODY = "client_secret_post";

  private ClientDto getClient(String clientId) {
    ClientSearchDto clientSearchTemplate = new ClientSearchDto();
    clientSearchTemplate.setClientId(clientId);
    List<ClientDto> result = (List<ClientDto>) ClientServerFactory.getInstance().getDao().find(clientSearchTemplate, 1);
    if (result.size() == 1) {
      ClientDto client = result.get(0);
      return client;
    } else {
      return null;
    }
  }

  private boolean verifyCredentials(ContainerRequestContext requestContext) {
    String authString = requestContext.getHeaderString("authorization");
    boolean result;
    if (authString != null) {
      authString = authString.replaceFirst("[Bb]asic ", "");
      String[] credentials = new String(Base64.getUrlDecoder().decode(authString)).split(":");
      ClientDto client = getClient(credentials[0]);
      if (client != null && HEADER.equals(client.getTokenAuthMethod().getValue())) {
        result = client.getClientSecret().equals(credentials[1]);
        if (result) {
          requestContext.setSecurityContext(new ClientSecurityContext(credentials[0]));
        }
      } else {
        result = false;
      }
    } else {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try (InputStream in = requestContext.getEntityStream()) {
        ReaderWriter.writeTo(in, out);
        byte[] requestEntity = out.toByteArray();
        Map<String, String> parameters = URIUtil.parseParameters(new String(requestEntity), null);
        ClientDto client = getClient(parameters.get("client_id"));
        if (client != null) {
          if (BODY.equals(client.getTokenAuthMethod().getValue())) {
            result = client.getClientSecret().equals(parameters.get("client_secret"));
          } else if (NONE.equals(client.getTokenAuthMethod().getValue())) {
            result = true;
          } else {
            result = false;
          }
        } else {
          result = false;
        }
        if (result) {
          requestContext.setSecurityContext(new ClientSecurityContext(parameters.get("client_id")));
        }
        requestContext.setEntityStream(new ByteArrayInputStream(requestEntity));
      } catch (IOException ex) {
        throw new ContainerException(ex);
      }
    }
    return result;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    if (!verifyCredentials(requestContext)) {
      throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
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
