package org.jepria.oauth.model.client;

import org.jepria.server.data.OptionDto;

import java.util.List;

public interface ClientService {

  List<OptionDto<String>> getGrantType();

  List<OptionDto<String>> getGrantResponseType(List<String> grantTypeCodes);

  List<OptionDto<String>> getClientGrantTypes(String clientId);

  List<OptionDto<String>> getClientResponseTypes(String clientId);
}
