package org.jepria.oauth.authorization;

import org.jepria.oauth.client.ClientServerFactory;
import org.jepria.oauth.clienturi.ClientUriServerFactory;
import org.jepria.oauth.clienturi.dto.ClientUriDto;
import org.jepria.oauth.clienturi.dto.ClientUriSearchDtoLocal;
import org.jepria.oauth.main.exception.HandledRuntimeException;
import org.jepria.oauth.sdk.ResponseType;
import org.jepria.oauth.sdk.token.Decryptor;
import org.jepria.oauth.sdk.token.Token;
import org.jepria.oauth.sdk.token.TokenImpl;
import org.jepria.oauth.sdk.token.Verifier;
import org.jepria.oauth.sdk.token.rsa.DecryptorRSA;
import org.jepria.oauth.sdk.token.rsa.SignatureVerifierRSA;
import org.jepria.oauth.sdk.token.rsa.VerifierRSA;
import org.jepria.oauth.session.SessionService;
import org.jepria.oauth.session.dto.SessionCreateDto;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDtoLocal;
import org.jepria.server.data.OptionDto;
import org.jepria.server.data.RuntimeSQLException;
import org.jepria.server.service.security.Credential;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

import static org.jepria.oauth.sdk.OAuthConstants.*;

public class AuthorizationService {

  private final SessionService sessionService;

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

  public AuthorizationService(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  public SessionDto authorize(String responseType,
                              String clientId,
                              String redirectUri,
                              String codeChallenge) {
    if (!ResponseType.implies(responseType)) {
      throw new HandledRuntimeException(UNSUPPORTED_RESPONSE_TYPE);
    }

    List<OptionDto<String>> clientResponseTypes = ClientServerFactory.getInstance().getService().getClientResponseTypes(clientId);
    if (clientResponseTypes.size() == 0 || !clientResponseTypes.stream().anyMatch(clientResponseType -> clientResponseType.getValue().equals(responseType))) {
      throw new HandledRuntimeException(UNAUTHORIZED_CLIENT, "Client doesn't have enough permissions to use responseType=" + responseType);
    }

    SessionCreateDto sessionDto = new SessionCreateDto();
    sessionDto.setAuthorizationCode(generateCode());
    sessionDto.setClientId(clientId);
    sessionDto.setRedirectUri(redirectUri);
    sessionDto.setCodeChallenge(codeChallenge);
    try {
      return sessionService.findByPrimaryKey(sessionService.create(sessionDto, serverCredential), serverCredential);
    } catch (RuntimeSQLException ex) {
      SQLException sqlException = ex.getSQLException();
      if (sqlException.getErrorCode() == 20001) {
        throw new HandledRuntimeException(UNAUTHORIZED_CLIENT, "Client ID not found");
      }
      if (sqlException.getErrorCode() == 20002) {
        throw new HandledRuntimeException(INVALID_REQUEST, "redirect_uri mismatch");
      }
      throw new RuntimeSQLException(sqlException);
    }
  }

  public SessionDto authorize(String responseType,
                              String clientId,
                              String redirectUri,
                              String codeChallenge,
                              String sessionToken,
                              String issuer,
                              String publicKey,
                              String privateKey) {
    if (!ResponseType.implies(responseType)) {
      throw new HandledRuntimeException(UNSUPPORTED_RESPONSE_TYPE);
    }
    List<OptionDto<String>> clientResponseTypes = ClientServerFactory.getInstance().getService().getClientResponseTypes(clientId);
    if (clientResponseTypes.size() == 0 || !clientResponseTypes.stream().anyMatch(clientResponseType -> clientResponseType.getValue().equals(responseType))) {
      throw new HandledRuntimeException(UNAUTHORIZED_CLIENT, "Client doesn't have enough permissions to use responseType=" + responseType);
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
        return sessionService.findByPrimaryKey(sessionService.create(sessionDto, serverCredential), serverCredential);
      } else {
        return authorize(responseType, clientId, redirectUri, codeChallenge);
      }
    } catch (RuntimeSQLException ex) {
      SQLException sqlException = ex.getSQLException();
      if (sqlException.getErrorCode() == 20001) {
        throw new HandledRuntimeException(UNAUTHORIZED_CLIENT, "Client ID not found");
      }
      if (sqlException.getErrorCode() == 20002) {
        throw new HandledRuntimeException(INVALID_REQUEST, "redirect_uri mismatch");
      }
      throw new RuntimeSQLException(sqlException);
    } catch (ParseException ex) {
      /**
       * Получен битый токен сессии, запрашиваем авторизацию заново без сессии
       */
      return authorize(responseType, clientId, redirectUri, codeChallenge);
    }
  }

  /**
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
    } catch (Throwable th) {
      throw new HandledRuntimeException(SERVER_ERROR, th);
    }
  }

  public void logout(String clientId,
                     String redirectUri,
                     String sessionToken,
                     String issuer,
                     String publicKey,
                     String privateKey) {
    ClientUriSearchDtoLocal clientUriSearchTemplate = new ClientUriSearchDtoLocal();
    clientUriSearchTemplate.setClientId(clientId);
    List<ClientUriDto> clientUriList = ClientUriServerFactory.getInstance().getService().findClientUri(clientUriSearchTemplate, null);
    if (!clientUriList.stream().anyMatch(clientUriDto -> clientUriDto.getClientUri().equals(redirectUri))) {
      throw new HandledRuntimeException(INVALID_REQUEST, "redirect_uri mismatch");
    }

    try {
      Token token = TokenImpl.parseFromString(sessionToken);
      Decryptor decryptor = new DecryptorRSA(privateKey);
      token = decryptor.decrypt(token);
      Verifier verifier = new SignatureVerifierRSA(publicKey);
      if (verifier.verify(token) && issuer.equals(token.getIssuer())) {
        SessionSearchDtoLocal searchTemplate = new SessionSearchDtoLocal();
        searchTemplate.setSessionTokenId(token.getJti());
        searchTemplate.setBlocked(false);
        sessionService
          .find(searchTemplate, serverCredential)
          .stream()
          .forEach(sessionDto -> sessionService.delete(sessionDto.getSessionId(), serverCredential));
      }
    } catch (ParseException ex) {
      throw new HandledRuntimeException(SERVER_ERROR, ex);
    }
  }
}
