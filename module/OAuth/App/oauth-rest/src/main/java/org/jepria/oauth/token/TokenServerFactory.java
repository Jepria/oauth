package org.jepria.oauth.token;

import org.jepria.oauth.authentication.AuthenticationServerFactory;
import org.jepria.oauth.dao.token.TokenDaoImpl;
import org.jepria.oauth.key.KeyServerFactory;
import org.jepria.oauth.model.token.TokenService;
import org.jepria.oauth.model.token.dao.TokenDao;
import org.jepria.oauth.service.token.TokenServiceImpl;
import org.jepria.oauth.session.SessionServerFactory;
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
      service = new TokenServiceImpl(AuthenticationServerFactory.getInstance().getService(),
          SessionServerFactory.getInstance().getService(),
          KeyServerFactory.getInstance().getService());
    }
    return service;
  }
}
