package org.jepria.oauth.authentication;

import org.jepria.oauth.authorization.AuthorizationServerFactory;
import org.jepria.oauth.authorization.dto.AuthRequestDto;
import org.jepria.oauth.authorization.dto.AuthRequestSearchDtoLocal;
import org.jepria.oauth.authorization.dto.AuthRequestUpdateDto;
import org.jepria.oauth.sdk.token.rsa.EncryptorRSA;
import org.jepria.oauth.sdk.token.rsa.SignerRSA;
import org.jepria.oauth.sdk.token.TokenImpl;
import org.jepria.oauth.sdk.token.Encryptor;
import org.jepria.oauth.sdk.token.Signer;
import org.jepria.oauth.sdk.token.Token;
import org.jepria.oauth.token.TokenServerFactory;
import org.jepria.oauth.token.dto.TokenDto;

import javax.security.auth.login.LoginException;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.jepria.oauth.authorization.AuthorizationFieldNames.AUTH_REQUEST_ID;

public class AuthenticationService {

  public static int DEFAULT_EXPIRE_TIME = 8;

  public Integer loginByPassword(String username, String password) throws LoginException {
    Integer operatorId = AuthenticationServerFactory.getInstance().getDao().loginByPassword(username, password);
    if (operatorId == null) {
      throw new LoginException();
    } else {
      return operatorId;
    }
  }

  private AuthRequestDto getAuthRequest(String authCode, String clientId, String redirectUri) {
    try {
      AuthRequestSearchDtoLocal searchTemplate = new AuthRequestSearchDtoLocal();
      searchTemplate.setClientId(clientId);
      searchTemplate.setAuthorizationCode(authCode);
      searchTemplate.setRedirectUri(redirectUri);
      AuthRequestDto authRequest = AuthorizationServerFactory.getInstance().getService().find(searchTemplate).get(0);
      return authRequest;
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  public String authenticate(
    String authCode,
    String redirectUri,
    String clientId,
    String username,
    String password,
    String host,
    String publicKey,
    String privateKey) throws LoginException {
      AuthRequestDto authRequest = getAuthRequest(authCode, clientId, redirectUri);
      if (authRequest == null || TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - authRequest.getDateIns().getTime()) > 10) {
        throw new IllegalStateException("Authorization code not found or has expired");
      }
      if (authRequest.getOperator() != null) {
        throw new IllegalStateException("Request has already passed authentication");
      }
      if (authRequest.getAccessTokenId() != null) {
        throw new IllegalStateException("Request is finished");
      }
      Integer operatorId = loginByPassword(username, password);
      Token sessionToken = generateSessionToken(username, operatorId, host, privateKey, null);
      updateAuthRequest(authRequest.getAuthRequestId(), operatorId, sessionToken.getJti(), sessionToken.getIssueTime(), sessionToken.getExpirationTime());
      /**
       * Encrypt token with public key
       */
      Encryptor encryptor = new EncryptorRSA(publicKey);
      try {
        sessionToken = encryptor.encrypt(sessionToken);
      } catch (ParseException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    return sessionToken.asString();
  }

  public TokenDto getToken(String privateKey, String host, String authCode, String clientId, String redirectUri) {
    return TokenServerFactory.getInstance().getService().createTokenForImplicitGrant(privateKey, host, authCode, clientId, redirectUri);
  }

  private void updateAuthRequest(Integer authRequestId, Integer operatorId, String sessionId, Date sessionDateIns, Date sessionDateFinish) {
    AuthRequestUpdateDto updateDto = new AuthRequestUpdateDto();
    updateDto.setOperatorId(operatorId);
    updateDto.setSessionTokenId(sessionId);
    updateDto.setSessionTokenDateIns(sessionDateIns);
    updateDto.setSessionTokenDateFinish(sessionDateFinish);
    AuthorizationServerFactory.getInstance().getDao().update(new HashMap<String, Integer>(){{
      put(AUTH_REQUEST_ID, authRequestId);
    }}, updateDto, 1);
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
       * TODO решить как задавать audience, возможно конкретное приложение, и пересоздавать токен
       * TODO пересмотреть концепцию передачи данных пользователя
       */
      Token token = new TokenImpl(tokenId, Collections.EMPTY_LIST,username + ":" + operatorId,
        issuer, addHours(new Date(), expiresIn == null ? DEFAULT_EXPIRE_TIME : expiresIn), new Date());
      /**
       * Sign token with private key
       */
      Signer signer = new SignerRSA(privateKey);
      token = signer.sign(token);
      return token;
    } catch (Throwable th) {
      th.printStackTrace();
      throw new RuntimeException(th);
    }
  }

  private Date addHours(Date date, int hours) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.HOUR_OF_DAY, hours);
    return calendar.getTime();
  }
}
