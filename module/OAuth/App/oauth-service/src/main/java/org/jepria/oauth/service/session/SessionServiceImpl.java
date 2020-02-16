package org.jepria.oauth.service.session;

import org.jepria.oauth.model.session.SessionService;
import org.jepria.oauth.model.session.dao.SessionDao;
import org.jepria.oauth.model.session.dto.SessionCreateDto;
import org.jepria.oauth.model.session.dto.SessionDto;
import org.jepria.oauth.model.session.dto.SessionSearchDto;
import org.jepria.oauth.model.session.dto.SessionUpdateDto;
import org.jepria.server.service.rest.EntityService;
import org.jepria.server.service.security.Credential;

import java.util.List;

public class SessionServiceImpl implements SessionService {
  
  private final SessionDao dao;
  private final EntityService entityService;
  
  public SessionServiceImpl(EntityService entityService, SessionDao dao) {
    this.entityService = entityService;
    this.dao = dao;
  }
  
  /**
   *
   * @param template
   * @param credential
   * @return
   */
  public List<SessionDto> find(SessionSearchDto template, Credential credential) {
    template.setHasToken(false);
    List<SessionDto> result = (List<SessionDto>) dao.find(template, credential.getOperatorId());
    return result;
  }

  /**
   *
   * @param sessionId
   * @param credential
   * @return
   */
  public SessionDto findByPrimaryKey(Integer sessionId, Credential credential) {
    return (SessionDto) entityService.getRecordById(String.valueOf(sessionId), credential);
  }

  /**
   *
   * @param record
   * @param credential
   * @return
   */
  public Integer create(SessionCreateDto record, Credential credential) {
    return Integer.valueOf(entityService.create(record, credential));
  }

  /**
   *
   * @param record
   * @param credential
   */
  public void update(SessionUpdateDto record, Credential credential) {
    entityService.update(String.valueOf(record.getSessionId()), record, credential);
  }

  /**
   *
   * @param sessionId
   * @param credential
   */
  public void delete(Integer sessionId, Credential credential) {
    entityService.deleteRecord(String.valueOf(sessionId), credential);
  }

}
