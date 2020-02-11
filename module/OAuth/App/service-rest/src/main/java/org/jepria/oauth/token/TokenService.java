package org.jepria.oauth.token;

import org.jepria.oauth.authentication.AuthenticationServerFactory;
import org.jepria.oauth.authorization.AuthorizationServerFactory;
import org.jepria.oauth.authorization.dto.AuthRequestCreateDto;
import org.jepria.oauth.authorization.dto.AuthRequestDto;
import org.jepria.oauth.authorization.dto.AuthRequestSearchDtoLocal;
import org.jepria.oauth.authorization.dto.AuthRequestUpdateDto;
import org.jepria.oauth.main.exception.HandledRuntimeException;
import org.jepria.oauth.sdk.GrantType;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.oauth.token.dto.TokenInfoDto;

import org.jepria.oauth.sdk.token.Signer;
import org.jepria.oauth.sdk.token.Token;
import org.jepria.oauth.sdk.token.Verifier;
import org.jepria.oauth.sdk.token.rsa.SignerRSA;
import org.jepria.oauth.sdk.token.TokenImpl;
import org.jepria.oauth.sdk.token.rsa.VerifierRSA;

import javax.security.auth.login.LoginException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.jepria.oauth.sdk.OAuthConstants.*;

public class TokenService {

  private AuthRequestDto getAuthRequest(String authCode,
                                        String clientId,
                                        String redirectUri,
                                        String tokenId) {
    AuthRequestSearchDtoLocal searchTemplate = new AuthRequestSearchDtoLocal();
    searchTemplate.setAuthorizationCode(authCode);
    searchTemplate.setClientId(clientId);
    searchTemplate.setRedirectUri(redirectUri);
    searchTemplate.setTokenId(tokenId);
    List<AuthRequestDto> result = (List<AuthRequestDto>) AuthorizationServerFactory.getInstance().getDao().find(searchTemplate, 1);
    if (result.size() == 1) {
      return result.get(0);
    } else {
      throw new NoSuchElementException("Authorization request not found");
    }
  }

  private void updateAuthRequest(Integer authRequestId,
                                 Integer operatorId,
                                 String accessTokenId,
                                 Date accessTokenDateIns,
                                 Date accessTokenDateFinish,
                                 String sessionTokenId,
                                 Date sessionTokenDateIns,
                                 Date sessionTokenDateFinish) {
    AuthRequestUpdateDto updateDto = new AuthRequestUpdateDto();
    updateDto.setAuthRequestId(authRequestId);
    updateDto.setOperatorId(operatorId);
    updateDto.setAccessTokenId(accessTokenId);
    updateDto.setAccessTokenDateIns(accessTokenDateIns);
    updateDto.setAccessTokenDateFinish(accessTokenDateFinish);
    updateDto.setSessionTokenId(sessionTokenId);
    updateDto.setSessionTokenDateIns(sessionTokenDateIns);
    updateDto.setSessionTokenDateFinish(sessionTokenDateFinish);
    AuthorizationServerFactory.getInstance().getService().update(updateDto);
  }

  /**
   * @param grantType
   * @param privateKey
   * @param host
   * @param authCode
   * @param clientId
   * @param redirectUri
   * @return
   */
  public TokenDto create(
    String grantType,
    String privateKey,
    String host,
    String authCode,
    String clientId,
    String redirectUri,
    String username,
    String password) {
    TokenDto result;
    if (grantType == null) {
      throw new HandledRuntimeException(UNSUPPORTED_GRANT_TYPE, "Grant type must be not null");
    }
    switch (grantType) {
      case GrantType.AUTHORIZATION_CODE: {
        result = createTokenForAuthCodeGrant(privateKey, host, authCode, clientId, new String(Base64.getUrlDecoder().decode(redirectUri)));
        break;
      }
//        case GrantType.CLIENT_CREDENTIALS: {TODO
//          break;
//        }
      case GrantType.PASSWORD: {
        result = createTokenForUserCredentialsGrant(privateKey, host, clientId, username, password);
        break;
      }
      default: {
        throw new HandledRuntimeException(UNSUPPORTED_GRANT_TYPE, "Grant type '" + grantType + "' is not supported");
      }
    }
    return result;
  }

  /**
   * @param privateKeyString
   * @param host
   * @param authCode
   * @param clientId
   * @param redirectUri
   * @return
   */
  public TokenDto createTokenForImplicitGrant(String privateKeyString,
                                              String host,
                                              String authCode,
                                              String clientId,
                                              String redirectUri) {
    if (authCode == null) {
      throw new IllegalArgumentException("Authorization code is null.");
    }
    if (clientId == null) {
      throw new IllegalArgumentException("Client ID is null.");
    }
    if (redirectUri == null) {
      throw new IllegalArgumentException("Redirect URI is null.");
    }

    AuthRequestDto authRequest = getAuthRequest(authCode, clientId, redirectUri, null);
    if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - authRequest.getDateIns().getTime()) > 10) {
      throw new HandledRuntimeException(INVALID_GRANT, "Authorization code active time has expired.");
    }
    if (authRequest.getAccessTokenId() != null) {
      throw new HandledRuntimeException(INVALID_GRANT, "Request is finished.");
    }
    Token token = generateToken(authRequest.getOperatorLogin(), null, authRequest.getOperator().getValue(), host, privateKeyString, 1);
    TokenDto tokenDto = new TokenDto();
    tokenDto.setAccessToken(token.asString());
    tokenDto.setExpiresIn(3600); // 1 hour active time
    tokenDto.setTokenType("Bearer");
    updateAuthRequest(authRequest.getAuthRequestId(),
      authRequest.getOperator().getValue(),
      token.getJti(),
      token.getIssueTime(),
      token.getExpirationTime(),
      authRequest.getSessionTokenId(),
      authRequest.getSessionTokenDateIns(),
      authRequest.getSessionTokenDateFinish());
    return tokenDto;
  }

  /**
   * @param privateKeyString
   * @param host
   * @param authCode
   * @param clientId
   * @param redirectUri
   * @return
   */
  private TokenDto createTokenForAuthCodeGrant(String privateKeyString,
                                               String host,
                                               String authCode,
                                               String clientId,
                                               String redirectUri) {
    if (authCode == null) {
      throw new IllegalArgumentException("Authorization code is null.");
    }
    if (clientId == null) {
      throw new IllegalArgumentException("Client ID is null.");
    }
    if (redirectUri == null) {
      throw new IllegalArgumentException("Redirect URI is null.");
    }
    AuthRequestDto authRequest = getAuthRequest(authCode, clientId, redirectUri, null);
    if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - authRequest.getDateIns().getTime()) > 10) {
      throw new HandledRuntimeException(INVALID_GRANT, "Authorization code active time has expired.");
    }
    if (authRequest.getAccessTokenId() != null) {
      throw new HandledRuntimeException(INVALID_GRANT, "Request is finished.");
    }
    Token token = generateToken(authRequest.getOperatorLogin(), null, authRequest.getOperator().getValue(), host, privateKeyString, 4);
    TokenDto tokenDto = new TokenDto();
    tokenDto.setAccessToken(token.asString());
    tokenDto.setExpiresIn(14400); // 4 hours active time
    tokenDto.setTokenType("Bearer");
    updateAuthRequest(authRequest.getAuthRequestId(),
      authRequest.getOperator().getValue(),
      token.getJti(),
      token.getIssueTime(),
      token.getExpirationTime(),
      authRequest.getSessionTokenId(),
      authRequest.getSessionTokenDateIns(),
      authRequest.getSessionTokenDateFinish());
    return tokenDto;
  }

  /**
   * @param privateKeyString
   * @param host
   * @param username
   * @param password
   * @return
   */
  private TokenDto createTokenForUserCredentialsGrant(String privateKeyString,
                                                      String host,
                                                      String clientId,
                                                      String username,
                                                      String password) {
    TokenDto result;
    try {
      Integer operatorId = AuthenticationServerFactory.getInstance().getService().loginByPassword(username, password);

      Token token = generateToken(username, Collections.singletonList(clientId), operatorId, host, privateKeyString, 1);
      TokenDto tokenDto = new TokenDto();
      tokenDto.setAccessToken(token.asString());
      tokenDto.setExpiresIn(3600); // 1 hours active time
      tokenDto.setTokenType("Bearer");

      AuthRequestCreateDto createDto = new AuthRequestCreateDto();
      createDto.setAuthorizationCode(AuthorizationServerFactory.getInstance().getService().generateCode());
      createDto.setClientId(clientId);
      createDto.setOperatorId(operatorId);
      createDto.setAccessTokenId(token.getJti());
      createDto.setAccessTokenDateIns(token.getIssueTime());
      createDto.setAccessTokenDateFinish(token.getExpirationTime());
      AuthorizationServerFactory.getInstance().getService().create(createDto);

      result = tokenDto;
    } catch (LoginException e) {
      throw new HandledRuntimeException(ACCESS_DENIED, e);
    }
    return result;
  }

  /**
   * @param privateKeyString
   * @param host
   * @param clientId
   * @param clientSecret
   * @return
   */
  private TokenDto createTokenForClientCredentialsGrant(String privateKeyString, String host, String clientId, String clientSecret) {
    throw new UnsupportedOperationException();
  }

  /**
   * @param publicKey
   * @param hostContext
   * @param tokenString
   * @return
   */
  public TokenInfoDto getTokenInfo(String publicKey, String hostContext, String tokenString) {
    TokenInfoDto result = new TokenInfoDto();
    Token token;
    try {
      token = TokenImpl.parseFromString(tokenString);
      AuthRequestDto authRequestDto = getAuthRequest(null, null, null, token.getJti());
      if (authRequestDto.getBlocked()) {
        result.setActive(false);
        return result;
      }
      Verifier verifier = new VerifierRSA(null, hostContext, new Date(), publicKey);
      if (!verifier.verify(token)) {
        result.setActive(false);
        return result;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      result.setActive(false);
      return result;
    }
    result.setActive(true);
    result.setJti(token.getJti());
    result.setAud(token.getAudience());
    result.setIss(token.getIssuer());
    result.setExp(token.getExpirationTime().getTime());
    result.setSub(token.getSubject());
    return result;
  }

  /**
   * @param clientId
   * @param tokenString
   * @return
   */
  public void revokeToken(String clientId, String tokenString) {
    try {
      Token token = TokenImpl.parseFromString(tokenString);
      AuthRequestDto authRequestDto = getAuthRequest(null, clientId, null, token.getJti());
      AuthorizationServerFactory.getInstance().getDao().blockAuthRequest(authRequestDto.getAuthRequestId());
    } catch (ParseException e) {
      throw new HandledRuntimeException(SERVER_ERROR, e);
    }
  }

  private Token generateToken(String username,
                              List<String> audience,
                              Integer userId,
                              String issuer,
                              String privateKeyString,
                              Integer expiresIn) {
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
       * TODO пересмотреть концепцию передачи данных пользователя
       */
      Token token = new TokenImpl(tokenId, audience != null ? audience : Collections.singletonList("RFInfo"), username + ":" + userId,
        issuer, addHours(new Date(), expiresIn), new Date());
      /**
       * Sign token with private key
       */
      Signer signer = new SignerRSA(privateKeyString);
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
