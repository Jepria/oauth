package org.jepria.oauth.authorization;

import org.jepria.oauth.authorization.dao.AuthorizationDaoImpl;
import org.jepria.oauth.authorization.dao.AuthorizationDao;
import org.jepria.server.ServerFactory;

public class AuthorizationServerFactory extends ServerFactory<AuthorizationDao> {

  private AuthorizationServerFactory() {
    super(new AuthorizationDaoImpl(), "jdbc/RFInfoDS");
  }

  public static AuthorizationServerFactory getInstance() {
     return new AuthorizationServerFactory();
  }

  public AuthorizationService getService() {
    return new AuthorizationService();
  }
}
