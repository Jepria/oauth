package org.jepria.oauth.key.dao;

import org.jepria.oauth.key.dto.KeyCreateDto;
import org.jepria.oauth.key.dto.KeyDto;

public interface KeyDao {

  String setKeys(KeyCreateDto keyCreateDto, Integer operatorId);
  
  KeyDto getKeys(String keyId, Integer operatorId);
}
