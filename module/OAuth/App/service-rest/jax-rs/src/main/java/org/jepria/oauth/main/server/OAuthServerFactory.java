package org.jepria.oauth.main.server;

import org.jepria.server.env.EnvironmentPropertySupport;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import static org.jepria.compat.server.JepRiaServerConstant.BACK_UP_DATA_SOURCE;
import static org.jepria.compat.server.JepRiaServerConstant.DEFAULT_DATA_SOURCE_JNDI_NAME;

public class OAuthServerFactory<D> {
  @Context
  private HttpServletRequest request;
  private final D dao;
  private D proxyDao;
  private String dataSourceJndiName;
  private String moduleName;

  public OAuthServerFactory(D dao, String dataSourceJndiName) {
    this.dao = dao;
    this.dataSourceJndiName = dataSourceJndiName;
  }

  public D getDao() {
    String dataSource = EnvironmentPropertySupport.getInstance(request).getProperty(BACK_UP_DATA_SOURCE, DEFAULT_DATA_SOURCE_JNDI_NAME);
    if (this.proxyDao == null) {
      this.proxyDao = OAuthTransactionFactory.createProxy(this.dao, this.dataSourceJndiName, dataSource, this.moduleName);
    }
    return this.proxyDao;
  }

  public String getDataSourceJndiName() {
    return this.dataSourceJndiName;
  }

  public String getModuleName() {
    return this.moduleName;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }
}
