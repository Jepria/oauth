package org.jepria.oauth.token;

import org.jepria.oauth.client.ClientService;
import org.jepria.oauth.client.dto.ClientDto;
import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.key.KeyService;
import org.jepria.oauth.key.dto.KeyDto;
import org.jepria.oauth.main.Utils;
import org.jepria.oauth.sdk.GrantType;
import org.jepria.oauth.sdk.ResponseType;
import org.jepria.oauth.sdk.token.Token;
import org.jepria.oauth.sdk.token.TokenImpl;
import org.jepria.oauth.sdk.token.Verifier;
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
  
  private static final String TOKEN_TYPE = "Bearer";
  private final SessionService sessionService;
  private final KeyService keyService;
  private final ClientService clientService;
  
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
                             String refreshTokenId,
                             Date refreshTokenDateIns,
                             Date refreshTokenDateFinish,
                             Credential credential) {
    SessionUpdateDto updateDto = new SessionUpdateDto();
    updateDto.setSessionId(session.getSessionId());
    updateDto.setAuthorizationCode(session.getAuthorizationCode());
    updateDto.setClientId(session.getClientId());
    updateDto.setRedirectUri(session.getRedirectUri());
    updateDto.setCodeChallenge(session.getCodeChallenge());
    updateDto.setOperatorId(session.getOperator().getValue());
    updateDto.setAccessTokenId(accessTokenId);
    updateDto.setAccessTokenDateIns(accessTokenDateIns);
    updateDto.setAccessTokenDateFinish(accessTokenDateFinish);
    updateDto.setRefreshTokenId(refreshTokenId);
    updateDto.setRefreshTokenDateIns(refreshTokenDateIns);
    updateDto.setRefreshTokenDateFinish(refreshTokenDateFinish);
    updateDto.setSessionTokenId(session.getSessionTokenId());
    updateDto.setSessionTokenDateIns(session.getSessionTokenDateIns());
    updateDto.setSessionTokenDateFinish(session.getSessionTokenDateFinish());
    sessionService.update(String.valueOf(updateDto.getSessionId()), updateDto, credential);
  }
  
  @Override
  public TokenDto create(String clientId,
                         String authCode,
                         String issuer,
                         URI redirectUri,
                         Long accessTokenLifeTime,
                         Long refreshTokenLifeTime) {
    checkClient(clientId);
    KeyDto keyDto = keyService.getKeys(null, Utils.serverCredential);
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
      Utils.serverCredential);
    if (session == null) throw new OAuthRuntimeException(INVALID_GRANT, "Authorization request not found");
    if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - session.getDateIns().getTime()) > 10) {
      throw new OAuthRuntimeException(INVALID_GRANT, "Authorization code active time has expired.");
    }
    if (session.getAccessTokenId() != null) {
      throw new OAuthRuntimeException(INVALID_GRANT, "Request is finished.");
    }
    Token accessToken = Utils.generateToken(session.getOperatorLogin(), Collections.singletonList(clientId),
      session.getOperator().getValue(), issuer, keyDto.getPrivateKey(), accessTokenLifeTime, null);
    Token refreshToken = Utils.generateToken(session.getOperatorLogin(), Collections.singletonList(clientId),
      session.getOperator().getValue(), issuer, keyDto.getPrivateKey(), refreshTokenLifeTime, null);
    TokenDto tokenDto = new TokenDto();
    tokenDto.setAccessToken(accessToken.asString());
    tokenDto.setRefreshToken(refreshToken.asString());
    tokenDto.setExpiresIn(new Long(accessTokenLifeTime));
    tokenDto.setTokenType(TOKEN_TYPE);
    updateSession(session,
      accessToken.getJti(),
      accessToken.getIssueTime(),
      accessToken.getExpirationTime(),
      refreshToken.getJti(),
      refreshToken.getIssueTime(),
      refreshToken.getExpirationTime(),
      Utils.serverCredential);
    return tokenDto;
  }
  
  private void checkClient(String clientId) {
    List<ClientDto> result = clientService.getClient(clientId, null, Utils.serverCredential.getOperatorId());
    if (result.size() != 1) {
      throw new OAuthRuntimeException(UNAUTHORIZED_CLIENT, "Client not found");
    }
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
                         Long accessTokenLifeTime,
                         Long refreshTokenLifeTime) {
    checkClient(clientId);
    checkClientGrantTypes(clientId, GrantType.PASSWORD);
    KeyDto keyDto = keyService.getKeys(null, Utils.serverCredential);
    return createTokenPair(keyDto.getPrivateKey(), issuer, clientId, username, userId, accessTokenLifeTime, refreshTokenLifeTime);
  }
  
  @Override
  public TokenDto create(String clientId,
                         String refreshTokenString,
                         String issuer,
                         Long accessTokenLifeTime,
                         Long refreshTokenLifeTime) {
    checkClient(clientId);
    checkClientGrantTypes(clientId, GrantType.REFRESH_TOKEN);
    KeyDto keyDto = keyService.getKeys(null, Utils.serverCredential);
    try {
      Token refreshToken = TokenImpl.parseFromString(refreshTokenString);
      Verifier verifier = new VerifierRSA(clientId != null ? Collections.singletonList(clientId) : null, issuer, new Date(), keyDto.getPublicKey());
      if (verifier.verify(refreshToken)) {
        SessionDto sessionDto = getSession(null,
          clientId,
          null,
          null,
          refreshToken.getJti(),
          Utils.serverCredential);
        if (sessionDto == null) throw new OAuthRuntimeException(INVALID_GRANT, "Authorization request not found");
        sessionService.deleteRecord(String.valueOf(sessionDto.getSessionId()), Utils.serverCredential);
        return createTokenPair(
          keyDto.getPrivateKey(),
          issuer,
          refreshToken.getAudience().isEmpty() ? null : refreshToken.getAudience().get(0),
          refreshToken.getUsername(),
          Integer.valueOf(refreshToken.getSubject()),
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
                         Long accessTokenLifeTime,
                         Long refreshTokenLifeTime) {
    checkClient(clientId);
    checkClientGrantTypes(clientId, GrantType.CLIENT_CREDENTIALS);
    KeyDto keyDto = keyService.getKeys(null, Utils.serverCredential);
    return createTokenPair(keyDto.getPrivateKey(), issuer, clientId, clientId, clientOperatorId, accessTokenLifeTime, refreshTokenLifeTime);
  }
  
  private TokenDto createTokenPair(String privateKeyString,
                                   String issuer,
                                   String clientId,
                                   String username,
                                   Integer userId,
                                   Long accessTokenLifeTime,
                                   Long refreshTokenLifeTime) {
    Token accessToken = Utils.generateToken(username, Collections.singletonList(clientId), userId, issuer,
      privateKeyString, accessTokenLifeTime, null);
    Token refreshToken = Utils.generateToken(username, Collections.singletonList(clientId), userId, issuer, privateKeyString, refreshTokenLifeTime, null);
    TokenDto tokenDto = new TokenDto();
    tokenDto.setAccessToken(accessToken.asString());
    tokenDto.setExpiresIn(accessTokenLifeTime);
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
    sessionService.create(createDto, Utils.serverCredential);
    return tokenDto;
  }
  
  @Override
  public TokenInfoDto getTokenInfo(String issuerContext, String tokenString) {
    TokenInfoDto result = new TokenInfoDto();
    Token token;
    try {
      KeyDto keyDto = keyService.getKeys(null, Utils.serverCredential);
      token = TokenImpl.parseFromString(tokenString);
      SessionDto sessionDto = getSession(null,
        null,
        null,
        token.getJti(),
        null,
        Utils.serverCredential);
      if (sessionDto == null) {
        result.setActive(false);
        return result;
      }
      Verifier verifier = new VerifierRSA(null, issuerContext, new Date(), keyDto.getPublicKey());
      if (!verifier.verify(token)) {
        result.setActive(false);
        return result;
      }
      result.setClient_id(sessionDto.getClientId());
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
    result.setUsername(token.getUsername());
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
        Utils.serverCredential);
      if (sessionDto != null) {
        sessionService.deleteRecord(String.valueOf(sessionDto.getSessionId()), Utils.serverCredential);
      }
    } catch (ParseException e) {
      throw new OAuthRuntimeException(SERVER_ERROR, e);
    } catch (IllegalArgumentException ex) {
      throw new OAuthRuntimeException(SERVER_ERROR, ex);
    }
  }
  
  private String generateCode() {
    try {
      return UUID.randomUUID().toString().replaceAll("-", "");
    } catch (Throwable th) {
      throw new OAuthRuntimeException(SERVER_ERROR, th);
    }
  }
}
