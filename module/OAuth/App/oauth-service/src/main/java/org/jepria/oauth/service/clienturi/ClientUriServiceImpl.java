package org.jepria.oauth.service.clienturi;

import org.jepria.oauth.model.clienturi.ClientUriService;
import org.jepria.oauth.model.clienturi.dto.ClientUriDto;
import org.jepria.oauth.model.clienturi.dto.ClientUriSearchDto;
import org.jepria.server.data.Dao;

import java.util.List;

public class ClientUriServiceImpl implements ClientUriService {

  private final Dao dao;

  public ClientUriServiceImpl(Dao dao) {
    this.dao = dao;
  }

  public List<ClientUriDto> findClientUri(ClientUriSearchDto template, Integer operatorId) {
    return (List<ClientUriDto>) dao.find(template, operatorId);
  }
}
