package org.jepria.oauth.authorization;

import org.jepria.oauth.client.ClientServerFactory;
import org.jepria.oauth.clienturi.ClientUriServerFactory;
import org.jepria.oauth.model.authorization.AuthorizationService;
import org.jepria.oauth.service.authorization.AuthorizationServiceImpl;
import org.jepria.oauth.session.SessionServerFactory;
import org.jepria.server.ServerFactory;
import org.jepria.server.data.Dao;

public class AuthorizationServerFactory extends ServerFactory<Dao> {

  private static AuthorizationServerFactory instance;
  private AuthorizationService service;

  private AuthorizationServerFactory() {
    super(null, null);
  }

  public static AuthorizationServerFactory getInstance() {
    if (instance == null) {
      instance = new AuthorizationServerFactory();
    }
    return instance;
  }

  public AuthorizationService getService() {
    if (service == null) {
      service = new AuthorizationServiceImpl(SessionServerFactory.getInstance().getService(),
          ClientServerFactory.getInstance().getService());
    }
    return service;
  }

  @Override
  public Dao getDao() {
    throw new UnsupportedOperationException();
  }
}
