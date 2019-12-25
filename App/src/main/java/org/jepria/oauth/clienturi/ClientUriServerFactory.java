package org.jepria.oauth.clienturi;

import org.jepria.oauth.clienturi.dao.ClientUriDao;
import org.jepria.oauth.clienturi.dao.ClientUriDaoImpl;
import org.jepria.server.ServerFactory;
import org.jepria.server.service.rest.EntityService;
import org.jepria.server.service.rest.EntityServiceImpl;
import org.jepria.server.service.rest.SearchService;
import org.jepria.server.service.rest.SearchServiceImpl;

public class ClientUriServerFactory extends ServerFactory<ClientUriDao> {

  private ClientUriServerFactory() {
    super(new ClientUriDaoImpl(), "jdbc/RFInfoDS");
  }

  public static ClientUriServerFactory getInstance() {
    return new ClientUriServerFactory();
  }

  public ClientUriService getService() {
    return new ClientUriService();
  }

  /**
   * @return сервис, воплощающий логику CRUD-операций (create, get-by-id, update, delete)
   */
  public EntityService getEntityService() {
    return new EntityServiceImpl(getDao(), new ClientUriRecordDefinition());
  }

}
