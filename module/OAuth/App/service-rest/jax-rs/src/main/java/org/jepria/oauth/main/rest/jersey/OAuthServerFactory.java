package org.jepria.oauth.main.rest.jersey;

import org.jepria.compat.server.dao.transaction.TransactionFactory;
import org.jepria.compat.server.db.Db;
import org.jepria.server.ServerFactory;

import java.sql.CallableStatement;

import static org.jepria.compat.server.JepRiaServerConstant.DEFAULT_DATA_SOURCE_JNDI_NAME;

/**
 * if priority DataSource OAuthDS is not available, change to RFInfoDS
 * @param <D>
 */
public class OAuthServerFactory<D> extends ServerFactory<D> {

  private D proxyDao;
  private D dao;

  public OAuthServerFactory(D dao, String dataSourceJndiName) {
    super(dao, dataSourceJndiName);
    this.dao = dao;
  }

  public void testConnection() throws IllegalStateException {
    Db db = new Db(getDataSourceJndiName());
    String sqlQuery = "select 1 from dual";
    try (CallableStatement cs = db.prepare(sqlQuery)){
      cs.execute();
    } catch (Throwable ex) {
      throw new IllegalStateException(getDataSourceJndiName() + " is not available, switching to RFInfoDS", ex);
    } finally {
      db.closeAll();
    }
  }

  public D getDao() {
    try {
      testConnection();
      return super.getDao();
    } catch (IllegalStateException ex) {
      if (this.proxyDao == null) {
        this.proxyDao = TransactionFactory.createProxy(this.dao, DEFAULT_DATA_SOURCE_JNDI_NAME, getModuleName());
      }
      return this.proxyDao;
    }
  }
}
