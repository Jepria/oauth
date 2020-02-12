package org.jepria.oauth.token;

import org.jepria.oauth.authentication.AuthenticationServerFactory;
import org.jepria.oauth.authorization.AuthorizationServerFactory;
import org.jepria.oauth.main.exception.HandledRuntimeException;
import org.jepria.oauth.sdk.GrantType;
import org.jepria.oauth.session.SessionServerFactory;
import org.jepria.oauth.session.SessionService;
import org.jepria.oauth.session.dto.SessionCreateDto;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDtoLocal;
import org.jepria.oauth.session.dto.SessionUpdateDto;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.oauth.token.dto.TokenInfoDto;

import org.jepria.oauth.sdk.token.Signer;
import org.jepria.oauth.sdk.token.Token;
import org.jepria.oauth.sdk.token.Verifier;
import org.jepria.oauth.sdk.token.rsa.SignerRSA;
import org.jepria.oauth.sdk.token.TokenImpl;
import org.jepria.oauth.sdk.token.rsa.VerifierRSA;
import org.jepria.server.service.security.Credential;

import javax.security.auth.login.LoginException;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.jepria.oauth.sdk.OAuthConstants.*;

public class TokenService {

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

  private SessionDto getSession(String authCode,
                                String clientId,
                                String redirectUri,
                                String tokenId,
                                Credential credential) {
    SessionSearchDtoLocal searchTemplate = new SessionSearchDtoLocal();
    searchTemplate.setAuthorizationCode(authCode);
    searchTemplate.setClientId(clientId);
    searchTemplate.setRedirectUri(redirectUri);
    searchTemplate.setAccessTokenId(tokenId);
    List<SessionDto> result = sessionService.find(searchTemplate, credential);
    if (result.size() == 1) {
      return result.get(0);
    } else {
      throw new HandledRuntimeException(INVALID_GRANT, "Authorization request not found");
    }
  }

  private void updateSession(Integer sessionId,
                                 Integer operatorId,
                                 String accessTokenId,
                                 Date accessTokenDateIns,
                                 Date accessTokenDateFinish,
                                 String sessionTokenId,
                                 Date sessionTokenDateIns,
                                 Date sessionTokenDateFinish,
                                 Credential credential) {
    SessionUpdateDto updateDto = new SessionUpdateDto();
    updateDto.setSessionId(sessionId);
    updateDto.setOperatorId(operatorId);
    updateDto.setAccessTokenId(accessTokenId);
    updateDto.setAccessTokenDateIns(accessTokenDateIns);
    updateDto.setAccessTokenDateFinish(accessTokenDateFinish);
    updateDto.setSessionTokenId(sessionTokenId);
    updateDto.setSessionTokenDateIns(sessionTokenDateIns);
    updateDto.setSessionTokenDateFinish(sessionTokenDateFinish);
    sessionService.update(updateDto, credential);
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
  public TokenDto create(String grantType,
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

    SessionDto session = getSession(authCode, clientId, redirectUri, null, serverCredential);
    if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - session.getDateIns().getTime()) > 10) {
      throw new HandledRuntimeException(INVALID_GRANT, "Authorization code active time has expired.");
    }
    if (session.getAccessTokenId() != null) {
      throw new HandledRuntimeException(INVALID_GRANT, "Request is finished.");
    }
    Token token = generateToken(session.getOperatorLogin(), null, session.getOperator().getValue(), host, privateKeyString, 1);
    TokenDto tokenDto = new TokenDto();
    tokenDto.setAccessToken(token.asString());
    tokenDto.setExpiresIn(3600); // 1 hour active time
    tokenDto.setTokenType("Bearer");
    updateSession(session.getSessionId(),
      session.getOperator().getValue(),
      token.getJti(),
      token.getIssueTime(),
      token.getExpirationTime(),
      session.getSessionTokenId(),
      session.getSessionTokenDateIns(),
      session.getSessionTokenDateFinish(),
      serverCredential);
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
    SessionDto session = getSession(authCode, clientId, redirectUri, null, serverCredential);
    if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - session.getDateIns().getTime()) > 10) {
      throw new HandledRuntimeException(INVALID_GRANT, "Authorization code active time has expired.");
    }
    if (session.getAccessTokenId() != null) {
      throw new HandledRuntimeException(INVALID_GRANT, "Request is finished.");
    }
    Token token = generateToken(session.getOperatorLogin(), null, session.getOperator().getValue(), host, privateKeyString, 4);
    TokenDto tokenDto = new TokenDto();
    tokenDto.setAccessToken(token.asString());
    tokenDto.setExpiresIn(14400); // 4 hours active time
    tokenDto.setTokenType("Bearer");
    updateSession(session.getSessionId(),
      session.getOperator().getValue(),
      token.getJti(),
      token.getIssueTime(),
      token.getExpirationTime(),
      session.getSessionTokenId(),
      session.getSessionTokenDateIns(),
      session.getSessionTokenDateFinish(),
      serverCredential);
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

      SessionCreateDto createDto = new SessionCreateDto();
      createDto.setAuthorizationCode(AuthorizationServerFactory.getInstance().getService().generateCode());
      createDto.setClientId(clientId);
      createDto.setOperatorId(operatorId);
      createDto.setAccessTokenId(token.getJti());
      createDto.setAccessTokenDateIns(token.getIssueTime());
      createDto.setAccessTokenDateFinish(token.getExpirationTime());
      sessionService.create(createDto, serverCredential);

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
  public TokenInfoDto getTokenInfo(String publicKey, String hostContext, String tokenString, Credential credential) {
    TokenInfoDto result = new TokenInfoDto();
    Token token;
    try {
      token = TokenImpl.parseFromString(tokenString);
      SessionDto sessionDto = getSession(null, null, null, token.getJti(), credential);
      if (sessionDto.getBlocked()) {
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
  public void deleteToken(String clientId, String tokenString, Credential credential) {
    try {
      Token token = TokenImpl.parseFromString(tokenString);
      SessionDto sessionDto = getSession(null, clientId, null, token.getJti(), credential);
      sessionService.delete(sessionDto.getSessionId(), credential);
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
