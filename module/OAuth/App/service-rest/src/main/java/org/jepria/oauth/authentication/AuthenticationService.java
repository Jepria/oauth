package org.jepria.oauth.authentication;

import org.jepria.oauth.authentication.dao.AuthenticationDao;
import org.jepria.oauth.authorization.AuthorizationServerFactory;
import org.jepria.oauth.main.exception.HandledRuntimeException;
import org.jepria.oauth.sdk.token.rsa.EncryptorRSA;
import org.jepria.oauth.sdk.token.rsa.SignerRSA;
import org.jepria.oauth.sdk.token.TokenImpl;
import org.jepria.oauth.sdk.token.Encryptor;
import org.jepria.oauth.sdk.token.Signer;
import org.jepria.oauth.sdk.token.Token;
import org.jepria.oauth.session.SessionServerFactory;
import org.jepria.oauth.session.SessionService;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDtoLocal;
import org.jepria.oauth.session.dto.SessionUpdateDto;
import org.jepria.oauth.token.TokenServerFactory;
import org.jepria.oauth.token.TokenService;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.server.service.security.Credential;

import javax.security.auth.login.LoginException;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.jepria.oauth.sdk.OAuthConstants.*;

public class AuthenticationService {

  public static int DEFAULT_EXPIRE_TIME = 8;

  private TokenService tokenService = TokenServerFactory.getInstance().getService();
  private AuthenticationDao dao = AuthenticationServerFactory.getInstance().getDao();
  private SessionService sessionService = SessionServerFactory.getInstance().getService();

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

  /**
   * @param username
   * @param password
   * @return
   * @throws LoginException
   */
  public Integer loginByPassword(String username, String password) throws LoginException {
    Integer operatorId = dao.loginByPassword(username, password);
    if (operatorId == null) {
      throw new LoginException();
    } else {
      return operatorId;
    }
  }

  /**
   * @param clientId
   * @param clientSecret
   * @return
   * @throws LoginException
   */
  public Integer loginByClientCredentials(String clientId, String clientSecret) throws LoginException {
    Integer clientID = dao.loginByClientCredentials(clientId, clientSecret);
    if (clientID == null) {
      throw new LoginException();
    } else {
      return clientID;
    }
  }

  /**
   * @param clientId
   * @return
   * @throws LoginException
   */
  public Integer loginByClientId(String clientId) throws LoginException {
    Integer clientID = dao.loginByClientCredentials(clientId, null);
    if (clientID == null) {
      throw new LoginException();
    } else {
      return clientID;
    }
  }

  /**
   * @param authCode
   * @param redirectUri
   * @param clientId
   * @param username
   * @param password
   * @param host
   * @param publicKey
   * @param privateKey
   * @return
   */
  public String authenticate(
    String authCode,
    String redirectUri,
    String clientId,
    String username,
    String password,
    String host,
    String publicKey,
    String privateKey) {
    SessionDto session = getSession(authCode, clientId, redirectUri);
    if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - session.getDateIns().getTime()) > 10) {
      throw new HandledRuntimeException(ACCESS_DENIED, "Authorization code not found or has expired");
    }
    if (session.getOperator() != null) {
      throw new HandledRuntimeException(ACCESS_DENIED, "Request has already passed authentication");
    }
    if (session.getAccessTokenId() != null) {
      throw new HandledRuntimeException(ACCESS_DENIED, "Request is finished");
    }
    try {
      Integer operatorId = loginByPassword(username, password);
      Token sessionToken = generateSessionToken(username, operatorId, host, privateKey, null);
      updateSession(session.getSessionId(),
        operatorId,
        sessionToken.getJti(),
        sessionToken.getIssueTime(),
        sessionToken.getExpirationTime(),
        serverCredential);

      /**
       * Encrypt token with public key
       */
      Encryptor encryptor = new EncryptorRSA(publicKey);
      try {
        sessionToken = encryptor.encrypt(sessionToken);
      } catch (ParseException e) {
        throw new HandledRuntimeException(SERVER_ERROR, e);
      }
      return sessionToken.asString();
    } catch (LoginException e) {
      throw new HandledRuntimeException(ACCESS_DENIED, e);
    }
  }

  /**
   * @param privateKey
   * @param host
   * @param authCode
   * @param clientId
   * @param redirectUri
   * @return
   */
  public TokenDto getToken(String privateKey, String host, String authCode, String clientId, String redirectUri) {
    return tokenService.createTokenForImplicitGrant(privateKey, host, authCode, clientId, redirectUri);
  }

  private SessionDto getSession(String authCode, String clientId, String redirectUri) {
    SessionSearchDtoLocal searchTemplate = new SessionSearchDtoLocal();
    searchTemplate.setClientId(clientId);
    searchTemplate.setAuthorizationCode(authCode);
    searchTemplate.setRedirectUri(redirectUri);
    List<SessionDto> sessions = sessionService.find(searchTemplate, serverCredential);
    if (sessions.size() == 1) {
      return sessions.get(0);
    } else {
      throw new HandledRuntimeException(ACCESS_DENIED, "Session not found");
    }
  }

  private void updateSession(Integer sessionId,
                             Integer operatorId,
                             String sessionTokenId,
                             Date sessionDateIns,
                             Date sessionDateFinish,
                             Credential serverCredential) {
    SessionUpdateDto updateDto = new SessionUpdateDto();
    updateDto.setSessionId(sessionId);
    updateDto.setOperatorId(operatorId);
    updateDto.setSessionTokenId(sessionTokenId);
    updateDto.setSessionTokenDateIns(sessionDateIns);
    updateDto.setSessionTokenDateFinish(sessionDateFinish);
    sessionService.update(updateDto, serverCredential);
  }

  private Token generateSessionToken(String username, Integer operatorId, String issuer, String privateKey, Integer expiresIn) {
    try {
      /**
       * Generate uuid for token ID
       */
      MessageDigest cryptoProvider = MessageDigest.getInstance("SHA-256");
      UUID randomUuid = UUID.randomUUID();
      cryptoProvider.update(randomUuid.toString().getBytes());
      String tokenId = Base64.getUrlEncoder().encodeToString(cryptoProvider.digest());
      /**
       * Create token with JWT lib
       */
      Token token = new TokenImpl(tokenId, Collections.EMPTY_LIST, username + ":" + operatorId,
        issuer, addHours(new Date(), expiresIn == null ? DEFAULT_EXPIRE_TIME : expiresIn), new Date());
      /**
       * Sign token with private key
       */
      Signer signer = new SignerRSA(privateKey);
      token = signer.sign(token);
      return token;
    } catch (Throwable th) {
      throw new HandledRuntimeException(SERVER_ERROR, th);
    }
  }

  private Date addHours(Date date, int hours) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.HOUR_OF_DAY, hours);
    return calendar.getTime();
  }
}
