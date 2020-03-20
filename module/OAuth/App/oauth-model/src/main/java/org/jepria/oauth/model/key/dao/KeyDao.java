package org.jepria.oauth.model.key.dao;

import org.jepria.oauth.model.key.dto.KeyCreateDto;
import org.jepria.oauth.model.key.dto.KeyDto;

public interface KeyDao {

  String setKeys(KeyCreateDto keyCreateDto, Integer operatorId);
  
  KeyDto getKeys(String keyId, Integer operatorId);
}
