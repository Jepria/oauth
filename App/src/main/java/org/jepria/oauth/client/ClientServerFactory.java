package org.jepria.oauth.client;

import org.jepria.oauth.client.dao.ClientDao;
import org.jepria.oauth.client.dao.ClientDaoImpl;
import org.jepria.server.ServerFactory;

public class ClientServerFactory extends ServerFactory<ClientDao> {

  private ClientServerFactory() {
    super(new ClientDaoImpl(), "jdbc/RFInfoDS");
  }

  public static ClientServerFactory getInstance() {
    return new ClientServerFactory();
  }

}
