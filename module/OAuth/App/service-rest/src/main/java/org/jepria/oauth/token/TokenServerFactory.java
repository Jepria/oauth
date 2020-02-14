package org.jepria.oauth.token;

import org.jepria.oauth.authentication.AuthenticationServerFactory;
import org.jepria.oauth.session.SessionServerFactory;
import org.jepria.oauth.token.dao.TokenDao;
import org.jepria.oauth.token.dao.TokenDaoImpl;
import org.jepria.server.ServerFactory;

public class TokenServerFactory extends ServerFactory<TokenDao> {

  private static TokenServerFactory instance;
  private TokenService service;

  private TokenServerFactory() {
    super(new TokenDaoImpl(), "jdbc/RFInfoDS");
  }

  public static TokenServerFactory getInstance() {
    if (instance == null) {
      instance = new TokenServerFactory();
    }
    return instance;
  }

  public TokenService getService() {
    if (service == null) {
      service = new TokenService(AuthenticationServerFactory.getInstance().getService(), SessionServerFactory.getInstance().getService());
    }
    return service;
  }
}
