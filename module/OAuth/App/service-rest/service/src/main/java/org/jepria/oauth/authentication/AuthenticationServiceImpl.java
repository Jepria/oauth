package org.jepria.oauth.authentication;

import org.jepria.oauth.authentication.dao.AuthenticationDao;
import org.jepria.oauth.authentication.dto.SessionTokenDto;
import org.jepria.oauth.clienturi.ClientUriService;
import org.jepria.oauth.clienturi.dto.ClientUriDto;
import org.jepria.oauth.clienturi.dto.ClientUriSearchDto;
import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.key.KeyService;
import org.jepria.oauth.key.dto.KeyDto;
import org.jepria.oauth.sdk.token.*;
import org.jepria.oauth.sdk.token.rsa.DecryptorRSA;
import org.jepria.oauth.sdk.token.rsa.EncryptorRSA;
import org.jepria.oauth.sdk.token.rsa.SignatureVerifierRSA;
import org.jepria.oauth.sdk.token.rsa.SignerRSA;
import org.jepria.oauth.session.SessionService;
import org.jepria.oauth.session.dto.SessionCreateDto;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDto;
import org.jepria.oauth.session.dto.SessionUpdateDto;
import org.jepria.server.data.RuntimeSQLException;
import org.jepria.server.service.security.Credential;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.jepria.oauth.sdk.OAuthConstants.*;

public class AuthenticationServiceImpl implements AuthenticationService {

  private final AuthenticationDao dao;
  private final SessionService sessionService;
  private final ClientUriService clientUriService;
  private final KeyService keyService;

  public AuthenticationServiceImpl(AuthenticationDao dao, SessionService sessionService, ClientUriService clientUriService, KeyService keyService) {
    this.dao = dao;
    this.sessionService = sessionService;
    this.clientUriService = clientUriService;
    this.keyService = keyService;
  }

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

  @Override
  public Integer loginByPassword(String username, String password) {
    Integer operatorId;
    try {
      operatorId = dao.loginByPassword(username, password);
    } catch (RuntimeSQLException th) {
      throw new OAuthRuntimeException(SERVER_ERROR, th);
    }
    if (operatorId == null) {
      throw new OAuthRuntimeException(ACCESS_DENIED, "Wrong username/password.");
    } else {
      return operatorId;
    }
  }

  @Override
  public Integer loginByPasswordHash(String username, String passwordHash) {
    Integer operatorId;
    try {
      operatorId = dao.loginByHash(username, passwordHash);
    } catch (RuntimeSQLException th) {
      throw new OAuthRuntimeException(SERVER_ERROR, th);
    }
    if (operatorId == null) {
      throw new OAuthRuntimeException(ACCESS_DENIED, "Wrong username/password.");
    } else {
      return operatorId;
    }
  }

  @Override
  public Integer loginByClientSecret(String clientId, String clientSecret) {
    if (clientId == null || clientSecret == null) {
      throw new OAuthRuntimeException(ACCESS_DENIED, "credentials must be not null");
    }
    try {
      return dao.loginByClientSecret(clientId, clientSecret);
    } catch (RuntimeSQLException th) {
      if (th.getSQLException().getErrorCode() == 20003) {
        throw new OAuthRuntimeException(ACCESS_DENIED, "Wrong clientId");
      } else {
        throw new OAuthRuntimeException(SERVER_ERROR, th);
      }
    }
  }

  @Override
  public void loginByClientId(String clientId) {
    if (clientId == null) {
      throw new OAuthRuntimeException(ACCESS_DENIED, "clientId must be not null");
    }
    try {
      dao.loginByClientSecret(clientId, null);
    } catch (RuntimeSQLException th) {
      if (th.getSQLException().getErrorCode() == 20003) {
        throw new OAuthRuntimeException(ACCESS_DENIED, "Wrong clientId");
      } else {
        throw new OAuthRuntimeException(SERVER_ERROR, th);
      }
    }
  }

  @Override
  public void loginByAuthorizationCode(String authorizationCode, String clientId, String codeVerifier) {
    loginByClientId(clientId);
    boolean result;
    try {
      result = dao.verifyPKCE(authorizationCode, codeVerifier);
    } catch (Throwable th) {
      throw new OAuthRuntimeException(SERVER_ERROR, th);
    }
    if (!result) {
      throw new OAuthRuntimeException(ACCESS_DENIED, "PKCE verification failed");
    }
  }

  @Override
  public SessionTokenDto authenticate(
    String authCode,
    String redirectUri,
    String clientId,
    String username,
    String password,
    String host,
    Integer sessionTokenLifeTime) {
    SessionDto session = getSession(authCode, clientId, redirectUri);
    if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - session.getDateIns().getTime()) > 10) {
      throw new OAuthRuntimeException(ACCESS_DENIED, "Authorization code not found or has expired");
    }
    if (session.getOperator() != null) {
      throw new OAuthRuntimeException(ACCESS_DENIED, "Request has already passed authentication");
    }
    if (session.getAccessTokenId() != null) {
      throw new OAuthRuntimeException(ACCESS_DENIED, "Request is finished");
    }
    Integer operatorId = loginByPassword(username, password);
    KeyDto keyDto = keyService.getKeys(null, serverCredential);
    Token sessionToken = generateSessionToken(username, operatorId, host, keyDto.getPrivateKey(), sessionTokenLifeTime);
    SessionCreateDto sessionCreateDto = new SessionCreateDto();
    sessionCreateDto.setAuthorizationCode(sessionToken.getJti());
    sessionCreateDto.setSessionTokenId(sessionToken.getJti());
    sessionCreateDto.setClientId(clientId);
    sessionCreateDto.setRedirectUri(redirectUri);
    sessionCreateDto.setSessionTokenDateIns(sessionToken.getIssueTime());
    sessionCreateDto.setSessionTokenDateFinish(sessionToken.getExpirationTime());
    sessionCreateDto.setOperatorId(operatorId);
    sessionService.create(sessionCreateDto, serverCredential);
    updateSession(session,
      operatorId,
      sessionToken.getJti(),
      sessionToken.getIssueTime(),
      sessionToken.getExpirationTime(),
      serverCredential);
    SessionTokenDto sessionTokenDto = new SessionTokenDto();
    sessionTokenDto.setExpirationDate(sessionToken.getExpirationTime());
    /**
     * Encrypt token with public key
     */
    Encryptor encryptor = new EncryptorRSA(keyDto.getPublicKey());
    try {
      sessionToken = encryptor.encrypt(sessionToken);
    } catch (ParseException e) {
      throw new OAuthRuntimeException(SERVER_ERROR, e);
    }
    sessionTokenDto.setToken(sessionToken.asString());
    return sessionTokenDto;
  }

  @Override
  public void logout(String clientId,
                     String redirectUri,
                     String sessionToken,
                     String issuer) {
    ClientUriSearchDto clientUriSearchTemplate = new ClientUriSearchDto();
    clientUriSearchTemplate.setClientId(clientId);
    List<ClientUriDto> clientUriList = clientUriService.findClientUri(clientUriSearchTemplate, 1);
    if (!clientUriList.stream().anyMatch(clientUriDto -> clientUriDto.getClientUri().equals(redirectUri))) {
      throw new OAuthRuntimeException(INVALID_REQUEST, "redirect_uri mismatch");
    }

    try {
      Token token = TokenImpl.parseFromString(sessionToken);
      KeyDto keyDto = keyService.getKeys(null, serverCredential);
      Decryptor decryptor = new DecryptorRSA(keyDto.getPrivateKey());
      token = decryptor.decrypt(token);
      Verifier verifier = new SignatureVerifierRSA(keyDto.getPublicKey());
      if (verifier.verify(token) && issuer.equals(token.getIssuer())) {
        SessionSearchDto searchTemplate = new SessionSearchDto();
        searchTemplate.setSessionTokenId(token.getJti());
        sessionService
          .find(searchTemplate, serverCredential)
          .stream()
          .forEach(sessionDto -> sessionService.deleteRecord(String.valueOf(sessionDto.getSessionId()), serverCredential));
      }
    } catch (ParseException ex) {
      throw new OAuthRuntimeException(SERVER_ERROR, ex);
    }
  }

  private SessionDto getSession(String authCode, String clientId, String redirectUri) {
    SessionSearchDto searchTemplate = new SessionSearchDto();
    searchTemplate.setClientId(clientId);
    searchTemplate.setAuthorizationCode(authCode);
    searchTemplate.setRedirectUri(redirectUri);
    List<SessionDto> sessions = sessionService.find(searchTemplate, serverCredential);
    if (sessions.size() == 1) {
      return sessions.get(0);
    } else {
      throw new OAuthRuntimeException(ACCESS_DENIED, "Session not found");
    }
  }

  private void updateSession(SessionDto sessionDto,
                             Integer operatorId,
                             String sessionTokenId,
                             Date sessionDateIns,
                             Date sessionDateFinish,
                             Credential serverCredential) {
    SessionUpdateDto updateDto = new SessionUpdateDto();
    updateDto.setSessionId(sessionDto.getSessionId());
    updateDto.setAuthorizationCode(sessionDto.getAuthorizationCode());
    updateDto.setClientId(sessionDto.getClient().getValue());
    updateDto.setRedirectUri(sessionDto.getRedirectUri());
    updateDto.setCodeChallenge(sessionDto.getCodeChallenge());
    updateDto.setOperatorId(operatorId);
    updateDto.setAccessTokenId(sessionDto.getAccessTokenId());
    updateDto.setAccessTokenDateIns(sessionDto.getSessionTokenDateIns());
    updateDto.setAccessTokenDateFinish(sessionDto.getAccessTokenDateFinish());
    updateDto.setAccessTokenId(sessionDto.getRefreshTokenId());
    updateDto.setAccessTokenDateIns(sessionDto.getRefreshTokenDateIns());
    updateDto.setAccessTokenDateFinish(sessionDto.getRefreshTokenDateFinish());
    updateDto.setSessionTokenId(sessionTokenId);
    updateDto.setSessionTokenDateIns(sessionDateIns);
    updateDto.setSessionTokenDateFinish(sessionDateFinish);
    sessionService.update(String.valueOf(updateDto.getSessionId()), updateDto, serverCredential);
  }

  private Token generateSessionToken(String username, Integer operatorId, String issuer, String privateKey, Integer expiresIn) {
    try {
      /**
       * Generate uuid for token ID
       */
      String tokenId = UUID.randomUUID().toString().replaceAll("-", "");
      /**
       * Create token with JWT lib
       */
      Token token = new TokenImpl(tokenId, Collections.EMPTY_LIST, username + ":" + operatorId,
        issuer, addHours(new Date(), expiresIn), new Date());
      /**
       * Sign token with private key
       */
      Signer signer = new SignerRSA(privateKey);
      token = signer.sign(token);
      return token;
    } catch (Throwable th) {
      throw new OAuthRuntimeException(SERVER_ERROR, th);
    }
  }

  private Date addHours(Date date, int hours) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.HOUR_OF_DAY, hours);
    return calendar.getTime();
  }
}
