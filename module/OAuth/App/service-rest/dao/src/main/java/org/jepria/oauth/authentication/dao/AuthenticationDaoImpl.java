package org.jepria.oauth.authentication.dao;

import org.jepria.compat.server.dao.ResultSetMapper;
import org.jepria.compat.server.db.Db;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.server.data.DaoSupport;
import org.jepria.server.service.security.pkg_Operator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

import static org.jepria.compat.server.JepRiaServerConstant.DEFAULT_DATA_SOURCE_JNDI_NAME;
import static org.jepria.oauth.session.SessionFieldNames.CODE_CHALLENGE;
import static org.jepria.oauth.session.SessionFieldNames.SESSION_ID;

public class AuthenticationDaoImpl implements AuthenticationDao {

  protected Db getDb() {
    return new Db(DEFAULT_DATA_SOURCE_JNDI_NAME);
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
          dto.setSessionId(getInteger(rs, SESSION_ID));
          dto.setCodeChallenge(rs.getString(CODE_CHALLENGE));
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
        if (records.get(0)
          .getCodeChallenge()
            .equals(Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(cryptoProvider.digest(codeVerifier.getBytes())))) {
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
