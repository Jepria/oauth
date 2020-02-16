package org.jepria.oauth.clienturi;

import org.jepria.oauth.dao.clienturi.ClientUriDaoImpl;
import org.jepria.oauth.model.clienturi.ClientUriService;
import org.jepria.oauth.model.clienturi.dao.ClientUriDao;
import org.jepria.oauth.service.clienturi.ClientUriServiceImpl;
import org.jepria.server.ServerFactory;
import org.jepria.server.service.rest.EntityService;
import org.jepria.server.service.rest.EntityServiceImpl;

public class ClientUriServerFactory extends ServerFactory<ClientUriDao> {

  private static ClientUriServerFactory instance;
  private ClientUriService service;

  private ClientUriServerFactory() {
    super(new ClientUriDaoImpl(), "jdbc/RFInfoDS");
  }

  public static ClientUriServerFactory getInstance() {
    if (instance == null) {
      instance = new ClientUriServerFactory();
    }
    return instance;
  }

  public ClientUriService getService() {
    if (service == null) {
      service = new ClientUriServiceImpl(getDao());
    }
    return service;
  }

  /**
   * @return сервис, воплощающий логику CRUD-операций (create, get-by-id, update, delete)
   */
  public EntityService getEntityService() {
    return new EntityServiceImpl(getDao(), new ClientUriRecordDefinition());
  }

}
