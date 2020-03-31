package org.jepria.oauth.authentication;

public interface AuthenticationService {
  
  /**
   * Проверка логина/пароля пользователя в системе
   *
   * @param username имя пользователя
   * @param password пароль пользователя
   * @return уникальный ID пользователя
   */
  Integer loginByPassword(String username, String password);

  /**
   * Проверка секретного слова клиентского приложения в системе
   *
   * @param clientId ID клиентского приложения
   * @param clientSecret секретное слово клиенского приложения
   * @return уникальный ID клиенского приложения
   */
  Integer loginByClientSecret(String clientId, String clientSecret);

  /**
   * Проверка ID клиентского приложения в системе
   *
   * @param clientId ID клиентского приложения
   * @return уникальный ID клиенского приложения
   */
  void loginByClientId(String clientId);

  /**
   *
   * @param clientId ID клиентского приложения
   * @param authorizationCode одноразовый код
   * @param codeVerifier проверочный код
   * @return уникальный ID клиенского приложения
   */
  void loginByAuthorizationCode(String clientId, String authorizationCode, String codeVerifier);

  /**
   * <pre>
   * Аутентификация OAuth, по логину/паролю пользвателя.
   * Результат -> созданная SSO сессия OAuth
   * </pre>
   *
   * @param authCode одноразовый код
   * @param redirectUri URL для перенаправления
   * @param clientId ID клиентского приложения
   * @param username имя пользователя
   * @param password пароль пользователя
   * @param host имя сервера
   * @return Session Token
   */
  String authenticate(
    String authCode,
    String redirectUri,
    String clientId,
    String username,
    String password,
    String host);

  /**
   * @param clientId ID клиентского приложения
   * @param redirectUri URL для перенаправления
   * @param sessionToken токен сессии
   * @param issuer имя сервера
   */
  void logout(String clientId,
              String redirectUri,
              String sessionToken,
              String issuer);
}
