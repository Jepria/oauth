package org.jepria.oauth.token;

import org.jepria.oauth.token.dao.TokenDao;
import org.jepria.oauth.token.dao.TokenDaoImpl;
import org.jepria.server.ServerFactory;

public class TokenServerFactory extends ServerFactory<TokenDao> {

  private TokenServerFactory() {
    super(new TokenDaoImpl(), "jdbc/RFInfoDS");
  }

  public TokenService getService() {
    return new TokenService();
  }

  public static TokenServerFactory getInstance() {
    return new TokenServerFactory();
  }
}
