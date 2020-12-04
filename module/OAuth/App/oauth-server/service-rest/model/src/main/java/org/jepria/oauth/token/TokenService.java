package org.jepria.oauth.token;

import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.oauth.token.dto.TokenInfoDto;

import java.net.URI;

public interface TokenService {

  /**
   * Создание токена при входе в OAuth через IMPLICIT flow (responseType = token)
   * @param responseType тип ответа
   * @param clientId ID клиентского приложения
   * @param issuer имя сервера
   * @param authCode одноразовый код
   * @param redirectUri URI для перенаправления
   * @param accessTokenLifeTime Время жизни токена доступа
   * @return токен
   */
  TokenDto create(String responseType,
                  String clientId,
                  String issuer,
                  String authCode,
                  URI redirectUri,
                  Integer accessTokenLifeTime);
  /**
   * Создание токена для OAuth GrantType Authorization Code
   *
   * @param clientId ID клиентского приложения
   * @param authCode  одноразовый код
   * @param redirectUri URL для перенаправления
   * @param issuer имя сервера
   * @param accessTokenLifeTime Время жизни токена доступа
   * @return токен
   */
  TokenDto create(String clientId,
                  String authCode,
                  String issuer,
                  URI redirectUri,
                  Integer accessTokenLifeTime);
  /**
   * Создание токена для OAuth GrantType Resource owner credentials
   *
   * @param clientId ID клиентского приложения
   * @param username имя пользователя
   * @param userId ID пользователя
   * @param issuer имя сервера
   * @param accessTokenLifeTime Время жизни токена доступа
   * @param refreshTokenLifeTime Время жизни токена обновления
   * @return токен
   */
  TokenDto create(String clientId,
                  String username,
                  Integer userId,
                  String issuer,
                  Integer accessTokenLifeTime,
                  Integer refreshTokenLifeTime);
  
  /**
   * Создание токена для всех OAuth GrantType Refresh token
   *
   * @param clientId ID клиентского приложения
   * @param refreshToken refresh токен
   * @param issuer имя сервера
   * @param accessTokenLifeTime Время жизни токена доступа
   * @param refreshTokenLifeTime Время жизни токена обновления
   * @return токен
   */
  TokenDto create(String clientId,
                  String refreshToken,
                  String issuer,
                  Integer accessTokenLifeTime,
                  Integer refreshTokenLifeTime);
  
  /**
   * Создание токена для всех OAuth GrantType Client credentials
   *
   * @param clientId ID клиентского приложения
   * @param clientOperatorId operator id приложения
   * @param issuer имя сервера
   * @param accessTokenLifeTime Время жизни токена доступа
   * @param refreshTokenLifeTime Время жизни токена обновления
   * @return токен
   */
  TokenDto create(String clientId,
                  Integer clientOperatorId,
                  String issuer,
                  Integer accessTokenLifeTime,
                  Integer refreshTokenLifeTime);
  
  /**
   * Получение информации о токене
   *
   * @param hostContext имя сервера
   * @param tokenString токен
   * @return информация о токене
   */
  TokenInfoDto getTokenInfo(String hostContext, String tokenString);
  
  /**
   * Удаление выданного токена
   *
   * @param clientId ID клиентского приложения
   * @param tokenString токен
   */
  void delete(String clientId, String tokenString);
  
}
