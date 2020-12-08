package org.jepria.oauth;

import oracle.jdbc.pool.OracleConnectionPoolDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public abstract class DaoTestBase {
  protected static InitialContext ic;
  protected static Properties properties;

  public static InitialContext prepareInitialContextForJdbc() throws NamingException {
    System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
    System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");

    InitialContext ic = new InitialContext();
    ic.createSubcontext("java:");
    ic.createSubcontext("java:/comp");
    ic.createSubcontext("java:/comp/env");
    ic.createSubcontext("java:/comp/env/jdbc");

    return ic;
  }

  @BeforeAll
  public static void initialize() throws IOException, SQLException, NamingException {
    ic = prepareInitialContextForJdbc();
    properties = new Properties();
    properties.load(DaoTestBase.class.getClassLoader().getResourceAsStream("database.properties"));
    OracleConnectionPoolDataSource dsPool = new OracleConnectionPoolDataSource();
    dsPool.setURL(properties.getProperty("datasource.url"));
    dsPool.setUser(properties.getProperty("datasource.username"));
    dsPool.setPassword(properties.getProperty("datasource.password"));

    ic.rebind(properties.getProperty("datasource.jndiName"), dsPool);
  }

  @AfterAll
  public static void destroy() throws NamingException {
    ic.close();
    ic = null;
  }
}
