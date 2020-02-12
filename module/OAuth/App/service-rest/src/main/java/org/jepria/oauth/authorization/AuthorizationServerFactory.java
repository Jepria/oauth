package org.jepria.oauth.authorization;

import org.jepria.server.ServerFactory;
import org.jepria.server.data.Dao;

public class AuthorizationServerFactory extends ServerFactory<Dao> {

  private AuthorizationServerFactory() {
    super(null, null);
  }

  public static AuthorizationServerFactory getInstance() {
     return new AuthorizationServerFactory();
  }

  public AuthorizationService getService() {
    return new AuthorizationService();
  }

  @Override
  public Dao getDao() {
    throw new UnsupportedOperationException();
  }
}
