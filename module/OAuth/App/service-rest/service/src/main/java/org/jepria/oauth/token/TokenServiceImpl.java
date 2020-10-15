package org.jepria.oauth.token;

import org.jepria.oauth.client.ClientService;
import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.key.KeyService;
import org.jepria.oauth.key.dto.KeyDto;
import org.jepria.oauth.sdk.GrantType;
import org.jepria.oauth.sdk.ResponseType;
import org.jepria.oauth.sdk.token.Signer;
import org.jepria.oauth.sdk.token.Token;
import org.jepria.oauth.sdk.token.TokenImpl;
import org.jepria.oauth.sdk.token.Verifier;
import org.jepria.oauth.sdk.token.rsa.SignerRSA;
import org.jepria.oauth.sdk.token.rsa.VerifierRSA;
import org.jepria.oauth.session.SessionService;
import org.jepria.oauth.session.dto.SessionCreateDto;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDto;
import org.jepria.oauth.session.dto.SessionUpdateDto;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.oauth.token.dto.TokenInfoDto;
import org.jepria.server.service.security.Credential;

import java.net.URI;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.jepria.oauth.sdk.OAuthConstants.*;

public class TokenServiceImpl implements TokenService {

//  private static final int ACCESS_TOKEN_LIFETIME = 3600 * 8; //8 hours
//  private static final int REFRESH_TOKEN_LIFETIME = 3600 * 24 * 7; //1 week
  private static final String TOKEN_TYPE = "Bearer";
  private final SessionService sessionService;
  private final KeyService keyService;
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

  public TokenServiceImpl(SessionService sessionService, KeyService keyService, ClientService clientService) {
    this.sessionService = sessionService;
    this.keyService = keyService;
    this.clientService = clientService;
  }

  private SessionDto getSession(String authCode,
                                String clientId,
                                String redirectUri,
                                String accessTokenId,
                                String refreshTokenId,
                                Credential credential) {
    SessionSearchDto searchTemplate = new SessionSearchDto();
    searchTemplate.setAuthorizationCode(authCode);
    searchTemplate.setClientId(clientId);
    searchTemplate.setRedirectUri(redirectUri);
    searchTemplate.setAccessTokenId(accessTokenId);
    searchTemplate.setRefreshTokenId(refreshTokenId);
    List<SessionDto> result = sessionService.find(searchTemplate, credential);
    if (result.size() == 1) {
      return result.get(0);
    } else {
      return null;
    }
  }

  private void updateSession(SessionDto session,
                             String accessTokenId,
                             Date accessTokenDateIns,
                             Date accessTokenDateFinish,
                             Credential credential) {
    SessionUpdateDto updateDto = new SessionUpdateDto();
    updateDto.setSessionId(session.getSessionId());
    updateDto.setAuthorizationCode(session.getAuthorizationCode());
    updateDto.setClientId(session.getClient().getValue());
    updateDto.setRedirectUri(session.getRedirectUri());
    updateDto.setCodeChallenge(session.getCodeChallenge());
    updateDto.setOperatorId(session.getOperator().getValue());
    updateDto.setAccessTokenId(accessTokenId);
    updateDto.setAccessTokenDateIns(accessTokenDateIns);
    updateDto.setAccessTokenDateFinish(accessTokenDateFinish);
    updateDto.setSessionTokenId(session.getSessionTokenId());
    updateDto.setSessionTokenDateIns(session.getSessionTokenDateIns());
    updateDto.setSessionTokenDateFinish(session.getSessionTokenDateFinish());
    sessionService.update(String.valueOf(updateDto.getSessionId()), updateDto, credential);
  }

  @Override
  public TokenDto create(String responseType,
                         String issuer,
                         String authCode,
                         String clientId,
                         URI redirectUri,
                         Integer accessTokenLifeTime) {
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
          serverCredential);
      if (session == null) throw new OAuthRuntimeException(INVALID_GRANT, "Authorization request not found");
      if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - session.getDateIns().getTime()) > 10) {
        throw new OAuthRuntimeException(INVALID_GRANT, "Authorization code active time has expired.");
      }
      if (session.getAccessTokenId() != null) {
        throw new OAuthRuntimeException(INVALID_GRANT, "Request is finished.");
      }
      Token accessToken = generateToken(session.getOperatorLogin(), null, session.getOperator().getValue(), issuer, keyDto.getPrivateKey(), accessTokenLifeTime);
      TokenDto tokenDto = new TokenDto();
      tokenDto.setAccessToken(accessToken.asString());
      tokenDto.setExpiresIn(new Long(accessTokenLifeTime * 3600));
      tokenDto.setTokenType(TOKEN_TYPE);
      updateSession(session,
          accessToken.getJti(),
          accessToken.getIssueTime(),
          accessToken.getExpirationTime(),
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
                         URI redirectUri,
                         Integer accessTokenLifeTime) {
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
        serverCredential);
    if (session == null) throw new OAuthRuntimeException(INVALID_GRANT, "Authorization request not found");
    if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - session.getDateIns().getTime()) > 10) {
      throw new OAuthRuntimeException(INVALID_GRANT, "Authorization code active time has expired.");
    }
    if (session.getAccessTokenId() != null) {
      throw new OAuthRuntimeException(INVALID_GRANT, "Request is finished.");
    }
    Token accessToken = generateToken(session.getOperatorLogin(), null, session.getOperator().getValue(), issuer, keyDto.getPrivateKey(), accessTokenLifeTime);
    TokenDto tokenDto = new TokenDto();
    tokenDto.setAccessToken(accessToken.asString());
    tokenDto.setExpiresIn(new Long(accessTokenLifeTime * 3600));
    tokenDto.setTokenType(TOKEN_TYPE);
    updateSession(session,
      accessToken.getJti(),
      accessToken.getIssueTime(),
      accessToken.getExpirationTime(),
      serverCredential);
    return tokenDto;
  }

  private void checkClientGrantTypes(String clientId, String grantType) {
    List<String> clientGrantTypes = clientService.getClientGrantTypes(clientId);
    if (clientGrantTypes.size() == 0 || !clientGrantTypes.stream().anyMatch(clientGrantType -> clientGrantType.equals(grantType))) {
      throw new OAuthRuntimeException(UNAUTHORIZED_CLIENT, "Client doesn't have enough permissions to use responseType=" + grantType);
    }
  }

  @Override
  public TokenDto create(String clientId,
                         String username,
                         Integer userId,
                         String issuer,
                         Integer accessTokenLifeTime,
                         Integer refreshTokenLifeTime) {
    checkClientGrantTypes(clientId, GrantType.PASSWORD);
    KeyDto keyDto = keyService.getKeys(null, serverCredential);
    return createTokenPair(keyDto.getPrivateKey(), issuer, clientId, username, userId, accessTokenLifeTime, refreshTokenLifeTime);
  }
  
  @Override
  public TokenDto create(String clientId,
                         String refreshTokenString,
                         String issuer,
                         Integer accessTokenLifeTime,
                         Integer refreshTokenLifeTime) {
    checkClientGrantTypes(clientId, GrantType.REFRESH_TOKEN);
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
            serverCredential);
        if (sessionDto == null) throw new OAuthRuntimeException(INVALID_GRANT, "Authorization request not found");
        String[] subject = refreshToken.getSubject().split(":");
        sessionService.deleteRecord(String.valueOf(sessionDto.getSessionId()), serverCredential);
        return createTokenPair(
            keyDto.getPrivateKey(),
            issuer,
            refreshToken.getAudience().isEmpty() ? null : refreshToken.getAudience().get(0),
            subject[0],
            Integer.valueOf(subject[1]),
            accessTokenLifeTime,
            refreshTokenLifeTime);
      } else {
        throw new OAuthRuntimeException(INVALID_GRANT, "Token verification failed");
      }
    } catch (ParseException | IllegalArgumentException e) {
      throw new OAuthRuntimeException(INVALID_GRANT, e);
    }
  }
  
  @Override
  public TokenDto create(String clientId,
                         Integer clientOperatorId,
                         String issuer,
                         Integer accessTokenLifeTime,
                         Integer refreshTokenLifeTime) {
    checkClientGrantTypes(clientId, GrantType.CLIENT_CREDENTIALS);
    KeyDto keyDto = keyService.getKeys(null, serverCredential);
    return createTokenPair(keyDto.getPrivateKey(), issuer, clientId, clientId, clientOperatorId, accessTokenLifeTime, refreshTokenLifeTime);
  }
  
  private TokenDto createTokenPair(String privateKeyString,
                                   String issuer,
                                   String clientId,
                                   String username,
                                   Integer userId,
                                   Integer accessTokenLifeHours,
                                   Integer refreshTokenLifeHours) {
    Token accessToken = generateToken(username, Collections.singletonList(clientId), userId, issuer, privateKeyString, accessTokenLifeHours);
    Token refreshToken = generateToken(username, Collections.singletonList(clientId), userId, issuer, privateKeyString, refreshTokenLifeHours);
    TokenDto tokenDto = new TokenDto();
    tokenDto.setAccessToken(accessToken.asString());
    tokenDto.setExpiresIn(new Long(accessTokenLifeHours * 3600));
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
          serverCredential);
      if (sessionDto == null) {
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
          serverCredential);
      if (sessionDto != null) {
        sessionService.deleteRecord(String.valueOf(sessionDto.getSessionId()), serverCredential);
      }
    } catch (ParseException e) {
      throw new OAuthRuntimeException(SERVER_ERROR, e);
    } catch (IllegalArgumentException ex) {
      System.out.println("Failed token value: " + tokenString);
      throw new OAuthRuntimeException(SERVER_ERROR, ex);
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
      String tokenId = UUID.randomUUID().toString().replaceAll("-", "");
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
      return UUID.randomUUID().toString().replaceAll("-", "");
    } catch (Throwable th) {
      throw new OAuthRuntimeException(SERVER_ERROR, th);
    }
  }
}
