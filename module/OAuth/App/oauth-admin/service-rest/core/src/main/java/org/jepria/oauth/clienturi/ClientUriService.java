package org.jepria.oauth.clienturi;

import org.jepria.oauth.clienturi.dto.ClientUriDto;
import org.jepria.oauth.clienturi.dto.ClientUriSearchDto;

import java.util.List;

public interface ClientUriService {
  
  List<ClientUriDto> findClientUri(ClientUriSearchDto template, Integer operatorId);
  
}
