package org.jepria.oauth.session;

import org.jepria.oauth.main.server.OAuthServerFactory;
import org.jepria.oauth.session.dao.SessionDao;

import javax.inject.Inject;

public class SessionServerFactory extends OAuthServerFactory<SessionDao> {

  @Inject
  public SessionServerFactory(SessionDao dao) {
    super(dao, "jdbc/OAuthDS");
  }

  public SessionService getService() {
    return new SessionServiceImpl(getDao(), new SessionRecordDefinition());
  }

}
