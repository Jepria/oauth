package org.jepria.oauth.authorization;

import org.jepria.oauth.client.ClientService;
import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.key.KeyService;
import org.jepria.oauth.key.dto.KeyDto;
import org.jepria.oauth.sdk.ResponseType;
import org.jepria.oauth.sdk.token.Decryptor;
import org.jepria.oauth.sdk.token.Token;
import org.jepria.oauth.sdk.token.TokenImpl;
import org.jepria.oauth.sdk.token.Verifier;
import org.jepria.oauth.sdk.token.rsa.DecryptorRSA;
import org.jepria.oauth.sdk.token.rsa.VerifierRSA;
import org.jepria.oauth.session.SessionService;
import org.jepria.oauth.session.dto.SessionCreateDto;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.server.data.RuntimeSQLException;
import org.jepria.server.service.security.Credential;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.jepria.oauth.sdk.OAuthConstants.*;

public class AuthorizationServiceImpl implements AuthorizationService {

  private final SessionService sessionService;
  private final ClientService clientService;
  private final KeyService keyService;
  
  private Credential serverCredential = new Credential() {
    @Override
    public int getOperatorId() {
      return 1;
    }

    @Override
    public String getUsername() {
      return "SERVER";
    }

    @Override
    public boolean isUserInRole(String roleShortName) {
      return true;
    }
  };

  public AuthorizationServiceImpl(SessionService sessionService, ClientService clientService, KeyService keyService) {
    this.clientService = clientService;
    this.sessionService = sessionService;
    this.keyService = keyService;
  }

  @Override
  public SessionDto authorize(String responseType,
                              String clientId,
                              String redirectUri,
                              String codeChallenge) {
    if (!ResponseType.implies(responseType)) {
      throw new OAuthRuntimeException(UNSUPPORTED_RESPONSE_TYPE);
    }

    List<String> clientResponseTypes = clientService.getClientResponseTypes(clientId);
    if (clientResponseTypes.size() == 0 || !clientResponseTypes.stream().anyMatch(clientResponseType -> clientResponseType.equals(responseType))) {
      throw new OAuthRuntimeException(UNAUTHORIZED_CLIENT, "Client doesn't have enough permissions to use responseType=" + responseType);
    }

    SessionCreateDto sessionDto = new SessionCreateDto();
    sessionDto.setAuthorizationCode(generateCode());
    sessionDto.setClientId(clientId);
    sessionDto.setRedirectUri(redirectUri);
    sessionDto.setCodeChallenge(codeChallenge);
    try {
      String sessionId = String.valueOf(sessionService.create(sessionDto, serverCredential));
      return (SessionDto) sessionService.getRecordById(sessionId, serverCredential);
    } catch (RuntimeSQLException ex) {
      SQLException sqlException = ex.getSQLException();
      if (sqlException.getErrorCode() == 20001) {
        throw new OAuthRuntimeException(UNAUTHORIZED_CLIENT, "Client ID not found");
      }
      if (sqlException.getErrorCode() == 20004) {
        throw new OAuthRuntimeException(INVALID_REQUEST, "redirect_uri mismatch");
      }
      throw new OAuthRuntimeException(SERVER_ERROR, sqlException.getMessage());
    }
  }

  @Override
  public SessionDto authorize(String responseType,
                              String clientId,
                              String redirectUri,
                              String codeChallenge,
                              String sessionToken,
                              String issuer) {
    if (!ResponseType.implies(responseType)) {
      throw new OAuthRuntimeException(UNSUPPORTED_RESPONSE_TYPE);
    }
    List<String> clientResponseTypes = clientService.getClientResponseTypes(clientId);
    if (clientResponseTypes.size() == 0 || !clientResponseTypes.stream().anyMatch(clientResponseType -> clientResponseType.equals(responseType))) {
      throw new OAuthRuntimeException(UNAUTHORIZED_CLIENT, "Client doesn't have enough permissions to use responseType=" + responseType);
    }
    try {
      KeyDto keyDto = keyService.getKeys(null, serverCredential);
      Token token = TokenImpl.parseFromString(sessionToken);
      Decryptor decryptor = new DecryptorRSA(keyDto.getPrivateKey());
      token = decryptor.decrypt(token);
      Verifier verifier = new VerifierRSA(null, issuer, new Date(), keyDto.getPublicKey());
      if (verifier.verify(token)) {
        String[] subject = token.getSubject().split(":");

        SessionCreateDto sessionDto = new SessionCreateDto();
        sessionDto.setAuthorizationCode(generateCode());
        sessionDto.setClientId(clientId);
        sessionDto.setRedirectUri(redirectUri);
        sessionDto.setOperatorId(Integer.valueOf(subject[1]));
        sessionDto.setSessionTokenId(token.getJti());
        sessionDto.setSessionTokenDateIns(token.getIssueTime());
        sessionDto.setSessionTokenDateFinish(token.getExpirationTime());
        sessionDto.setCodeChallenge(codeChallenge);
        return (SessionDto) sessionService.getRecordById(String.valueOf(sessionService.create(sessionDto, serverCredential)), serverCredential);
      } else {
        /**
         * Сессия истекла или не валидна
         */
        return authorize(responseType, clientId, redirectUri, codeChallenge);
      }
    } catch (RuntimeSQLException ex) {
      SQLException sqlException = ex.getSQLException();
      if (sqlException.getErrorCode() == 20001) {
        throw new OAuthRuntimeException(UNAUTHORIZED_CLIENT, "Client ID not found");
      }
      if (sqlException.getErrorCode() == 20004) {
        throw new OAuthRuntimeException(INVALID_REQUEST, "redirect_uri mismatch");
      }
      throw new RuntimeSQLException(sqlException);
    } catch (Throwable ex) {
      return authorize(responseType, clientId, redirectUri, codeChallenge);
    }
  }
  
  private String generateCode() {
    try {
      return UUID.randomUUID().toString().replaceAll("-", "");
    } catch (Throwable th) {
      throw new OAuthRuntimeException(SERVER_ERROR, th);
    }
  }
}
