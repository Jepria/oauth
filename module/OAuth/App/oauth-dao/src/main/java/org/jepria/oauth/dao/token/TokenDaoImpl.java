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
}
