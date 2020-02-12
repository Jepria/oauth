package org.jepria.oauth.main.security;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ContainerException;
import org.jepria.oauth.authentication.AuthenticationServerFactory;
import org.jepria.oauth.authentication.AuthenticationService;
import org.jepria.oauth.authorization.AuthorizationServerFactory;
import org.jepria.oauth.client.ClientServerFactory;
import org.jepria.oauth.client.dto.ClientDto;
import org.jepria.oauth.client.dto.ClientSearchDto;
import org.jepria.oauth.sdk.util.URIUtil;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDtoLocal;
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
@WithClientCredentials
@Priority(Priorities.AUTHENTICATION)
public final class ClientCredentialsRequestFilter implements ContainerRequestFilter {

  @Context
  HttpServletRequest request;
  AuthenticationService authenticationService = AuthenticationServerFactory.getInstance().getService();

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

  private SessionDto getRequestDto(String clientId, String authCode) {
    SessionSearchDtoLocal searchDto = new SessionSearchDtoLocal();
    searchDto.setClientId(clientId);
    searchDto.setAuthorizationCode(authCode);
    List<SessionDto> result = (List<SessionDto>) AuthorizationServerFactory.getInstance().getDao().find(searchDto, 1);
    if (result.size() == 1) {
      SessionDto client = result.get(0);
      return client;
    } else {
      return null;
    }
  }

  private void login(ContainerRequestContext requestContext) throws LoginException {
    String authString = requestContext.getHeaderString("authorization");
    if (authString != null) {
      /*
      Client credentials in Authorization Header
       */
      authString = authString.replaceFirst("[Bb]asic ", "");
      String[] credentials = new String(Base64.getUrlDecoder().decode(authString)).split(":");
      Integer clientId = authenticationService.loginByClientCredentials(credentials[0], credentials[1]);
      ClientDto client = getClient(credentials[0]);
      requestContext.setSecurityContext(new ClientSecurityContext(clientId, client.getClientName()));
    } else {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try (InputStream in = requestContext.getEntityStream()) {
        ReaderWriter.writeTo(in, out);
        byte[] requestEntity = out.toByteArray();
        Map<String, String> parameters = URIUtil.parseParameters(new String(requestEntity), null);
        if (parameters.get(CLIENT_ID) != null && parameters.get(CLIENT_SECRET) != null) {
          Integer clientId = authenticationService.loginByClientCredentials(parameters.get(CLIENT_ID), parameters.get(CLIENT_SECRET));
          ClientDto client = getClient(parameters.get(CLIENT_ID));
          requestContext.setSecurityContext(new ClientSecurityContext(clientId, client.getClientName()));
        } else if (parameters.get(CLIENT_ID) != null && parameters.get(CLIENT_SECRET) == null && parameters.get(CODE) != null) {
          /*
           * PKCE for public clients
           * https://tools.ietf.org/html/rfc7636
           */
          SessionDto authRequest = getRequestDto(parameters.get(CLIENT_ID), parameters.get(CODE));
          if (authRequest != null && authRequest.getCodeChallenge() == null && parameters.get(CODE_VERIFIER) == null) {
            MessageDigest cryptoProvider = MessageDigest.getInstance("SHA-256");
            if (authRequest
              .getCodeChallenge()
              .equals(Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(cryptoProvider.digest(parameters.get(CODE_VERIFIER).getBytes())))) {
              Integer clientId = authenticationService.loginByClientId(parameters.get(CLIENT_ID));
              ClientDto client = getClient(parameters.get(CLIENT_ID));
              requestContext.setSecurityContext(new ClientSecurityContext(clientId, client.getClientName()));
            }
          }
        } else {
          throw new LoginException();
        }
        requestContext.setEntityStream(new ByteArrayInputStream(requestEntity));
      } catch (IOException | NoSuchAlgorithmException ex) {
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
    private final String clientName;

    public ClientSecurityContext(Integer clientId, String clientName) {
      this.clientId = clientId;
      this.clientName = clientName;
    }

    @Override
    public boolean isUserInRole(final String roleName) {
      return true;
    }

    @Override
    public Principal getUserPrincipal() {
      return new PrincipalImpl(clientName, clientId);
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
