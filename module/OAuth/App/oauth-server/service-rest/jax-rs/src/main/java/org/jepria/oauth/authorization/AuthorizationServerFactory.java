package org.jepria.oauth.authorization;

import org.jepria.oauth.client.ClientServerFactory;
import org.jepria.oauth.key.KeyServerFactory;
import org.jepria.oauth.main.server.OAuthServerFactory;
import org.jepria.oauth.session.SessionServerFactory;
import org.jepria.server.data.Dao;

import javax.inject.Inject;

public class AuthorizationServerFactory extends OAuthServerFactory<Dao> {

  @Inject
  SessionServerFactory sessionServerFactory;
  @Inject
  ClientServerFactory clientServerFactory;
  @Inject
  KeyServerFactory keyServerFactory;

  public AuthorizationServerFactory() {
    super(null, null);
  }

  public AuthorizationService getService() {
    return new AuthorizationServiceImpl(sessionServerFactory.getService(),
        clientServerFactory.getService(),
        keyServerFactory.getService());
  }

  @Override
  public Dao getDao() {
    throw new UnsupportedOperationException();
  }
}
