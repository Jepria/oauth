package org.jepria.oauth.dao.authentication;

import com.technology.jep.jepcommon.security.pkg_Operator;
import com.technology.jep.jepria.server.db.Db;
import org.jepria.oauth.model.authentication.dao.AuthenticationDao;
import org.jepria.server.data.RuntimeSQLException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
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
  public Integer loginByClientSecret(String clientId, String clientSecret) {
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
  public Boolean verifyPKCE(String authorizationCode, String codeVerifier) {
    //language=Oracle
    String sqlQuery = "select ss.CODE_CHALLENGE from OA_SESSION ss where ss.AUTHORIZATION_CODE like ?";
    Db db = getDb();
    CallableStatement callableStatement = db.prepare(sqlQuery);
    try {
      callableStatement.setString(1, authorizationCode);
      ResultSet rs = callableStatement.executeQuery();
      List<String> result = new ArrayList<>();
      while (rs.next()) {
        result.add(rs.getString("CODE_CHALLENGE"));
      }
      if (result.size() == 1) {
        MessageDigest cryptoProvider = MessageDigest.getInstance("SHA-256");
        if (result.get(0)
          .equals(Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(cryptoProvider.digest(codeVerifier.getBytes())))) {
          return Boolean.TRUE;
        }
      } else {
        return null;
      }
    } catch (SQLException e) {
      throw new RuntimeSQLException(e);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      /**
       * Никогда не должно случится.
       */
    } finally {
      db.closeAll();
      return Boolean.FALSE;
    }
  }
}
