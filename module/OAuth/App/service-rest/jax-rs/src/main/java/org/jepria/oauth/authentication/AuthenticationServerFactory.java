package org.jepria.oauth.authentication;

import org.jepria.oauth.authentication.dao.AuthenticationDao;
import org.jepria.oauth.clienturi.ClientUriServerFactory;
import org.jepria.oauth.key.KeyServerFactory;
import org.jepria.oauth.main.rest.jersey.OAuthServerFactory;
import org.jepria.oauth.session.SessionServerFactory;

import javax.inject.Inject;

public class AuthenticationServerFactory extends OAuthServerFactory<AuthenticationDao> {

  @Inject
  SessionServerFactory sessionServerFactory;
  @Inject
  ClientUriServerFactory clientUriServerFactory;
  @Inject
  KeyServerFactory keyServerFactory;

  @Inject
  public AuthenticationServerFactory(AuthenticationDao dao) {
    super(dao, "jdbc/OAuthDS");
  }

  public AuthenticationService getService() {
    return new AuthenticationServiceImpl(getDao(),
        sessionServerFactory.getService(),
        clientUriServerFactory.getService(),
        keyServerFactory.getService());
  }
}
