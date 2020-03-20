package org.jepria.oauth.key;

import org.jepria.oauth.dao.key.KeyDaoImpl;
import org.jepria.oauth.model.key.KeyService;
import org.jepria.oauth.model.key.dao.KeyDao;
import org.jepria.oauth.service.key.KeyServiceImpl;
import org.jepria.server.ServerFactory;

public class KeyServerFactory extends ServerFactory<KeyDao> {

  private static KeyServerFactory instance;
  private KeyService service;

  private KeyServerFactory() {
    super(new KeyDaoImpl(), "jdbc/RFInfoDS");
  }

  public static KeyServerFactory getInstance() {
    if (instance == null) {
      instance = new KeyServerFactory();
    }
    return instance;
  }

  public KeyService getService() {
    if (service == null) {
      service = new KeyServiceImpl(getDao());
    }
    return service;
  }
}
