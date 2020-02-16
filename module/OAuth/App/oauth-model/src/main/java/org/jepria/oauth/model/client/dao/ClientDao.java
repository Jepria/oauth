package org.jepria.oauth.model.client.dao;

import org.jepria.server.data.Dao;
import org.jepria.server.data.OptionDto;

import java.util.List;

public interface ClientDao extends Dao {
  List<OptionDto<String>> getClientResponseTypes(String clientId);

  List<OptionDto<String>> getClientGrantTypes(String clientId);

  List<OptionDto<String>> getApplicationGrantType(String applicationTypeCode);

  List<OptionDto<String>> getGrantType();

  List<OptionDto<String>> getGrantResponseType(List<String> grantTypeCodes);
}
