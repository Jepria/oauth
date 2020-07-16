package org.jepria.oauth.session;

import org.jepria.oauth.session.dao.SessionDao;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDto;
import org.jepria.server.data.Dao;
import org.jepria.server.data.OptionDto;
import org.jepria.server.data.RecordDefinition;
import org.jepria.server.service.rest.EntityServiceImpl;
import org.jepria.server.service.security.Credential;

import java.util.List;

public class SessionServiceImpl extends EntityServiceImpl implements SessionService {
  
  private final SessionDao dao;
  
  public SessionServiceImpl(SessionDao dao, RecordDefinition recordDefinition) {
    super(dao, recordDefinition);
    this.dao = dao;
  }

  public List<SessionDto> find(SessionSearchDto template, Credential credential) {
    List<SessionDto> result = (List<SessionDto>) dao.find(template, credential.getOperatorId());
    return result;
  }

  @Override
  public List<OptionDto<String>> getOperators(String operatorName, Integer maxRowCount) {
    return dao.getOperators(operatorName, maxRowCount);
  }
}
