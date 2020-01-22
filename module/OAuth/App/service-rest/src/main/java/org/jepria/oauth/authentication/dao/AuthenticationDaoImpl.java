package org.jepria.oauth.authentication.dao;

import com.technology.jep.jepcommon.security.pkg_Operator;
import com.technology.jep.jepria.server.db.Db;
import org.jepria.oauth.authorization.dao.AuthorizationDaoImpl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AuthenticationDaoImpl implements AuthenticationDao {

  private Db db;

  protected Db getDb() {
    if (db == null) {
      db = new Db("jdbc/RFInfoDS");
    }
    return db;
  }

  @Override
  public Integer loginByPassword(String username, String password) {
    Integer operatorId = null;
    try {
      operatorId = pkg_Operator.logon(getDb(), username, password, null);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return operatorId;
  }

  @Override
  public List<?> find(Object template, Integer operatorId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<?> findByPrimaryKey(Map<String, ?> primaryKeyMap, Integer operatorId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object create(Object record, Integer operatorId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void update(Map<String, ?> primaryKey, Object record, Integer operatorId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(Map<String, ?> primaryKey, Integer operatorId) {
    throw new UnsupportedOperationException();
  }
}
