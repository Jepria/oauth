package org.jepria.oauth.authentication.dao;

import org.jepria.compat.server.dao.CallContext;
import org.jepria.compat.server.dao.ResultSetMapper;
import org.jepria.compat.server.db.Db;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.server.data.DaoSupport;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class AuthenticationDaoImpl implements AuthenticationDao {

  @Override
  public Integer loginByPassword(String username, String password) {

    Integer result = null;
    String sqlQuery =
        " begin"
            + "  ? := pkg_Operator.Login("
            + " operatorLogin => ?"
            + ", password => ?"
            + ");"
            + "  ? := pkg_Operator.GetCurrentUserID;"
            + " end;";
    Db db = CallContext.getDb();
    try {
      CallableStatement callableStatement = db.prepare(sqlQuery);
      // Установим Логин.
      callableStatement.setString(2, username);
      // Установим Пароль.
      callableStatement.setString(3, password);

      callableStatement.registerOutParameter(1, Types.VARCHAR);
      callableStatement.registerOutParameter(4, Types.INTEGER);

      callableStatement.execute();

      result = callableStatement.getInt(4);
      if (callableStatement.wasNull())
        result = null;

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    } finally {
      db.closeStatement(sqlQuery);
    }

    return result;
  }

  @Override
  public Integer loginByHash(String username, String passwordHash) {
    Integer result = null;
    String sqlQuery =
        " begin"
            + "  ? := pkg_Operator.Login("
            + " operatorLogin => ?"
            + ", passwordHash => ?"
            + ");"
            + "  ? := pkg_Operator.GetCurrentUserID;"
            + " end;";
    Db db = CallContext.getDb();
    try {
      CallableStatement callableStatement = db.prepare(sqlQuery);
      // Установим Логин.
      callableStatement.setString(2, username);
      // Установим Пароль.
      callableStatement.setString(3, passwordHash);

      callableStatement.registerOutParameter(1, Types.VARCHAR);
      callableStatement.registerOutParameter(4, Types.INTEGER);

      callableStatement.execute();

      result = callableStatement.getInt(4);
      if (callableStatement.wasNull())
        result = null;

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    } finally {
      db.closeStatement(sqlQuery);
    }

    return result;
  }
  
  @Override
  public Integer loginByClientSecret(String clientId, String clientSecret) {
    String sqlQuery =
      "begin  "
        + "? := pkg_OAuth.verifyClientCredentials("
        + "clientShortName => ? "
        + ", clientSecret => ? "
        + ");"
        + " end;";
    return DaoSupport.getInstance().executeAndReturn(sqlQuery
      , Integer.class
      , clientId
      , clientSecret);
  }
  
  @Override
  public Boolean verifyPKCE(String authorizationCode, String codeVerifier) {
    String sqlQuery =
      "begin  "
        + "? := pkg_OAuth.findSession("
        + "sessionId => ? "
        + ", authCode => ? "
        + ", clientShortName => ? "
        + ", redirectUri => ? "
        + ", operatorId => ? "
        + ", codeChallenge => '' "
        + ", accessToken => ? "
        + ", refreshToken => ? "
        + ", sessionToken => ? "
        + ", operatorIdIns => ? "
        + ");"
        + " end;";
    List<SessionDto> records = null;
    records = DaoSupport.getInstance().find(sqlQuery,
      new ResultSetMapper<SessionDto>() {
        @Override
        public void map(ResultSet rs, SessionDto dto) throws SQLException {
          dto.setSessionId(rs.getString("SESSION_ID"));
          dto.setCodeChallenge(rs.getString("CODE_CHALLENGE"));
        }
      }
      , SessionDto.class
      , null
      , authorizationCode
      , null
      , null
      , null
      , null
      , null
      , null
      , 1);
    try {
      if (records.size() == 1) {
        MessageDigest cryptoProvider = MessageDigest.getInstance("SHA-256");
        byte[] hash = cryptoProvider.digest(codeVerifier.getBytes());
        
        StringBuffer hexString = new StringBuffer();
  
        for (int i = 0; i < hash.length; i++) {
          String hex = Integer.toHexString(0xff & hash[i]);
          if (hex.length() == 1) hexString.append('0');
          hexString.append(hex);
        }
  
        if (records.get(0)
          .getCodeChallenge()
          .equals(hexString.toString())) {
          return Boolean.TRUE;
        }
      } else {
        return Boolean.FALSE;
      }
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      /**
       * Никогда не должно случится.
       */
    }
    return Boolean.FALSE;
  }
}
