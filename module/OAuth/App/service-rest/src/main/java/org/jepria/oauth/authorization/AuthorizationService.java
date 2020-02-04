package org.jepria.oauth.authorization;

import org.jepria.oauth.authorization.dto.AuthRequestCreateDto;
import org.jepria.oauth.authorization.dto.AuthRequestDto;
import org.jepria.oauth.authorization.dto.AuthRequestSearchDtoLocal;
import org.jepria.oauth.authorization.dto.AuthRequestUpdateDto;
import org.jepria.oauth.client.ClientServerFactory;
import org.jepria.oauth.clienturi.ClientUriServerFactory;
import org.jepria.oauth.clienturi.dto.ClientUriDto;
import org.jepria.oauth.clienturi.dto.ClientUriSearchDtoLocal;
import org.jepria.oauth.main.exception.HandledRuntimeException;
import org.jepria.oauth.sdk.ResponseType;
import org.jepria.oauth.sdk.token.DecryptorRSA;
import org.jepria.oauth.sdk.token.SignatureVerifierRSA;
import org.jepria.oauth.sdk.token.TokenImpl;
import org.jepria.oauth.sdk.token.VerifierRSA;
import org.jepria.oauth.sdk.token.interfaces.Token;
import org.jepria.oauth.sdk.token.interfaces.Verifier;
import org.jepria.server.data.OptionDto;
import org.jepria.server.data.RuntimeSQLException;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.*;

import static org.jepria.oauth.authorization.AuthorizationFieldNames.AUTH_REQUEST_ID;
import static org.jepria.oauth.sdk.OAuthConstants.UNAUTHORIZED_CLIENT;
import static org.jepria.oauth.sdk.OAuthConstants.UNSUPPORTED_RESPONSE_TYPE;

public class AuthorizationService {

  public AuthRequestDto authorize(String responseType, String clientId, String redirectUri, String codeChallenge) {
      if (!ResponseType.implies(responseType)) {
      throw new HandledRuntimeException(UNSUPPORTED_RESPONSE_TYPE);
    }

    List<OptionDto<String>> clientResponseTypes = ClientServerFactory.getInstance().getService().getClientResponseTypes(clientId);
    if (clientResponseTypes.size() == 0 || !clientResponseTypes.stream().anyMatch(clientResponseType -> clientResponseType.getValue().equals(responseType))) {
      throw new HandledRuntimeException(UNAUTHORIZED_CLIENT);
    }

    AuthRequestCreateDto authRequestDto = new AuthRequestCreateDto();
    authRequestDto.setAuthorizationCode(generateCode());
    authRequestDto.setClientId(clientId);
    authRequestDto.setRedirectUri(redirectUri);
    authRequestDto.setCodeChallenge(codeChallenge);
    try {
      List<AuthRequestDto> authRequestList = (List<AuthRequestDto>) AuthorizationServerFactory.getInstance().getDao().findByPrimaryKey(new HashMap<String, Integer>() {{
        put(AUTH_REQUEST_ID, (create(authRequestDto)));
      }}, 1);
      return authRequestList.get(0);
    } catch (RuntimeSQLException ex) {
      SQLException sqlException = ex.getSQLException();
      if (sqlException.getErrorCode() == 20001) {
        throw new HandledRuntimeException(UNAUTHORIZED_CLIENT);
      }
      if (sqlException.getErrorCode() == 20002) {
        throw new IllegalArgumentException("redirect_uri mismatch");
      }
      throw new RuntimeSQLException(sqlException);
    }
  }

  public AuthRequestDto authorize(String responseType, String clientId, String redirectUri, String codeChallenge, String sessionToken, String issuer, String publicKey, String privateKey) {
    if (!ResponseType.implies(responseType)) {
      throw new HandledRuntimeException(UNSUPPORTED_RESPONSE_TYPE);
    }
    List<OptionDto<String>> clientResponseTypes = ClientServerFactory.getInstance().getService().getClientResponseTypes(clientId);
    if (clientResponseTypes.size() == 0 || !clientResponseTypes.stream().anyMatch(clientResponseType -> clientResponseType.getValue().equals(responseType))) {
      throw new HandledRuntimeException(UNAUTHORIZED_CLIENT);
    }
    try {
      Token token = TokenImpl.parseFromString(sessionToken);
      token.decrypt(new DecryptorRSA(privateKey));
      Verifier verifier = new VerifierRSA(null, issuer, new Date(), publicKey);
      if (verifier.verify(token)) {
        String[] subject = token.getSubject().split(":");

        AuthRequestCreateDto authRequestDto = new AuthRequestCreateDto();
        authRequestDto.setAuthorizationCode(generateCode());
        authRequestDto.setClientId(clientId);
        authRequestDto.setRedirectUri(redirectUri);
        authRequestDto.setOperatorId(Integer.valueOf(subject[1]));
        authRequestDto.setSessionTokenId(token.getJti());
        authRequestDto.setSessionTokenDateIns(token.getIssueTime());
        authRequestDto.setSessionTokenDateFinish(token.getExpirationTime());
        authRequestDto.setCodeChallenge(codeChallenge);
        List<AuthRequestDto> authRequestList = (List<AuthRequestDto>) AuthorizationServerFactory.getInstance().getDao().findByPrimaryKey(new HashMap<String, Integer>() {{
          put(AUTH_REQUEST_ID, (create(authRequestDto)));
        }}, 1);
        return authRequestList.get(0);
      } else {
        return authorize(responseType, clientId, redirectUri, codeChallenge);
      }
    } catch (RuntimeSQLException ex) {
      SQLException sqlException = ex.getSQLException();
      if (sqlException.getErrorCode() == 20001) {
        throw new HandledRuntimeException(UNAUTHORIZED_CLIENT);
      }
      if (sqlException.getErrorCode() == 20002) {
        throw new IllegalArgumentException("redirect_uri mismatch");
      }
      throw new RuntimeSQLException(sqlException);
    } catch (Throwable th) {
      th.printStackTrace();
      return authorize(responseType, clientId, redirectUri, codeChallenge);
    }
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

  public void logout(String clientId, String redirectUri, String sessionToken, String issuer, String publicKey, String privateKey) {
    ClientUriSearchDtoLocal clientUriSearchTemplate = new ClientUriSearchDtoLocal();
    clientUriSearchTemplate.setClientId(clientId);
    List<ClientUriDto> clientUriList = ClientUriServerFactory.getInstance().getService().findClientUri(clientUriSearchTemplate, null);
    if (!clientUriList.stream().anyMatch(clientUriDto -> clientUriDto.getClientUri().equals(redirectUri))) {
      throw new IllegalArgumentException("redirect_uri mismatch");
    }

    try {
      Token token = TokenImpl.parseFromString(sessionToken);
      token.decrypt(new DecryptorRSA(privateKey));
      Verifier verifier = new SignatureVerifierRSA(publicKey);
      if (verifier.verify(token) && issuer.equals(token.getIssuer())) {
        AuthRequestSearchDtoLocal searchTemplate = new AuthRequestSearchDtoLocal();
        searchTemplate.setSessionId(token.getJti());
        searchTemplate.setBlocked(false);
        AuthorizationServerFactory.getInstance()
          .getDao()
          .find(searchTemplate, null)
          .stream()
          .forEach(authRequestDto -> block(((AuthRequestDto) authRequestDto).getAuthRequestId()));
      }
    } catch (Throwable th) {
      th.printStackTrace();
      throw new RuntimeException(th);
    }
  }
}
