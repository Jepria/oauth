package org.jepria.oauth.key;

import org.jepria.oauth.key.dto.KeyDto;
import org.jepria.server.service.security.Credential;

public interface KeyService {
  
  String setKeys(Credential credential);
  
  KeyDto getKeys(String keyId, Credential credential);
  
}
