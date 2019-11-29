package org.jepria.oauth.authorization;

import org.jepria.oauth.authorization.dto.AuthRequestCreateDto;
import org.jepria.oauth.authorization.dto.AuthRequestDto;
import org.jepria.oauth.authorization.dto.AuthRequestSearchDto;
import org.jepria.oauth.authorization.dto.AuthRequestUpdateDto;
import org.jepria.oauth.client.ClientServerFactory;
import org.jepria.oauth.client.dto.ClientDto;
import org.jepria.oauth.client.dto.ClientSearchDto;
import org.jepria.oauth.clienturi.ClientUriServerFactory;
import org.jepria.oauth.clienturi.dto.ClientUriDto;
import org.jepria.oauth.clienturi.dto.ClientUriSearchDtoLocal;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.*;

import static org.jepria.oauth.authorization.AuthorizationFieldNames.AUTH_REQUEST_ID;
import static org.jepria.oauth.sdk.OAuthConstants.*;

public class AuthorizationService {

  public Response authorize(String responseType, String clientId, String redirectUriEncoded, String state, String host) {
    String redirectUri = new String(Base64.getUrlDecoder().decode(redirectUriEncoded));
    if (!isValidUri(redirectUri)) {
      return Response.status(400).entity("redirect_uri is invalid or not avaliable").build();
    }

    Response response = null;
    try {
      if (CODE.equalsIgnoreCase(responseType)) {
        AuthRequestDto authRequest = initializeAuthorizationRequest(clientId, redirectUri);
        response = Response.temporaryRedirect(new URI("/oauth/login/?"
          + RESPONSE_TYPE + "=" + CODE
          + "&" + CODE + "=" + authRequest.getAuthorizationCode()
          + "&" + REDIRECT_URI + "=" + redirectUriEncoded
          + "&" + CLIENT_NAME + "=" + authRequest.getClient().getName()
          + "&" + STATE + "=" + state)).build();
      } else if (TOKEN.equalsIgnoreCase(responseType)) {
        response = Response.temporaryRedirect(URI.create(redirectUri + getSeparator(redirectUri) + ERROR + UNSUPPORTED_RESPONSE_TYPE)).build();
      } else {
        response = Response.temporaryRedirect(URI.create(redirectUri + getSeparator(redirectUri) + ERROR + UNSUPPORTED_RESPONSE_TYPE)).build();
      }
    } catch (IllegalArgumentException | NoSuchElementException e) {
      e.printStackTrace();
      response = Response.temporaryRedirect(URI.create(redirectUri + getSeparator(redirectUri) + ERROR + INVALID_REQUEST + "&" + ERROR_DESCRIPTION + URLEncoder.encode(e.getMessage(), "UTF-8"))).build();
    } catch (Throwable e) {
      e.printStackTrace();
      response = Response.temporaryRedirect(URI.create(redirectUri + getSeparator(redirectUri) + ERROR + "&" + SERVER_ERROR)).build();
    } finally {
      return response;
    }
  }

  public AuthRequestDto initializeAuthorizationRequest(String clientCode, String redirectUri) throws IllegalArgumentException {
    AuthRequestDto result;
    ClientSearchDto clientSearchTemplate = new ClientSearchDto();
    clientSearchTemplate.setClientCode(clientCode);
    List<ClientDto> clientList = (List<ClientDto>) ClientServerFactory.getInstance().getDao().find(clientSearchTemplate, 1);
    if (clientList.isEmpty() || clientList.size() > 1) {
      throw new NoSuchElementException("client_id not found");
    }
    ClientUriSearchDtoLocal clientUriSearchTemplate = new ClientUriSearchDtoLocal();
    clientUriSearchTemplate.setClientId(clientList.get(0).getClientId());
    List<ClientUriDto> clientUriList = (List<ClientUriDto>) ClientUriServerFactory.getInstance().getDao().find(clientUriSearchTemplate, 1);
    if (clientUriList.stream().anyMatch(clientUri -> clientUri.getClientUri().equals(redirectUri) || redirectUri.startsWith(clientUri.getClientUri()))) {
      AuthRequestCreateDto authRequestDto = new AuthRequestCreateDto();
      authRequestDto.setAuthorizationCode(generateCode());
      authRequestDto.setClientCode(clientCode);
      authRequestDto.setRedirectUri(redirectUri);
      List<AuthRequestDto> authRequestList = (List<AuthRequestDto>) AuthorizationServerFactory.getInstance().getDao().findByPrimaryKey(new HashMap<String, Integer>() {{
        put(AUTH_REQUEST_ID, (create(authRequestDto)));
      }}, 1);
      if (authRequestList.size() == 1) {
        result = authRequestList.get(0);
      } else {
        throw new NoSuchElementException("authorization request not found");
      }
    } else {
      throw new IllegalArgumentException("redirect_uri doesn't match to Client URI whitelist");
    }
    return result;
  }

  public List<AuthRequestDto> find(AuthRequestSearchDto template) {
    List<AuthRequestDto> result = (List<AuthRequestDto>) AuthorizationServerFactory.getInstance().getDao().find(template, null);
    return result;
  }

  public Integer create(AuthRequestCreateDto record) {
    if (record.getAuthorizationCode() == null) {
      throw new IllegalArgumentException("Authorization code must be not null");
    }
    if (record.getRedirectUri() == null) {
      throw new IllegalArgumentException("Redirect URI must be not null");
    }
    if (record.getClientCode() == null) {
      throw new IllegalArgumentException("Client ID must be not null");
    }
    Integer result = (Integer) AuthorizationServerFactory.getInstance().getDao().create(record, null);
    if (result == null) {
      throw new RuntimeException("Record was not created.");
    }
    return result;
  }

  public void update(AuthRequestUpdateDto record) {
    if (record.getAuthRequestId() == null) {
      throw new IllegalArgumentException("Primary key must be not null");
    }
    AuthorizationServerFactory.getInstance().getDao().update(new HashMap<String, Integer>(){{
      put(AUTH_REQUEST_ID, record.getAuthRequestId());
    }}, record, null);
  }

  private String generateCode() {
    try {
      MessageDigest cryptoProvider = MessageDigest.getInstance("SHA-256");
      UUID randomUuid = UUID.randomUUID();
      cryptoProvider.update(randomUuid.toString().getBytes());
      return Base64.getUrlEncoder().withoutPadding().encodeToString(cryptoProvider.digest());
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isValidUri(String redirectUri)  {
    try {
      new URI(redirectUri);
      return true;
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Get next separator for URI
   *
   * @param uri
   * @return
   */
  public static String getSeparator(String uri) {
    String separator = "";
    if (uri != null) {
      if (uri.contains("?")) {
        separator = "&";
      } else if (uri.endsWith("/")) {
        separator = "?";
      } else {
        separator = "/?";
      }
    }
    return separator;
  }
}
