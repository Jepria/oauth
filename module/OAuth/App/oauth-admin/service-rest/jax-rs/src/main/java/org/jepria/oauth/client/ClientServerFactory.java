package org.jepria.oauth.client;

import org.jepria.oauth.client.dao.ClientDao;
import org.jepria.server.ServerFactory;
import org.jepria.server.service.rest.EntityService;
import org.jepria.server.service.rest.EntityServiceImpl;
import org.jepria.server.service.rest.SearchService;
import org.jepria.server.service.rest.SearchServiceImpl;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.util.function.Supplier;

public class ClientServerFactory extends ServerFactory<ClientDao> {

  @Inject
  public ClientServerFactory(ClientDao dao) {
    super(dao, "jdbc/OAuthDS");
  }

  public ClientService getService() {
    return new ClientServiceImpl(getDao());
  }

  /**
   * @return сервис, воплощающий логику CRUD-операций (create, get-by-id, update, delete)
   */
  public EntityService getEntityService() {
    return new EntityServiceImpl(getDao(), new ClientRecordDefinition());
  }

  /**
   * @return сервис, воплощающий логику поиска объектов сущности
   */
  public SearchService getSearchService(Supplier<HttpSession> session) {
    return new SearchServiceImpl(getDao(), new ClientRecordDefinition(), session);
  }

}
