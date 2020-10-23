package org.jepria.oauth.token;

import org.jepria.oauth.client.ClientServerFactory;
import org.jepria.oauth.key.KeyServerFactory;
import org.jepria.oauth.main.server.OAuthServerFactory;
import org.jepria.oauth.session.SessionServerFactory;

import javax.inject.Inject;

public class TokenServerFactory extends OAuthServerFactory {

  @Inject
  SessionServerFactory sessionServerFactory;
  @Inject
  KeyServerFactory keyServerFactory;
  @Inject
  ClientServerFactory clientServerFactory;

  public TokenServerFactory() {
    super(null, "jdbc/OAuthDS");
  }

  public TokenService getService() {
    return new TokenServiceImpl(sessionServerFactory.getService(), keyServerFactory.getService(), clientServerFactory.getService());
  }
}
