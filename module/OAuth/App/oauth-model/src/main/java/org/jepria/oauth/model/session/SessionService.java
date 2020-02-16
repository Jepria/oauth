package org.jepria.oauth.model.session;

import org.jepria.oauth.model.session.dto.SessionCreateDto;
import org.jepria.oauth.model.session.dto.SessionDto;
import org.jepria.oauth.model.session.dto.SessionSearchDto;
import org.jepria.oauth.model.session.dto.SessionUpdateDto;
import org.jepria.server.service.security.Credential;

import java.util.List;

public interface SessionService {
  
  /**
   * @param template
   * @param credential
   * @return
   */
  List<SessionDto> find(SessionSearchDto template, Credential credential);
  
  /**
   * @param sessionId
   * @param credential
   * @return
   */
  SessionDto findByPrimaryKey(Integer sessionId, Credential credential);
  
  /**
   * @param record
   * @param credential
   * @return
   */
  Integer create(SessionCreateDto record, Credential credential);
  
  /**
   * @param record
   * @param credential
   */
  void update(SessionUpdateDto record, Credential credential);
  
  /**
   * @param sessionId
   * @param credential
   */
  void delete(Integer sessionId, Credential credential);
  
}
