package org.jepria.oauth.model.clienturi;

import org.jepria.oauth.model.clienturi.dto.ClientUriDto;
import org.jepria.oauth.model.clienturi.dto.ClientUriSearchDto;

import java.util.List;

public interface ClientUriService {
  
  List<ClientUriDto> findClientUri(ClientUriSearchDto template, Integer operatorId);
  
}
