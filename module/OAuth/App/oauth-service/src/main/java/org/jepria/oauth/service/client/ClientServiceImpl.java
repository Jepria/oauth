package org.jepria.oauth.service.client;

import org.jepria.oauth.model.client.ClientService;
import org.jepria.oauth.model.client.dao.ClientDao;
import org.jepria.server.data.OptionDto;

import java.util.List;

public class ClientServiceImpl implements ClientService {

  private final ClientDao dao;

  public ClientServiceImpl(ClientDao dao) {
    this.dao = dao;
  }

  public List<OptionDto<String>> getGrantType() {
    return dao.getGrantType();
  }

  public List<OptionDto<String>> getGrantResponseType(List<String> grantTypeCodes) {
    return dao.getGrantResponseType(grantTypeCodes);
  }

  public List<OptionDto<String>> getClientGrantTypes(String clientId) {
    return dao.getClientGrantTypes(clientId);
  }

  public List<OptionDto<String>> getClientResponseTypes(String clientId) {
    return dao.getClientResponseTypes(clientId);
  }

}