package org.jepria.oauth.service.session;

import org.jepria.oauth.model.session.SessionService;
import org.jepria.oauth.model.session.dto.SessionDto;
import org.jepria.oauth.model.session.dto.SessionSearchDto;
import org.jepria.server.data.Dao;
import org.jepria.server.data.RecordDefinition;
import org.jepria.server.service.rest.EntityServiceImpl;
import org.jepria.server.service.security.Credential;

import java.util.List;

public class SessionServiceImpl extends EntityServiceImpl implements SessionService {
  
  private final Dao dao;
  
  public SessionServiceImpl(Dao dao, RecordDefinition recordDefinition) {
    super(dao, recordDefinition);
    this.dao = dao;
  }

  public List<SessionDto> find(SessionSearchDto template, Credential credential) {
    template.setHasToken(false);
    List<SessionDto> result = (List<SessionDto>) dao.find(template, credential.getOperatorId());
    return result;
  }

}
