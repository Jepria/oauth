package org.jepria.oauth.authentication;

import org.jepria.oauth.authentication.dao.AuthenticationDao;
import org.jepria.oauth.authentication.dto.SessionTokenDto;
import org.jepria.oauth.clienturi.ClientUriService;
import org.jepria.oauth.clienturi.dto.ClientUriDto;
import org.jepria.oauth.clienturi.dto.ClientUriSearchDto;
import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.key.KeyService;
import org.jepria.oauth.key.dto.KeyDto;
import org.jepria.oauth.main.Utils;
import org.jepria.oauth.sdk.token.Decryptor;
import org.jepria.oauth.sdk.token.Token;
import org.jepria.oauth.sdk.token.TokenImpl;
import org.jepria.oauth.sdk.token.Verifier;
import org.jepria.oauth.sdk.token.rsa.DecryptorRSA;
import org.jepria.oauth.sdk.token.rsa.SignatureVerifierRSA;
import org.jepria.oauth.session.LoginConfirmService;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDto;
import org.jepria.server.data.RuntimeSQLException;

import java.text.ParseException;
import java.util.List;

import static org.jepria.oauth.sdk.OAuthConstants.*;

public class AuthenticationServiceImpl implements AuthenticationService {

  private final AuthenticationDao dao;
  private final LoginConfirmService sessionService;
  private final ClientUriService clientUriService;
  private final KeyService keyService;

  public AuthenticationServiceImpl(AuthenticationDao dao, LoginConfirmService sessionService, ClientUriService clientUriService, KeyService keyService) {
    this.dao = dao;
    this.sessionService = sessionService;
    this.clientUriService = clientUriService;
    this.keyService = keyService;
  }

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
    String sessionId,
    String redirectUri,
    String clientId,
    String username,
    String password,
    String host,
    Integer sessionTokenLifeTime) {
    Integer operatorId = loginByPassword(username, password);
    return sessionService.confirm(sessionId, username, operatorId, redirectUri, clientId, host, Utils.serverCredential, sessionTokenLifeTime);
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
      KeyDto keyDto = keyService.getKeys(null, Utils.serverCredential);
      Decryptor decryptor = new DecryptorRSA(keyDto.getPrivateKey());
      token = decryptor.decrypt(token);
      Verifier verifier = new SignatureVerifierRSA(keyDto.getPublicKey());
      if (verifier.verify(token) && issuer.equals(token.getIssuer())) {
        SessionSearchDto searchTemplate = new SessionSearchDto();
        searchTemplate.setSessionTokenId(token.getJti());
        sessionService
          .find(searchTemplate, Utils.serverCredential)
          .stream()
          .forEach(sessionDto -> sessionService.deleteRecord(String.valueOf(sessionDto.getSessionId()), Utils.serverCredential));
      }
    } catch (ParseException ex) {
      throw new OAuthRuntimeException(SERVER_ERROR, ex);
    }
  }
}
