package org.jepria.oauth.session;

import org.jepria.oauth.session.dao.SessionDao;
import org.jepria.oauth.session.dao.SessionDaoImpl;
import org.jepria.server.ServerFactory;
import org.jepria.server.data.Dao;
import org.jepria.server.service.rest.SearchService;
import org.jepria.server.service.rest.SearchServiceImpl;

import javax.servlet.http.HttpSession;
import java.util.function.Supplier;

public class SessionServerFactory extends ServerFactory<SessionDao> {

  private static SessionServerFactory instance;

  private SessionServerFactory() {
    super(new SessionDaoImpl(), "jdbc/RFInfoDS");
  }

  public static SessionServerFactory getInstance() {
    if (instance == null) {
      instance = new SessionServerFactory();
    }
    return instance;
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
