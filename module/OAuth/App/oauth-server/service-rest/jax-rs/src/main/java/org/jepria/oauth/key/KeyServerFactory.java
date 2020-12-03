package org.jepria.oauth.key;

import org.jepria.oauth.key.dao.KeyDao;
import org.jepria.oauth.main.server.OAuthServerFactory;

import javax.inject.Inject;

public class KeyServerFactory extends OAuthServerFactory<KeyDao> {

  @Inject
  public KeyServerFactory(KeyDao dao) {
    super(dao, "jdbc/OAuthDS");
  }

  public KeyService getService() {
    return new KeyServiceImpl(getDao());
  }
}
