package org.jepria.oauth.clienturi;

import org.jepria.oauth.clienturi.dao.ClientUriDao;
import org.jepria.oauth.clienturi.dao.ClientUriDaoImpl;
import org.jepria.server.ServerFactory;

public class ClientUriServerFactory extends ServerFactory<ClientUriDao> {

  private ClientUriServerFactory() {
    super(new ClientUriDaoImpl(), "jdbc/RFInfoDS");
  }

  public static ClientUriServerFactory getInstance() {
    return new ClientUriServerFactory();
  }
}
