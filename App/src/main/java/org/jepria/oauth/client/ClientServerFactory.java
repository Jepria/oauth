package org.jepria.oauth.client;

import org.jepria.oauth.client.dao.ClientDao;
import org.jepria.oauth.client.dao.ClientDaoImpl;
import org.jepria.server.ServerFactory;
import org.jepria.server.service.rest.EntityService;
import org.jepria.server.service.rest.EntityServiceImpl;
import org.jepria.server.service.rest.SearchService;
import org.jepria.server.service.rest.SearchServiceImpl;

import javax.servlet.http.HttpSession;
import java.util.function.Supplier;

public class ClientServerFactory extends ServerFactory<ClientDao> {

  private ClientServerFactory() {
    super(new ClientDaoImpl(), "jdbc/RFInfoDS");
  }

  public static ClientServerFactory getInstance() {
    return new ClientServerFactory();
  }

  public ClientService getService() {
    return new ClientService();
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
