package org.jepria.oauth.client;

import org.jepria.oauth.client.dao.ClientDao;
import org.jepria.oauth.main.server.OAuthServerFactory;

import javax.inject.Inject;

public class ClientServerFactory extends OAuthServerFactory<ClientDao> {

  @Inject
  public ClientServerFactory(ClientDao dao) {
    super(dao, "jdbc/OAuthDS");
  }

  public ClientService getService() {
    return new ClientServiceImpl(getDao());
  }

}
