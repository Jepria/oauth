package org.jepria.oauth.authentication;

import org.jepria.oauth.dao.authentication.AuthenticationDaoImpl;
import org.jepria.oauth.model.authentication.AuthenticationService;
import org.jepria.oauth.model.authentication.dao.AuthenticationDao;
import org.jepria.oauth.service.authentication.AuthenticationServiceImpl;
import org.jepria.oauth.session.SessionServerFactory;
import org.jepria.server.ServerFactory;

public class AuthenticationServerFactory extends ServerFactory<AuthenticationDao> {

  private static AuthenticationServerFactory instance;
  private AuthenticationService service;

  private AuthenticationServerFactory() {
    super(new AuthenticationDaoImpl(), "jdbc/RFInfoDS");
  }

  public static AuthenticationServerFactory getInstance() {
    if (instance == null) {
      instance = new AuthenticationServerFactory();
    }
    return instance;
  }

  public AuthenticationService getService() {
    if (service == null) {
      service = new AuthenticationServiceImpl(getDao(), SessionServerFactory.getInstance().getService());
    }
    return service;
  }
}
