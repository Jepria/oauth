package org.jepria.oauth.authorization;

import org.jepria.oauth.authorization.dto.AuthRequestCreateDto;
import org.jepria.oauth.authorization.dto.AuthRequestDto;
import org.jepria.oauth.authorization.dto.AuthRequestSearchDtoLocal;
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
import java.security.SecureRandom;
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
        response =  Response.status(302).location(new URI("/oauth/login/?"
          + RESPONSE_TYPE + "=" + CODE
          + "&" + CODE + "=" + authRequest.getAuthorizationCode()
          + "&" + REDIRECT_URI + "=" + redirectUriEncoded
          + "&" + CLIENT_NAME + "=" + authRequest.getClient().getName()
          + "&" + STATE + "=" + state)).build();
      } else if (TOKEN.equalsIgnoreCase(responseType)) {
        response =  Response.status(302).location(URI.create(redirectUri + getSeparator(redirectUri) + ERROR + UNSUPPORTED_RESPONSE_TYPE)).build();
      } else {
        response =  Response.status(302).location(URI.create(redirectUri + getSeparator(redirectUri) + ERROR + UNSUPPORTED_RESPONSE_TYPE)).build();
      }
    } catch (IllegalArgumentException | NoSuchElementException e) {
      e.printStackTrace();
      response =  Response.status(302).location(URI.create(redirectUri + getSeparator(redirectUri) + ERROR + INVALID_REQUEST + "&" + ERROR_DESCRIPTION + URLEncoder.encode(e.getMessage(), "UTF-8"))).build();
    } catch (Throwable e) {
      e.printStackTrace();
      response =  Response.status(302).location(URI.create(redirectUri + getSeparator(redirectUri) + ERROR + "&" + SERVER_ERROR)).build();
    } finally {
      return response;
    }
  }

  public AuthRequestDto initializeAuthorizationRequest(String clientId, String redirectUri) throws IllegalArgumentException {
    AuthRequestDto result;
    ClientSearchDto clientSearchTemplate = new ClientSearchDto();
    clientSearchTemplate.setClientId(clientId);
    List<ClientDto> clientList = (List<ClientDto>) ClientServerFactory.getInstance().getDao().find(clientSearchTemplate, 1);
    if (clientList.isEmpty() || clientList.size() > 1) {
      throw new NoSuchElementException("client not found");
    }
    ClientUriSearchDtoLocal clientUriSearchTemplate = new ClientUriSearchDtoLocal();
    clientUriSearchTemplate.setClientId(clientList.get(0).getClientId());
    List<ClientUriDto> clientUriList = (List<ClientUriDto>) ClientUriServerFactory.getInstance().getDao().find(clientUriSearchTemplate, 1);
    if (clientUriList.stream().anyMatch(clientUri -> clientUri.getClientUri().equals(redirectUri) || redirectUri.startsWith(clientUri.getClientUri()))) {
      AuthRequestCreateDto authRequestDto = new AuthRequestCreateDto();
      authRequestDto.setAuthorizationCode(generateCode());
      authRequestDto.setClientId(clientId);
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

  public List<AuthRequestDto> find(AuthRequestSearchDtoLocal template) {
    template.setFinished(false);
    List<AuthRequestDto> result = (List<AuthRequestDto>) AuthorizationServerFactory.getInstance().getDao().find(template, 1);
    return result;
  }

  private Integer create(AuthRequestCreateDto record) {
    if (record.getAuthorizationCode() == null) {
      throw new IllegalArgumentException("Authorization code must be not null");
    }
    if (record.getRedirectUri() == null) {
      throw new IllegalArgumentException("Redirect URI must be not null");
    }
    if (record.getClientId() == null) {
      throw new IllegalArgumentException("Client ID must be not null");
    }
    Integer result = (Integer) AuthorizationServerFactory.getInstance().getDao().create(record, 1);
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

  public void block(Integer authRequestId) {
    AuthorizationServerFactory.getInstance().getDao().blockAuthRequest(authRequestId);
  }

  private String generateCode() {
    try {
      UUID randomUuid = UUID.randomUUID();
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
      byte[] salt = new byte[16];
      random.nextBytes(salt);
      md.update(salt);
      return Base64.getUrlEncoder().withoutPadding().encodeToString(md.digest(randomUuid.toString().getBytes()));
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
