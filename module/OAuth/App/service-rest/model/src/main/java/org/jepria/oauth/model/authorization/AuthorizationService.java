package org.jepria.oauth.model.authorization;

import org.jepria.oauth.model.session.dto.SessionDto;

public interface AuthorizationService {

  /**
   *
   * @param responseType тип ответа
   * @param clientId  ID клиентского приложения
   * @param redirectUri URL для перенаправления
   * @param codeChallenge хэш проверочного кода
   * @return OAuth сессия
   */
  SessionDto authorize(String responseType,
                       String clientId,
                       String redirectUri,
                       String codeChallenge);

  /**
   * @param responseType тип ответа
   * @param clientId ID клиентского приложения
   * @param redirectUri URL для перенаправления
   * @param codeChallenge хэш проверочного кода
   * @param sessionToken токен сессии
   * @param issuer имя сервера
   * @return OAuth сессия
   */
  SessionDto authorize(String responseType,
                       String clientId,
                       String redirectUri,
                       String codeChallenge,
                       String sessionToken,
                       String issuer);
}
