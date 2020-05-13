package org.jepria.oauth.client;

import org.jepria.oauth.client.ClientService;
import org.jepria.oauth.client.dao.ClientDao;
import org.jepria.oauth.client.dto.ClientDto;
import org.jepria.oauth.client.dto.ClientSearchDto;
import org.jepria.oauth.sdk.ApplicationType;
import org.jepria.oauth.sdk.GrantType;

import java.util.List;

public class ClientServiceImpl implements ClientService {

  private final ClientDao dao;

  public ClientServiceImpl(ClientDao dao) {
    this.dao = dao;
  }

  public List<String> getGrantType() {
    return GrantType.getGrantTypes();
  }

  public List<String> getGrantResponseType(List<String> grantTypeCodes) {
    return GrantType.getGrantResponseTypes(grantTypeCodes);
  }

  public List<String> getClientGrantTypes(String clientId) {
    return dao.getClientGrantTypes(clientId);
  }

  public List<String> getClientResponseTypes(String clientId) {
    return GrantType.getGrantResponseTypes(dao.getClientGrantTypes(clientId));
  }

  @Override
  public List<String> getApplicationTypes() {
    return ApplicationType.getApplicationTypes();
  }

  @Override
  public List<String> getApplicationGrantTypes(String applicationTypeCodes) {
    return ApplicationType.getApplicationGrantTypes(applicationTypeCodes);
  }
  
  @Override
  public List<ClientDto> getClient(String clientName, Integer operatorId) {
    ClientSearchDto searchDto = new ClientSearchDto();
    searchDto.setClientName(clientName);
    return (List<ClientDto>) dao.find(searchDto, operatorId);
  }
}
