package org.jepria.oauth.main.server;

import org.jepria.compat.server.dao.transaction.annotation.After;
import org.jepria.compat.server.dao.transaction.annotation.Before;
import org.jepria.compat.server.dao.transaction.handler.EndTransactionHandler;
import org.jepria.compat.server.dao.transaction.handler.EndTransactionHandlerImpl;
import org.jepria.compat.server.dao.transaction.handler.StartTransactionHandler;
import org.jepria.compat.server.dao.transaction.handler.StartTransactionHandlerImpl;
import org.jepria.compat.server.db.Db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Фабрика, создающая прокси для выполнения методов Dao в рамках одной транзакции.
 */
public class OAuthTransactionFactory {

  /**
   * Обработчик вызова метода Dao, обеспечивающий его выполнение в рамках одной транзакции.<br/>
   * Механизм работы следующий:
   * <ul>
   *   <li>Вызывается обработчик старта транзакции.</li>
   *   <li>Вызывается метод Dao, который выполняется в рамках транзакции. При возникновении исключения
   *   оно перехватывается.</li>
   *   <li>Вызывается обработчик завершения транзакции.</li>
   * </ul>
   * @param <D> интерфейс Dao
   */
  private static class TransactionInvocationHandler<D> implements InvocationHandler {

    /**
     * Объект Dao.
     */
    private final D dao;
    /**
     * JNDI-имя источника данных.
     */
    private final String dataSourceJndiName;
    /**
     * JNDI-имя запасного источника данных.
     */
    private final String backupDataSourceJndiName;
    /**
     * Имя модуля для передачи в DB.
     */
    private final String moduleName;

    /**
     * Создаёт экземпляр транзакционного обработчика.
     * @param dao объект Dao
     * @param dataSourceJndiName JNDI-имя источника данных
     * @param backupDataSourceJndiName JNDI-имя источника данных
     * @param moduleName имя модуля для передачи в DB
     */
    public TransactionInvocationHandler(D dao, String dataSourceJndiName,  String backupDataSourceJndiName, String moduleName) {
      this.dao = dao;
      this.dataSourceJndiName = dataSourceJndiName;
      this.backupDataSourceJndiName = backupDataSourceJndiName;
      this.moduleName = moduleName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Object result = null;
      try {
        result = call(method, args, dataSourceJndiName, moduleName);
      } catch (Throwable ex) {
        if (ex.getMessage().contains("DataSource 'java:/comp/env/" + dataSourceJndiName +"' not found")) {
          result = call(method, args, backupDataSourceJndiName, moduleName);
        } else {
          throw ex;
        }
      }
      return result;
    }

    private Object call(Method method, Object[] args, String dataSourceJndiName, String moduleName) throws Throwable {
      Class<?> daoClass = dao.getClass();
      Method implementingMethod = daoClass.getMethod(
          method.getName(), method.getParameterTypes());

      Before before = implementingMethod.getAnnotation(Before.class);
      Class<? extends StartTransactionHandler> startTransactionHandlerClass =
          before != null ? before.startTransactionHandler() : StartTransactionHandlerImpl.class;

      After after = implementingMethod.getAnnotation(After.class);
      Class<? extends EndTransactionHandler> endTransactionHandlerClass =
          after != null ? after.endTransactionHandler() : EndTransactionHandlerImpl.class;

      Db db = startTransactionHandlerClass.newInstance().handle(dataSourceJndiName, moduleName);
      Throwable caught = null;
      Object result = null;
      synchronized (db) {
        try {
          result = method.invoke(dao, args);
        } catch(Exception exc) {
          /*
           * Необходимо вызвать getCause(), поскольку выброшенное из Dao исключение
           * будет обёрнуто в InvocationTargetException.
           */
          caught = exc.getCause();
        }

        endTransactionHandlerClass.newInstance().handle(caught);

        if (caught != null) {
          throw caught;
        }
      }
      return result;
    }
  }


  /**
   * Создаёт прокси для переданного Dao.
   * @param dao объект Dao
   * @param dataSourceJndiName JNDI-имя источника данных
   * @param moduleName имя модуля
   * @return созданный прокси
   */
  @SuppressWarnings("unchecked")
  public static <D> D createProxy(D dao, String dataSourceJndiName, String backupDataSourceJndiName, String moduleName) {
    Class<?> daoClass = dao.getClass();
    return (D) Proxy.newProxyInstance(
        OAuthTransactionFactory.class.getClassLoader(),
        daoClass.getInterfaces(),
        new TransactionInvocationHandler<D>(dao, dataSourceJndiName, backupDataSourceJndiName,moduleName));
  }
}
