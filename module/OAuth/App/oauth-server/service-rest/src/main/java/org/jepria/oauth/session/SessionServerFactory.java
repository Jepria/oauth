package org.jepria.oauth.session;

import org.jepria.oauth.key.KeyServerFactory;
import org.jepria.oauth.main.server.OAuthServerFactory;
import org.jepria.oauth.session.dao.SessionDao;

import javax.inject.Inject;

public class SessionServerFactory extends OAuthServerFactory<SessionDao> {

  @Inject
  KeyServerFactory keyServerFactory;
  
  @Inject
  public SessionServerFactory(SessionDao dao) {
    super(dao, "jdbc/OAuthDS");
  }

  public LoginConfirmService getService() {
    return new LoginConfirmService(getDao(), keyServerFactory.getService());
  }

}
