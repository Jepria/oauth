package org.jepria.oauth.clienturi;

import org.jepria.oauth.clienturi.dao.ClientUriDao;
import org.jepria.oauth.clienturi.dto.ClientUriDto;
import org.jepria.oauth.clienturi.dto.ClientUriSearchDtoLocal;

import java.util.List;

public class ClientUriService {

  private final ClientUriDao dao;

  public ClientUriService(ClientUriDao dao) {
    this.dao = dao;
  }

  public List<ClientUriDto> findClientUri(ClientUriSearchDtoLocal template, Integer operatorId) {
    return (List<ClientUriDto>) dao.find(template, operatorId);
  }
}
