package org.jepria.oauth.session;

import org.jepria.oauth.session.dao.SessionDao;
import org.jepria.server.ServerFactory;
import org.jepria.server.service.rest.PostGetSearchService;
import org.jepria.server.service.rest.PostGetSearchServiceImpl;
import org.jepria.server.service.rest.SearchService;
import org.jepria.server.service.rest.SearchServiceImpl;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.util.function.Supplier;

public class SessionServerFactory extends ServerFactory<SessionDao> {

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
