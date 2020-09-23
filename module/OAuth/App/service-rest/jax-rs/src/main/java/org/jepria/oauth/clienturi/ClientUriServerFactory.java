package org.jepria.oauth.clienturi;

import org.jepria.oauth.clienturi.dao.ClientUriDao;
import org.jepria.server.ServerFactory;
import org.jepria.server.data.Dao;
import org.jepria.server.service.rest.EntityService;
import org.jepria.server.service.rest.EntityServiceImpl;

import javax.inject.Inject;

public class ClientUriServerFactory extends ServerFactory<Dao> {

  @Inject
  public ClientUriServerFactory(ClientUriDao dao) {
    super(dao, "jdbc/RFInfoDS");
  }

  public ClientUriService getService() {
    return new ClientUriServiceImpl(getDao());
  }

  /**
   * @return сервис, воплощающий логику CRUD-операций (create, get-by-id, update, delete)
   */
  public EntityService getEntityService() {
    return new EntityServiceImpl(getDao(), new ClientUriRecordDefinition());
  }

}
