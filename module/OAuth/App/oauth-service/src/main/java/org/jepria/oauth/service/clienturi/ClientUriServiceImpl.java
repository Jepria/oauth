package org.jepria.oauth.service.clienturi;

import org.jepria.oauth.model.clienturi.ClientUriService;
import org.jepria.oauth.model.clienturi.dao.ClientUriDao;
import org.jepria.oauth.model.clienturi.dto.ClientUriDto;
import org.jepria.oauth.model.clienturi.dto.ClientUriSearchDto;

import java.util.List;

public class ClientUriServiceImpl implements ClientUriService {

  private final ClientUriDao dao;

  public ClientUriServiceImpl(ClientUriDao dao) {
    this.dao = dao;
  }

  public List<ClientUriDto> findClientUri(ClientUriSearchDto template, Integer operatorId) {
    return (List<ClientUriDto>) dao.find(template, operatorId);
  }
}
