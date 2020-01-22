package org.jepria.oauth.clienturi;

import org.jepria.oauth.clienturi.dto.ClientUriDto;
import org.jepria.oauth.clienturi.dto.ClientUriSearchDtoLocal;

import java.util.List;

public class ClientUriService {

  public List<ClientUriDto> findClientUri(ClientUriSearchDtoLocal template, Integer operatorId) {
    return (List<ClientUriDto>) ClientUriServerFactory.getInstance().getDao().find(template, operatorId);
  }
}
