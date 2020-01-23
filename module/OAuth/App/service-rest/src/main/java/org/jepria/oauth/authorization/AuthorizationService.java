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
import org.jepria.oauth.sdk.ResponseType;
import org.jepria.server.data.RuntimeSQLException;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.*;

import static org.jepria.oauth.authorization.AuthorizationFieldNames.AUTH_REQUEST_ID;
import static org.jepria.oauth.sdk.OAuthConstants.*;

public class AuthorizationService {

  public AuthRequestDto authorize(String responseType, String clientId, String redirectUri) {
    if (!ResponseType.implies(responseType)) {
      throw new IllegalStateException("Unsupported response_type");
    }
    if (!isValidUri(redirectUri)) {
      throw new IllegalArgumentException("redirect_uri is invalid");
    }
    AuthRequestDto result = null;
    AuthRequestCreateDto authRequestDto = new AuthRequestCreateDto();
    authRequestDto.setAuthorizationCode(generateCode());
    authRequestDto.setClientId(clientId);
    authRequestDto.setRedirectUri(redirectUri);
    try {
      List<AuthRequestDto> authRequestList = (List<AuthRequestDto>) AuthorizationServerFactory.getInstance().getDao().findByPrimaryKey(new HashMap<String, Integer>() {{
        put(AUTH_REQUEST_ID, (create(authRequestDto)));
      }}, 1);
      result = authRequestList.get(0);
    } catch (RuntimeSQLException ex) {
      SQLException sqlException = ex.getSQLException();
      if (sqlException.getErrorCode() == 20001) {
        throw new IllegalArgumentException("client_id is not valid");
      }
      if (sqlException.getErrorCode() == 20002) {
        throw new IllegalArgumentException("redirect_uri mismatch");
      }
    }
    return result;
  }

  /**
   *
   * @param template
   * @return
   */
  public List<AuthRequestDto> find(AuthRequestSearchDtoLocal template) {
    template.setHasToken(false);
    List<AuthRequestDto> result = (List<AuthRequestDto>) AuthorizationServerFactory.getInstance().getDao().find(template, 1);
    return result;
  }

  /**
   *
   * @param record
   * @return
   */
  public Integer create(AuthRequestCreateDto record) {
    Integer result = (Integer) AuthorizationServerFactory.getInstance().getDao().create(record, 1);
    if (result == null) {
      throw new RuntimeException("Record was not created.");
    }
    return result;
  }

  /**
   *
   * @param record
   */
  public void update(AuthRequestUpdateDto record) {
    if (record.getAuthRequestId() == null) {
      throw new IllegalArgumentException("Primary key must be not null");
    }
    AuthorizationServerFactory.getInstance().getDao().update(new HashMap<String, Integer>(){{
      put(AUTH_REQUEST_ID, record.getAuthRequestId());
    }}, record, null);
  }

  /**
   *
   * @param authRequestId
   */
  public void block(Integer authRequestId) {
    AuthorizationServerFactory.getInstance().getDao().blockAuthRequest(authRequestId);
  }

  /**
   *
   * @return
   */
  public String generateCode() {
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

  /**
   *
   * @param redirectUri
   * @return
   */
  private boolean isValidUri(String redirectUri)  {
    if (redirectUri == null) {
      return false;
    }
    try {
      new URI(redirectUri);
      return true;
    } catch (URISyntaxException | NullPointerException e) {
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
