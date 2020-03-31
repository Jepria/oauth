package org.jepria.oauth.client;

import org.jepria.oauth.client.dto.ClientCreateDto;
import org.jepria.oauth.client.dto.ClientDto;
import org.jepria.oauth.client.dto.ClientSearchDto;
import org.jepria.oauth.client.dto.ClientUpdateDto;
import org.jepria.server.data.RecordDefinitionDtoImpl;

public class ClientRecordDefinition extends RecordDefinitionDtoImpl {

  public ClientRecordDefinition() {
    super(ClientCreateDto.class, ClientUpdateDto.class, ClientDto.class, ClientSearchDto.class);
  }

}
