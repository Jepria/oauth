package org.jepria.oauth.session;

import org.jepria.oauth.session.dto.SessionCreateDto;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDtoLocal;
import org.jepria.oauth.session.dto.SessionUpdateDto;
import org.jepria.server.service.security.Credential;

import java.util.*;

public class SessionService {

  /**
   *
   * @param template
   * @param credential
   * @return
   */
  public List<SessionDto> find(SessionSearchDtoLocal template, Credential credential) {
    template.setHasToken(false);
    List<SessionDto> result = (List<SessionDto>) SessionServerFactory.getInstance().getDao().find(template, credential.getOperatorId());
    return result;
  }

  /**
   *
   * @param sessionId
   * @param credential
   * @return
   */
  public SessionDto findByPrimaryKey(Integer sessionId, Credential credential) {
    return (SessionDto) SessionServerFactory.getInstance().getEntityService().getRecordById(String.valueOf(sessionId), credential);
  }

  /**
   *
   * @param record
   * @param credential
   * @return
   */
  public Integer create(SessionCreateDto record, Credential credential) {
    return Integer.valueOf(SessionServerFactory.getInstance().getEntityService().create(record, credential));
  }

  /**
   *
   * @param record
   * @param credential
   */
  public void update(SessionUpdateDto record, Credential credential) {
    SessionServerFactory.getInstance().getEntityService().update(String.valueOf(record.getSessionId()), record, credential);
  }

  /**
   *
   * @param sessionId
   * @param credential
   */
  public void delete(Integer sessionId, Credential credential) {
    SessionServerFactory.getInstance().getEntityService().deleteRecord(String.valueOf(sessionId), credential);
  }

}
