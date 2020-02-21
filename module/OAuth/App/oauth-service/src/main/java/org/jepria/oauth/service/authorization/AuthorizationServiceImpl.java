package org.jepria.oauth.service.authorization;

import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.model.authorization.AuthorizationService;
import org.jepria.oauth.model.client.ClientService;
import org.jepria.oauth.model.clienturi.ClientUriService;
import org.jepria.oauth.model.clienturi.dto.ClientUriDto;
import org.jepria.oauth.model.clienturi.dto.ClientUriSearchDto;
import org.jepria.oauth.model.session.SessionService;
import org.jepria.oauth.model.session.dto.SessionCreateDto;
import org.jepria.oauth.model.session.dto.SessionDto;
import org.jepria.oauth.model.session.dto.SessionSearchDto;
import org.jepria.oauth.sdk.ResponseType;
import org.jepria.oauth.sdk.token.Decryptor;
import org.jepria.oauth.sdk.token.Token;
import org.jepria.oauth.sdk.token.TokenImpl;
import org.jepria.oauth.sdk.token.Verifier;
import org.jepria.oauth.sdk.token.rsa.DecryptorRSA;
import org.jepria.oauth.sdk.token.rsa.SignatureVerifierRSA;
import org.jepria.oauth.sdk.token.rsa.VerifierRSA;
import org.jepria.server.data.RuntimeSQLException;
import org.jepria.server.service.security.Credential;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

import static org.jepria.oauth.sdk.OAuthConstants.*;

public class AuthorizationServiceImpl implements AuthorizationService {

  private final SessionService sessionService;
  private final ClientService clientService;
  
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

  public AuthorizationServiceImpl(SessionService sessionService, ClientService clientService) {
    this.clientService = clientService;
    this.sessionService = sessionService;
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
      return (SessionDto) sessionService.getRecordById(String.valueOf(sessionService.create(sessionDto, serverCredential)), serverCredential);
    } catch (RuntimeSQLException ex) {
      SQLException sqlException = ex.getSQLException();
      if (sqlException.getErrorCode() == 20001) {
        throw new OAuthRuntimeException(UNAUTHORIZED_CLIENT, "Client ID not found");
      }
      if (sqlException.getErrorCode() == 20002) {
        throw new OAuthRuntimeException(INVALID_REQUEST, "redirect_uri mismatch");
      }
      throw new RuntimeSQLException(sqlException);
    }
  }

  @Override
  public SessionDto authorize(String responseType,
                              String clientId,
                              String redirectUri,
                              String codeChallenge,
                              String sessionToken,
                              String issuer,
                              String publicKey,
                              String privateKey) {
    if (!ResponseType.implies(responseType)) {
      throw new OAuthRuntimeException(UNSUPPORTED_RESPONSE_TYPE);
    }
    List<String> clientResponseTypes = clientService.getClientResponseTypes(clientId);
    if (clientResponseTypes.size() == 0 || !clientResponseTypes.stream().anyMatch(clientResponseType -> clientResponseType.equals(responseType))) {
      throw new OAuthRuntimeException(UNAUTHORIZED_CLIENT, "Client doesn't have enough permissions to use responseType=" + responseType);
    }
    try {
      Token token = TokenImpl.parseFromString(sessionToken);
      Decryptor decryptor = new DecryptorRSA(privateKey);
      token = decryptor.decrypt(token);
      Verifier verifier = new VerifierRSA(null, issuer, new Date(), publicKey);
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
      if (sqlException.getErrorCode() == 20002) {
        throw new OAuthRuntimeException(INVALID_REQUEST, "redirect_uri mismatch");
      }
      throw new RuntimeSQLException(sqlException);
    } catch (ParseException ex) {
      /**
       * Получен битый токен сессии, запрашиваем авторизацию заново без сессии
       */
      return authorize(responseType, clientId, redirectUri, codeChallenge);
    }
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
    } catch (Throwable th) {
      throw new OAuthRuntimeException(SERVER_ERROR, th);
    }
  }
}
