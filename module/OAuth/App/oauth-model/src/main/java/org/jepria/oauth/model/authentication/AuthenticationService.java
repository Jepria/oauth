package org.jepria.oauth.model.authentication;

public interface AuthenticationService {
  
  /**
   * @param username
   * @param password
   * @return
   */
  Integer loginByPassword(String username, String password);

  /**
   * @param clientId
   * @param clientSecret
   * @return
   */
  Integer loginByClientCredentials(String clientId, String clientSecret);

  /**
   * @param clientId
   * @return
   */
  Integer loginByClientId(String clientId);

  /**
   *
   * @param authorizationCode
   * @param clientId
   * @param codeVerifier
   * @return
   */
  Integer loginByPKCE(String authorizationCode, String clientId, String codeVerifier);

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
  String authenticate(
    String authCode,
    String redirectUri,
    String clientId,
    String username,
    String password,
    String host,
    String publicKey,
    String privateKey);
}
