package org.jepria.oauth.authentication;

import org.jepria.oauth.authentication.dto.SessionTokenDto;
import org.jepria.oauth.sdk.token.Token;

public interface AuthenticationService {
  
  /**
   * Authenticate user with login/password
   *
   * @param username login
   * @param password password
   * @return operator id
   */
  Integer loginByPassword(String username, String password);

  Integer loginByPasswordHash(String username, String passwordHash);

  /**
   * authenticate client with clientId/clientSecret
   *
   * @param clientId client application id
   * @param clientSecret client application secret
   * @return уникальный ID клиенского приложения
   */
  Integer loginByClientSecret(String clientId, String clientSecret);

  /**
   * Check client id validity
   *
   * @param clientId client application id
   */
  void loginByClientId(String clientId);

  /**
   *
   * @param clientId ID клиентского приложения
   * @param authorizationCode одноразовый код
   * @param codeVerifier проверочный код
   */
  void loginByAuthorizationCode(String clientId, String authorizationCode, String codeVerifier);

  /**
   * <pre>
   * Authenticate user by logn/password
   * </pre>
   *
   * @param sessionId auth session id
   * @param redirectUri client redirect uri
   * @param clientId client application id
   * @param username username
   * @param password password
   * @param host issuer server name
   * @param sessionTokenLifeTime session token life time
   * @return session token
   */
  SessionTokenDto authenticate(
    String sessionId,
    String redirectUri,
    String clientId,
    String username,
    String password,
    String host,
    Long sessionTokenLifeTime);

  /**
   * @param clientId client application id
   * @param redirectUri client redirect uri
   * @param sessionToken session token
   * @param issuer issuer server name
   */
  void logout(String clientId,
              String redirectUri,
              String sessionToken,
              String issuer);
}
