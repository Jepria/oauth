package org.jepria.oauth.authentication.dao;

import com.technology.jep.jepcommon.security.pkg_Operator;
import com.technology.jep.jepria.server.db.Db;
import org.jepria.server.data.RuntimeSQLException;

import javax.security.auth.login.LoginException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthenticationDaoImpl implements AuthenticationDao {

  protected Db getDb() {
    return new Db("jdbc/RFInfoDS");
  }

  @Override
  public Integer loginByPassword(String username, String password) {
    Integer operatorId = null;
    Db db = getDb();
    try {
      operatorId = pkg_Operator.logon(db, username, password, null);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      db.closeAll();
    }
    return operatorId;
  }

  @Override
  public Integer loginByClientCredentials(String clientId, String clientSecret) {
    //language=Oracle
    String sqlQuery = "select cl.client_id from OA_CLIENT cl where cl.CLIENT_CODE like ? and cl.CLIENT_SECRET like ?";
    Db db = getDb();
    CallableStatement callableStatement = db.prepare(sqlQuery);
    try {
      callableStatement.setString(1, clientId);
      callableStatement.setString(2, clientSecret);
      ResultSet rs = callableStatement.executeQuery();
      List<Integer> result = new ArrayList<>();
      while (rs.next()) {
        result.add(rs.getInt("CLIENT_ID"));
      }
      if (result.size() == 1) {
        return result.get(0);
      } else {
        return null;
      }
    } catch (SQLException e) {
      throw new RuntimeSQLException(e);
    } finally {
      db.closeAll();
    }
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
