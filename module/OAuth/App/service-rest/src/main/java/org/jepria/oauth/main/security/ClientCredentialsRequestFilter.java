package org.jepria.oauth.main.security;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ContainerException;
import org.jepria.oauth.authorization.AuthorizationServerFactory;
import org.jepria.oauth.authorization.dto.AuthRequestDto;
import org.jepria.oauth.authorization.dto.AuthRequestSearchDtoLocal;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.jepria.oauth.sdk.OAuthConstants.*;

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

  private AuthRequestDto getRequestDto(String clientId, String authCode) {
    AuthRequestSearchDtoLocal searchDto = new AuthRequestSearchDtoLocal();
    searchDto.setClientId(clientId);
    searchDto.setAuthorizationCode(authCode);
    List<AuthRequestDto> result = (List<AuthRequestDto>) AuthorizationServerFactory.getInstance().getDao().find(searchDto, 1);
    if (result.size() == 1) {
      AuthRequestDto client = result.get(0);
      return client;
    } else {
      return null;
    }
  }

  private boolean verifyCredentials(ContainerRequestContext requestContext) {
    String authString = requestContext.getHeaderString("authorization");
    boolean result = false;
    if (authString != null) {
      /*
      Client credentials in Authorization Header
       */
      authString = authString.replaceFirst("[Bb]asic ", "");
      String[] credentials = new String(Base64.getUrlDecoder().decode(authString)).split(":");
      ClientDto client = getClient(credentials[0]);
      if (client != null && HEADER.equals(client.getTokenAuthMethod().getValue())) {
        result = client.getClientSecret().equals(credentials[1]);
        if (result) {
          requestContext.setSecurityContext(new ClientSecurityContext(credentials[0]));
        }
      }
    } else {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try (InputStream in = requestContext.getEntityStream()) {
        ReaderWriter.writeTo(in, out);
        byte[] requestEntity = out.toByteArray();
        Map<String, String> parameters = URIUtil.parseParameters(new String(requestEntity), null);
        if (parameters.get(CLIENT_ID) != null && parameters.get(CLIENT_SECRET) != null) {
          /*
          Client credentials in Body
           */
          ClientDto client = getClient(parameters.get(CLIENT_ID));
          if (client != null) {
            if (BODY.equals(client.getTokenAuthMethod().getValue())) {
              result = client.getClientSecret().equals(parameters.get(CLIENT_SECRET));
            } else if (NONE.equals(client.getTokenAuthMethod().getValue())) {
              /*
              PKCE for public clients
               */
              AuthRequestDto authRequest = getRequestDto(parameters.get(CLIENT_ID), parameters.get(CODE));
              if (authRequest != null && authRequest.getCodeChallenge() == null && parameters.get("code_verifier") == null) {
                MessageDigest cryptoProvider = MessageDigest.getInstance("SHA-256");
                result = authRequest.getCodeChallenge().equals(Base64.getUrlEncoder().withoutPadding().encodeToString(cryptoProvider.digest(parameters.get("code_verifier").getBytes())));
              }
            }
          }
        }
        if (result) {
          requestContext.setSecurityContext(new ClientSecurityContext(parameters.get(CLIENT_ID)));
        }
        requestContext.setEntityStream(new ByteArrayInputStream(requestEntity));
      } catch (IOException | NoSuchAlgorithmException ex) {
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
