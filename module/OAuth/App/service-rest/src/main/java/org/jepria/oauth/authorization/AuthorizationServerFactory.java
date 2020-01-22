package org.jepria.oauth.authorization;

import org.jepria.oauth.authorization.dao.AuthorizationDaoImpl;
import org.jepria.oauth.authorization.dao.AuthorizationDao;
import org.jepria.server.ServerFactory;
import org.jepria.server.service.rest.EntityService;
import org.jepria.server.service.rest.EntityServiceImpl;
import org.jepria.server.service.rest.SearchService;
import org.jepria.server.service.rest.SearchServiceImpl;

import javax.servlet.http.HttpSession;
import java.util.function.Supplier;

public class AuthorizationServerFactory extends ServerFactory<AuthorizationDao> {

  private AuthorizationServerFactory() {
    super(new AuthorizationDaoImpl(), "jdbc/RFInfoDS");
  }

  public static AuthorizationServerFactory getInstance() {
     return new AuthorizationServerFactory();
  }

  public AuthorizationService getService() {
    return new AuthorizationService();
  }

  /**
   * @return сервис, воплощающий логику CRUD-операций (create, get-by-id, update, delete)
   */
  public EntityService getEntityService() {
    return new EntityServiceImpl(getDao(), new AuthorizationRecordDefinition());
  }

  /**
   * @return сервис, воплощающий логику поиска объектов сущности
   */
  public SearchService getSearchService(Supplier<HttpSession> session) {
    return new SearchServiceImpl(getDao(), new AuthorizationRecordDefinition(), session);
  }
}
