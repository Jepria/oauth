package org.jepria.oauth.model.token;

import org.jepria.oauth.model.token.dto.TokenDto;
import org.jepria.oauth.model.token.dto.TokenInfoDto;
import org.jepria.server.service.security.Credential;

public interface TokenService {
  
  /**
   * Создание токена при входе в OAuth через IMPLICIT flow (responseType = token)
   * @param responseType тип ответа
   * @param privateKey приватный ключ
   * @param host имя сервера
   * @param authCode одноразовый код
   * @param clientId ID клиентского приложения
   * @param redirectUri URL для перенаправления
   * @return токен
   */
  TokenDto create(String responseType,
                  String privateKey,
                  String host,
                  String authCode,
                  String clientId,
                  String redirectUri);

  /**
   * Создание токена для всех OAuth GrantType
   *
   * @param grantType тип гранта
   * @param publicKey публичный ключ
   * @param privateKey приватный ключ
   * @param host имя сервера
   * @param authCode  одноразовый код
   * @param clientId ID клиентского приложения
   * @param clientSecret секретное слово клиентского приложения
   * @param codeVerifier проверочный код
   * @param redirectUri URL для перенаправления
   * @param username имя пользователя
   * @param password пароль пользователя
   * @param refreshToken refresh токен
   * @return токен
   */
  TokenDto create(String grantType,
                  String publicKey,
                  String privateKey,
                  String host,
                  String authCode,
                  String clientId,
                  String clientSecret,
                  String codeVerifier,
                  String redirectUri,
                  String username,
                  String password,
                  String refreshToken);
  
  /**
   * Получение информации о токене
   *
   * @param publicKey публичный ключ
   * @param hostContext имя сервера
   * @param tokenString токен
   * @return информация о токене
   */
  TokenInfoDto getTokenInfo(String publicKey, String hostContext, String tokenString, Credential credential);
  
  /**
   * Удаление выданного токена
   *
   * @param clientId ID клиентского приложения
   * @param tokenString токен
   * @param credential креденциал
   */
  void delete(String clientId, String tokenString, Credential credential);
  
}
