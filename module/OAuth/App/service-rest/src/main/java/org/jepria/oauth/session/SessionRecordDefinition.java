package org.jepria.oauth.session;

import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDtoLocal;
import org.jepria.server.data.RecordDefinitionDtoImpl;

public class SessionRecordDefinition extends RecordDefinitionDtoImpl {

  public SessionRecordDefinition() {
    super(SessionSearchDtoLocal.class, SessionDto.class);
  }
}
