package org.jepria.oauth.authorization;

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
      service = new AuthorizationService(SessionServerFactory.getInstance().getService());
    }
    return service;
  }

  @Override
  public Dao getDao() {
    throw new UnsupportedOperationException();
  }
}
