package org.jepria.oauth.session;

import org.jepria.oauth.model.session.dto.SessionDto;
import org.jepria.oauth.model.session.dto.SessionSearchDto;
import org.jepria.server.data.RecordDefinitionDtoImpl;

public class SessionRecordDefinition extends RecordDefinitionDtoImpl {

  public SessionRecordDefinition() {
    super(SessionSearchDto.class, SessionDto.class);
  }
}
