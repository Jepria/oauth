package org.jepria.oauth.key;

import org.jepria.oauth.key.dao.KeyDaoImpl;
import org.jepria.oauth.key.dao.KeyDao;
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
