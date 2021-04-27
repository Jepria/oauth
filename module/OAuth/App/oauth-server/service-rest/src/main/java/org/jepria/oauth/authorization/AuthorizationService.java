package org.jepria.oauth.authorization;

import org.jepria.oauth.session.dto.SessionDto;

public interface AuthorizationService {

  /**
   * Create new auth session
   *
   * @param responseType response type
   * @param clientId  client application id
   * @param redirectUri client redirect URI
   * @param codeChallenge one time hash
   * @return OAuth session
   */
  SessionDto authorize(String responseType,
                       String clientId,
                       String redirectUri,
                       String codeChallenge);

  /**
   * Get current session from sessionToken or create new auth session
   *
   * @param responseType тип ответа
   * @param clientId ID клиентского приложения
   * @param redirectUri URL для перенаправления
   * @param codeChallenge хэш проверочного кода
   * @param sessionToken токен сессии
   * @param issuer имя сервера
   * @return OAuth session
   */
  SessionDto authorize(String responseType,
                       String clientId,
                       String redirectUri,
                       String codeChallenge,
                       String sessionToken,
                       String issuer);
  
  /**
   * Get client login uri
   * @param clientId
   * @return
   */
  String getClientLoginUri(String clientId);
}
