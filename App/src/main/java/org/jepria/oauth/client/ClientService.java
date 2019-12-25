package org.jepria.oauth.client;

import org.jepria.server.data.OptionDto;

import java.util.List;

public class ClientService {

  public List<OptionDto<String>> getGrantType() {
    return ClientServerFactory.getInstance().getDao().getGrantType();
  }

  public List<OptionDto<String>> getGrantResponseType(List<String> grantTypeCodes) {
    return ClientServerFactory.getInstance().getDao().getGrantResponseType(grantTypeCodes);
  }

}
