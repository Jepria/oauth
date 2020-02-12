package org.jepria.oauth.session;

import org.jepria.oauth.session.dao.SessionDao;
import org.jepria.oauth.session.dao.SessionDaoImpl;
import org.jepria.server.ServerFactory;
import org.jepria.server.service.rest.EntityService;
import org.jepria.server.service.rest.EntityServiceImpl;
import org.jepria.server.service.rest.SearchService;
import org.jepria.server.service.rest.SearchServiceImpl;

import javax.servlet.http.HttpSession;
import java.util.function.Supplier;

public class SessionServerFactory extends ServerFactory<SessionDao> {

  private SessionServerFactory() {
    super(new SessionDaoImpl(), "jdbc/RFInfoDS");
  }

  public static SessionServerFactory getInstance() {
     return new SessionServerFactory();
  }

  public SessionService getService() {
    return new SessionService();
  }

  /**
   * @return сервис, воплощающий логику CRUD-операций (create, get-by-id, update, delete)
   */
  public EntityService getEntityService() {
    return new EntityServiceImpl(getDao(), new SessionRecordDefinition());
  }

  /**
   * @return сервис, воплощающий логику поиска объектов сущности
   */
  public SearchService getSearchService(Supplier<HttpSession> session) {
    return new SearchServiceImpl(getDao(), new SessionRecordDefinition(), session);
  }
}
