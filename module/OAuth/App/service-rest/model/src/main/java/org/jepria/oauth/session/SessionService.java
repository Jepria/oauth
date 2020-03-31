package org.jepria.oauth.session;

import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDto;
import org.jepria.server.service.rest.EntityService;
import org.jepria.server.service.security.Credential;

import java.util.List;

public interface SessionService extends EntityService {
  
  /**
   * @param template шаблон поиска
   * @param credential креденциал
   * @return список сессий
   */
  List<SessionDto> find(SessionSearchDto template, Credential credential);
  
}
