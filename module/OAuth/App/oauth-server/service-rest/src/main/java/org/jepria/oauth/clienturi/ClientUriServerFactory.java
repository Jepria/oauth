package org.jepria.oauth.clienturi;

import org.jepria.oauth.clienturi.dao.ClientUriDao;
import org.jepria.oauth.main.server.OAuthServerFactory;
import org.jepria.server.data.Dao;

import javax.inject.Inject;

public class ClientUriServerFactory extends OAuthServerFactory<Dao> {

  @Inject
  public ClientUriServerFactory(ClientUriDao dao) {
    super(dao, "jdbc/OAuthDS");
  }

  public ClientUriService getService() {
    return new ClientUriServiceImpl(getDao());
  }
}
