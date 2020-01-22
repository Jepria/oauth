package org.jepria.oauth.clienturi;

import org.jepria.oauth.clienturi.dto.ClientUriDto;
import org.jepria.server.data.RecordDefinitionDtoImpl;

public class ClientUriRecordDefinition extends RecordDefinitionDtoImpl {

  public ClientUriRecordDefinition() {
    super(ClientUriDto.class);
  }
}
