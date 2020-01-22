package org.jepria.oauth.authentication;

import org.jepria.oauth.authentication.dao.AuthenticationDao;
import org.jepria.oauth.authentication.dao.AuthenticationDaoImpl;
import org.jepria.server.ServerFactory;

public class AuthenticationServerFactory extends ServerFactory<AuthenticationDao> {

  private AuthenticationServerFactory() {
    super(new AuthenticationDaoImpl(), "jdbc/RFInfoDS");
  }

  public static AuthenticationServerFactory getInstance() {
    return new AuthenticationServerFactory();
  }

  public AuthenticationService getService() {
    return new AuthenticationService();
  }
}
