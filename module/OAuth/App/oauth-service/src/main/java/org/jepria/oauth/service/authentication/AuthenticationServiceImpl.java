package org.jepria.oauth.service.authentication;

import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.model.authentication.AuthenticationService;
import org.jepria.oauth.model.authentication.dao.AuthenticationDao;
import org.jepria.oauth.model.clienturi.ClientUriService;
import org.jepria.oauth.model.clienturi.dto.ClientUriDto;
import org.jepria.oauth.model.clienturi.dto.ClientUriSearchDto;
import org.jepria.oauth.model.session.SessionService;
import org.jepria.oauth.model.session.dto.SessionDto;
import org.jepria.oauth.model.session.dto.SessionSearchDto;
import org.jepria.oauth.model.session.dto.SessionUpdateDto;
import org.jepria.oauth.sdk.token.*;
import org.jepria.oauth.sdk.token.rsa.DecryptorRSA;
import org.jepria.oauth.sdk.token.rsa.EncryptorRSA;
import org.jepria.oauth.sdk.token.rsa.SignatureVerifierRSA;
import org.jepria.oauth.sdk.token.rsa.SignerRSA;
import org.jepria.server.service.security.Credential;

import java.security.MessageDigest;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.jepria.oauth.sdk.OAuthConstants.*;

public class AuthenticationServiceImpl implements AuthenticationService {

  public static int DEFAULT_EXPIRE_TIME = 8;
  private final AuthenticationDao dao;
  private final SessionService sessionService;
  private final ClientUriService clientUriService;

  public AuthenticationServiceImpl(AuthenticationDao dao, SessionService sessionService, ClientUriService clientUriService) {
    this.dao = dao;
    this.sessionService = sessionService;
    this.clientUriService = clientUriService;
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

  public Integer loginByPassword(String username, String password) {
    try {
      Integer operatorId = dao.loginByPassword(username, password);
      if (operatorId == null) {
        throw new OAuthRuntimeException(ACCESS_DENIED, "Wrong username/password.");
      } else {
        return operatorId;
      }
    } catch (Throwable th) {
      throw new OAuthRuntimeException(SERVER_ERROR, th);
    }
  }

  @Override
  public Integer loginByClientSecret(String clientId, String clientSecret) {
    try {
      Integer clientID = dao.loginByClientSecret(clientId, clientSecret);
      if (clientID == null) {
        throw new OAuthRuntimeException(ACCESS_DENIED, "Wrong clientId/clientSecret");
      } else {
        return clientID;
      }
    } catch (Throwable th) {
      throw new OAuthRuntimeException(SERVER_ERROR, th);
    }
  }

  @Override
  public Integer loginByClientId(String clientId) {
    try {
      Integer clientID = dao.loginByClientSecret(clientId, null);
      if (clientID == null) {
        throw new OAuthRuntimeException(ACCESS_DENIED, "Wrong clientId");
      } else {
        return clientID;
      }
    } catch (Throwable th) {
      throw new OAuthRuntimeException(SERVER_ERROR, th);
    }
  }

  @Override
  public Integer loginByAuthorizationCode(String authorizationCode, String clientId, String codeVerifier) {
    Integer clientID = loginByClientId(clientId);
    try {
      if (dao.verifyPKCE(authorizationCode, codeVerifier)) {
        return clientID;
      } else {
        throw new OAuthRuntimeException(ACCESS_DENIED, "PKCE verification failed");
      }
    } catch (Throwable th) {
      throw new OAuthRuntimeException(SERVER_ERROR, th);
    }
  }

  @Override
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
      throw new OAuthRuntimeException(ACCESS_DENIED, "Authorization code not found or has expired");
    }
    if (session.getOperator() != null) {
      throw new OAuthRuntimeException(ACCESS_DENIED, "Request has already passed authentication");
    }
    if (session.getAccessTokenId() != null) {
      throw new OAuthRuntimeException(ACCESS_DENIED, "Request is finished");
    }
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
      throw new OAuthRuntimeException(SERVER_ERROR, e);
    }
    return sessionToken.asString();
  }

  @Override
  public void logout(String clientId,
                     String redirectUri,
                     String sessionToken,
                     String issuer,
                     String publicKey,
                     String privateKey) {
    ClientUriSearchDto clientUriSearchTemplate = new ClientUriSearchDto();
    clientUriSearchTemplate.setClientId(clientId);
    List<ClientUriDto> clientUriList = clientUriService.findClientUri(clientUriSearchTemplate, 1);
    if (!clientUriList.stream().anyMatch(clientUriDto -> clientUriDto.getClientUri().equals(redirectUri))) {
      throw new OAuthRuntimeException(INVALID_REQUEST, "redirect_uri mismatch");
    }

    try {
      Token token = TokenImpl.parseFromString(sessionToken);
      Decryptor decryptor = new DecryptorRSA(privateKey);
      token = decryptor.decrypt(token);
      Verifier verifier = new SignatureVerifierRSA(publicKey);
      if (verifier.verify(token) && issuer.equals(token.getIssuer())) {
        SessionSearchDto searchTemplate = new SessionSearchDto();
        searchTemplate.setSessionTokenId(token.getJti());
        searchTemplate.setBlocked(false);
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
    sessionService.update(String.valueOf(updateDto.getSessionId()), updateDto, serverCredential);
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
