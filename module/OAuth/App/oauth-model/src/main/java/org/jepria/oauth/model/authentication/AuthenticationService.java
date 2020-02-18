package org.jepria.oauth.model.authentication;

public interface AuthenticationService {
  
  /**
   * @param username имя пользователя
   * @param password пароль пользователя
   * @return уникальный ID пользователя
   */
  Integer loginByPassword(String username, String password);

  /**
   * @param clientId ID клиентского приложения
   * @param clientSecret секретное слово клиенского приложения
   * @return уникальный ID клиенского приложения
   */
  Integer loginByClientCredentials(String clientId, String clientSecret);

  /**
   * @param clientId ID клиентского приложения
   * @return уникальный ID клиенского приложения
   */
  Integer loginByClientId(String clientId);

  /**
   *
   * @param authorizationCode одноразовый код
   * @param clientId ID клиентского приложения
   * @param codeVerifier проверочный код
   * @return уникальный ID клиенского приложения
   */
  Integer loginByPKCE(String authorizationCode, String clientId, String codeVerifier);

  /**
   * @param authCode одноразовый код
   * @param redirectUri URL для перенаправления
   * @param clientId ID клиентского приложения
   * @param username имя пользователя
   * @param password пароль пользователя
   * @param host имя сервера
   * @param publicKey публичный ключ
   * @param privateKey приватный ключ
   * @return Session Cookie
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
