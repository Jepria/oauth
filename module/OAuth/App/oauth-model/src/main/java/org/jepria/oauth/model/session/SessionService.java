package org.jepria.oauth.model.session;

import org.jepria.oauth.model.session.dto.SessionCreateDto;
import org.jepria.oauth.model.session.dto.SessionDto;
import org.jepria.oauth.model.session.dto.SessionSearchDto;
import org.jepria.oauth.model.session.dto.SessionUpdateDto;
import org.jepria.server.service.security.Credential;

import java.util.List;

public interface SessionService {
  
  /**
   * @param template шаблон поиска
   * @param credential креденциал
   * @return список сессий
   */
  List<SessionDto> find(SessionSearchDto template, Credential credential);
  
  /**
   * @param sessionId ID сессии
   * @param credential креденциал
   * @return сессия
   */
  SessionDto findByPrimaryKey(Integer sessionId, Credential credential);
  
  /**
   * @param record создаваемая запись
   * @param credential креденциал
   * @return ID созданной сессии
   */
  Integer create(SessionCreateDto record, Credential credential);
  
  /**
   * @param record обновляемая запись
   * @param credential креденциал
   */
  void update(SessionUpdateDto record, Credential credential);
  
  /**
   * @param sessionId ID сессии
   * @param credential креденциал
   */
  void delete(Integer sessionId, Credential credential);
  
}
