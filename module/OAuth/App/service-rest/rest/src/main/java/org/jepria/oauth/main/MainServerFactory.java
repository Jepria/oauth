package org.jepria.oauth.main;

import org.jepria.oauth.main.dao.MainDao;
import org.jepria.oauth.main.dao.MainDaoImpl;
import org.jepria.server.ServerFactory;

public class MainServerFactory extends ServerFactory<MainDao> {

  private MainServerFactory() {
    super(new MainDaoImpl(), "jdbc/RFInfoDS");
  }

  public static MainServerFactory getInstance() {
    return new MainServerFactory();
  }

  public MainService getService() {
    return new MainServiceImpl(getDao());
  }

}
