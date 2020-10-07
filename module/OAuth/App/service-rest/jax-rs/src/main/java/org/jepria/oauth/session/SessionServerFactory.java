package org.jepria.oauth.session;

import org.jepria.oauth.main.rest.jersey.OAuthServerFactory;
import org.jepria.oauth.session.dao.SessionDao;
import org.jepria.oauth.session.dao.SessionDaoImpl;
import org.jepria.server.ServerFactory;
import org.jepria.server.data.Dao;
import org.jepria.server.service.rest.SearchService;
import org.jepria.server.service.rest.SearchServiceImpl;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.util.function.Supplier;

public class SessionServerFactory extends OAuthServerFactory<SessionDao> {

  @Inject
  public SessionServerFactory(SessionDao dao) {
    super(dao, "jdbc/OAuthDS");
  }

  public SessionService getService() {
    return getEntityService();
  }

  public SessionService getEntityService() {
    return new SessionServiceImpl(getDao(), new SessionRecordDefinition());
  }

  public SearchService getSearchService(Supplier<HttpSession> session) {
    return new SearchServiceImpl(getDao(), new SessionRecordDefinition(), session);
  }
}
