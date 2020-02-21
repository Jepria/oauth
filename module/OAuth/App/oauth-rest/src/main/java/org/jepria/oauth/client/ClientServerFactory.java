package org.jepria.oauth.client;

import org.jepria.oauth.dao.client.ClientDaoImpl;
import org.jepria.oauth.model.client.ClientService;
import org.jepria.oauth.model.client.dao.ClientDao;
import org.jepria.oauth.service.client.ClientServiceImpl;
import org.jepria.server.ServerFactory;
import org.jepria.server.service.rest.EntityService;
import org.jepria.server.service.rest.EntityServiceImpl;
import org.jepria.server.service.rest.SearchService;
import org.jepria.server.service.rest.SearchServiceImpl;

import javax.servlet.http.HttpSession;
import java.util.function.Supplier;

public class ClientServerFactory extends ServerFactory<ClientDao> {

  private static ClientServerFactory instance;
  private ClientService service;

  private ClientServerFactory() {
    super(new ClientDaoImpl(), "jdbc/RFInfoDS");
  }

  public static ClientServerFactory getInstance() {
    if (instance == null) {
      instance = new ClientServerFactory();
    }
    return instance;
  }

  public ClientService getService() {
    if (service == null) {
      service = new ClientServiceImpl(getDao());
    }
    return service;
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
