package org.jepria.oauth.session;

import org.jepria.oauth.dao.session.SessionDaoImpl;
import org.jepria.oauth.model.session.SessionService;
import org.jepria.oauth.model.session.dao.SessionDao;
import org.jepria.oauth.service.session.SessionServiceImpl;
import org.jepria.server.ServerFactory;
import org.jepria.server.service.rest.EntityService;
import org.jepria.server.service.rest.EntityServiceImpl;
import org.jepria.server.service.rest.SearchService;
import org.jepria.server.service.rest.SearchServiceImpl;

import javax.servlet.http.HttpSession;
import java.util.function.Supplier;

public class SessionServerFactory extends ServerFactory<SessionDao> {

  private static SessionServerFactory instance;
  private SessionService service;

  private SessionServerFactory() {
    super(new SessionDaoImpl(), "jdbc/RFInfoDS");
  }

  public static SessionServerFactory getInstance() {
    if (instance == null) {
      instance =  new SessionServerFactory();
    }
    return instance;
  }

  public SessionService getService() {
    if (service == null) {
      service = new SessionServiceImpl(getEntityService(), getDao());
    }
    return service;
  }

  public EntityService getEntityService() {
    return new EntityServiceImpl(getDao(), new SessionRecordDefinition());
  }

  public SearchService getSearchService(Supplier<HttpSession> session) {
    return new SearchServiceImpl(getDao(), new SessionRecordDefinition(), session);
  }
}
