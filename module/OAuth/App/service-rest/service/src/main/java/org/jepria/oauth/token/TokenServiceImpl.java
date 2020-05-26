package org.jepria.oauth.token;

import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.key.KeyService;
import org.jepria.oauth.key.dto.KeyDto;
import org.jepria.oauth.session.SessionService;
import org.jepria.oauth.session.dto.SessionCreateDto;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDto;
import org.jepria.oauth.session.dto.SessionUpdateDto;
import org.jepria.oauth.token.TokenService;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.oauth.token.dto.TokenInfoDto;
import org.jepria.oauth.sdk.ResponseType;
import org.jepria.oauth.sdk.token.Signer;
import org.jepria.oauth.sdk.token.Token;
import org.jepria.oauth.sdk.token.TokenImpl;
import org.jepria.oauth.sdk.token.Verifier;
import org.jepria.oauth.sdk.token.rsa.SignerRSA;
import org.jepria.oauth.sdk.token.rsa.VerifierRSA;
import org.jepria.server.service.security.Credential;

import java.net.URI;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.jepria.oauth.sdk.OAuthConstants.*;

public class TokenServiceImpl implements TokenService {

  private static final int ACCESS_TOKEN_LIFETIME = 3600; //1 hour
  private static final int REFRESH_TOKEN_LIFETIME = 3600 * 24 * 7; //1 week
  private static final String TOKEN_TYPE = "Bearer";
  private final SessionService sessionService;
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

  public TokenServiceImpl(SessionService sessionService, KeyService keyService) {
    this.sessionService = sessionService;
    this.keyService = keyService;
  }

  private SessionDto getSession(String authCode,
                                String clientId,
                                String redirectUri,
                                String accessTokenId,
                                String refreshTokenId,
                                Boolean hasToken,
                                Credential credential) {
    SessionSearchDto searchTemplate = new SessionSearchDto();
    searchTemplate.setAuthorizationCode(authCode);
    searchTemplate.setClientId(clientId);
    searchTemplate.setRedirectUri(redirectUri);
    searchTemplate.setAccessTokenId(accessTokenId);
    searchTemplate.setRefreshTokenId(refreshTokenId);
    searchTemplate.setHasToken(hasToken);
    List<SessionDto> result = sessionService.find(searchTemplate, credential);
    if (result.size() == 1) {
      return result.get(0);
    } else {
      throw new OAuthRuntimeException(INVALID_GRANT, "Authorization request not found");
    }
  }

  private void updateSession(Integer sessionId,
                             Integer userId,
                             String accessTokenId,
                             Date accessTokenDateIns,
                             Date accessTokenDateFinish,
                             String sessionTokenId,
                             Date sessionTokenDateIns,
                             Date sessionTokenDateFinish,
                             Credential credential) {
    SessionUpdateDto updateDto = new SessionUpdateDto();
    updateDto.setSessionId(sessionId);
    updateDto.setOperatorId(userId);
    updateDto.setAccessTokenId(accessTokenId);
    updateDto.setAccessTokenDateIns(accessTokenDateIns);
    updateDto.setAccessTokenDateFinish(accessTokenDateFinish);
    updateDto.setSessionTokenId(sessionTokenId);
    updateDto.setSessionTokenDateIns(sessionTokenDateIns);
    updateDto.setSessionTokenDateFinish(sessionTokenDateFinish);
    sessionService.update(String.valueOf(updateDto.getSessionId()), updateDto, credential);
  }

  @Override
  public TokenDto create(String responseType,
                         String issuer,
                         String authCode,
                         String clientId,
                         URI redirectUri) {
    if (ResponseType.TOKEN.equals(responseType)) {
      KeyDto keyDto = keyService.getKeys(null, serverCredential);
      if (authCode == null) {
        throw new OAuthRuntimeException(INVALID_REQUEST, "Authorization code is null.");
      }
      if (clientId == null) {
        throw new OAuthRuntimeException(INVALID_REQUEST, "Client ID is null.");
      }
      if (redirectUri == null) {
        throw new OAuthRuntimeException(INVALID_REQUEST, "Redirect URI is null.");
      }
      SessionDto session = getSession(authCode,
          clientId,
          redirectUri.toString(),
          null,
          null,
          false,
          serverCredential);
      if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - session.getDateIns().getTime()) > 10) {
        throw new OAuthRuntimeException(INVALID_GRANT, "Authorization code active time has expired.");
      }
      if (session.getAccessTokenId() != null) {
        throw new OAuthRuntimeException(INVALID_GRANT, "Request is finished.");
      }
      Token accessToken = generateToken(session.getOperatorLogin(), null, session.getOperator().getValue(), issuer, keyDto.getPrivateKey(), 4);
      TokenDto tokenDto = new TokenDto();
      tokenDto.setAccessToken(accessToken.asString());
      tokenDto.setExpiresIn(ACCESS_TOKEN_LIFETIME);
      tokenDto.setTokenType(TOKEN_TYPE);
      updateSession(session.getSessionId(),
          session.getOperator().getValue(),
          accessToken.getJti(),
          accessToken.getIssueTime(),
          accessToken.getExpirationTime(),
          session.getSessionTokenId(),
          session.getSessionTokenDateIns(),
          session.getSessionTokenDateFinish(),
          serverCredential);
      return tokenDto;
    } else {
      throw new OAuthRuntimeException(UNSUPPORTED_RESPONSE_TYPE);
    }
  }
  
  @Override
  public TokenDto create(String clientId,
                         String authCode,
                         String issuer,
                         URI redirectUri) {
    KeyDto keyDto = keyService.getKeys(null, serverCredential);
    if (authCode == null) {
      throw new OAuthRuntimeException(INVALID_REQUEST, "Authorization code is null.");
    }
    if (clientId == null) {
      throw new OAuthRuntimeException(INVALID_REQUEST, "Client ID is null.");
    }
    if (redirectUri == null) {
      throw new OAuthRuntimeException(INVALID_REQUEST, "Redirect URI is null.");
    }
    SessionDto session = getSession(authCode,
        clientId,
        redirectUri.toString(),
        null,
        null,
        false,
        serverCredential);
    if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - session.getDateIns().getTime()) > 10) {
      throw new OAuthRuntimeException(INVALID_GRANT, "Authorization code active time has expired.");
    }
    if (session.getAccessTokenId() != null) {
      throw new OAuthRuntimeException(INVALID_GRANT, "Request is finished.");
    }
    Token accessToken = generateToken(session.getOperatorLogin(), null, session.getOperator().getValue(), issuer, keyDto.getPrivateKey(), 4);
    TokenDto tokenDto = new TokenDto();
    tokenDto.setAccessToken(accessToken.asString());
    tokenDto.setExpiresIn(ACCESS_TOKEN_LIFETIME);
    tokenDto.setTokenType(TOKEN_TYPE);
    updateSession(session.getSessionId(),
        session.getOperator().getValue(),
        accessToken.getJti(),
        accessToken.getIssueTime(),
        accessToken.getExpirationTime(),
        session.getSessionTokenId(),
        session.getSessionTokenDateIns(),
        session.getSessionTokenDateFinish(),
        serverCredential);
    return tokenDto;
  }
  
  @Override
  public TokenDto create(String clientId,
                         String username,
                         Integer userId,
                         String issuer) {
    KeyDto keyDto = keyService.getKeys(null, serverCredential);
    return createTokenPair(keyDto.getPrivateKey(), issuer, clientId, username, userId);
  }
  
  @Override
  public TokenDto create(String clientId,
                         String refreshTokenString,
                         String issuer) {
    KeyDto keyDto = keyService.getKeys(null, serverCredential);
    try {
      Token refreshToken = TokenImpl.parseFromString(refreshTokenString);
      Verifier verifier = new VerifierRSA(clientId != null ? Collections.singletonList(clientId) : null, issuer, new Date(), keyDto.getPublicKey());
      if (verifier.verify(refreshToken)) {
        SessionDto sessionDto = getSession(null,
            clientId,
            null,
            null,
            refreshToken.getJti(),
            true,
            serverCredential);
        if (sessionDto.getBlocked()) {
          throw new OAuthRuntimeException(INVALID_GRANT, "Token is blocked");
        }
        String[] subject = refreshToken.getSubject().split(":");
        sessionService.deleteRecord(String.valueOf(sessionDto.getSessionId()), serverCredential);
        return createTokenPair(keyDto.getPrivateKey(), issuer, refreshToken.getAudience().isEmpty() ? null : refreshToken.getAudience().get(0), subject[0], Integer.valueOf(subject[1]));
      } else {
        throw new OAuthRuntimeException(INVALID_GRANT, "Token verification failed");
      }
    } catch (ParseException | IllegalArgumentException e) {
      throw new OAuthRuntimeException(INVALID_GRANT, e);
    }
  }
  
  @Override
  public TokenDto create(String clientId,
                         Integer userId,
                         String issuer) {
    KeyDto keyDto = keyService.getKeys(null, serverCredential);
    return createTokenPair(keyDto.getPrivateKey(), issuer, clientId, clientId, userId);
  }
  
  private TokenDto createTokenPair(String privateKeyString,
                                   String issuer,
                                   String clientId,
                                   String username,
                                   Integer userId) {
    Token accessToken = generateToken(username, Collections.singletonList(clientId), userId, issuer, privateKeyString, 1);
    Token refreshToken = generateToken(username, Collections.singletonList(clientId), userId, issuer, privateKeyString, 24 * 7);
    TokenDto tokenDto = new TokenDto();
    tokenDto.setAccessToken(accessToken.asString());
    tokenDto.setExpiresIn(ACCESS_TOKEN_LIFETIME); // 1 hours active time
    tokenDto.setTokenType("Bearer");
    tokenDto.setRefreshToken(refreshToken.asString());

    SessionCreateDto createDto = new SessionCreateDto();
    createDto.setAuthorizationCode(generateCode());
    createDto.setClientId(clientId);
    createDto.setOperatorId(userId);
    createDto.setAccessTokenId(accessToken.getJti());
    createDto.setAccessTokenDateIns(accessToken.getIssueTime());
    createDto.setAccessTokenDateFinish(accessToken.getExpirationTime());
    createDto.setRefreshTokenId(refreshToken.getJti());
    createDto.setRefreshTokenDateIns(refreshToken.getIssueTime());
    createDto.setRefreshTokenDateFinish(refreshToken.getExpirationTime());
    sessionService.create(createDto, serverCredential);
    return tokenDto;
  }

  @Override
  public TokenInfoDto getTokenInfo(String issuerContext, String tokenString) {
    TokenInfoDto result = new TokenInfoDto();
    Token token;
    try {
      KeyDto keyDto = keyService.getKeys(null, serverCredential);
      token = TokenImpl.parseFromString(tokenString);
      SessionDto sessionDto = getSession(null,
          null,
          null,
          token.getJti(),
          null,
          true,
          serverCredential);
      if (sessionDto.getBlocked()) {
        result.setActive(false);
        return result;
      }
      Verifier verifier = new VerifierRSA(null, issuerContext, new Date(), keyDto.getPublicKey());
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

  @Override
  public void delete(String clientId, String tokenString) {
    try {
      Token token = TokenImpl.parseFromString(tokenString);
      SessionDto sessionDto = getSession(null,
          clientId,
          null,
          token.getJti(),
          null,
          true,
          serverCredential);
      sessionService.deleteRecord(String.valueOf(sessionDto.getSessionId()), serverCredential);
    } catch (ParseException e) {
      throw new OAuthRuntimeException(SERVER_ERROR, e);
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
      throw new OAuthRuntimeException(SERVER_ERROR, th);
    }
  }

  private Date addHours(Date date, int hours) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.HOUR_OF_DAY, hours);
    return calendar.getTime();
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