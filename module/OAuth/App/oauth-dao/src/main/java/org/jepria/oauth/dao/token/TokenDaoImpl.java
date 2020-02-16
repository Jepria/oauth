package org.jepria.oauth.dao.token;

import org.jepria.oauth.model.token.dao.TokenDao;

import java.util.List;
import java.util.Map;

public class TokenDaoImpl implements TokenDao {
  @Override
  public String getPublicKey() {
    return null;
  }

  @Override
  public String getPrivateKey() {
    return null;
  }

  @Override
  public void createRSAKeyPair(String publicKey, String privateKey) {

  }

  @Override
  public List<?> find(Object template, Integer operatorId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<?> findByPrimaryKey(Map<String, ?> primaryKeyMap, Integer operatorId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object create(Object record, Integer operatorId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void update(Map<String, ?> primaryKey, Object record, Integer operatorId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(Map<String, ?> primaryKey, Integer operatorId) {
    throw new UnsupportedOperationException();
  }
}
