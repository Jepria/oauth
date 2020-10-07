package org.jepria.oauth.key;

import org.jepria.oauth.key.dao.KeyDao;
import org.jepria.oauth.main.rest.jersey.OAuthServerFactory;
import org.jepria.server.ServerFactory;

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
