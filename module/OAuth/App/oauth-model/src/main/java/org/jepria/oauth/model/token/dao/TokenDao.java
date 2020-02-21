package org.jepria.oauth.model.token.dao;

import org.jepria.server.data.Dao;

public interface TokenDao {
  String getPublicKey();
  String getPrivateKey();
  void createRSAKeyPair(String publicKey, String privateKey);
}
