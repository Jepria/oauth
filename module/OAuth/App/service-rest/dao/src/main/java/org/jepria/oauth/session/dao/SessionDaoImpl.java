package org.jepria.oauth.session.dao;

import com.technology.jep.jepria.server.dao.ResultSetMapper;
import com.technology.jep.jepria.server.db.Db;
import oracle.jdbc.OracleTypes;
import org.jepria.oauth.session.dto.SessionCreateDto;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDto;
import org.jepria.oauth.session.dto.SessionUpdateDto;
import org.jepria.server.data.Dao;
import org.jepria.server.data.OptionDto;
import org.jepria.server.data.RuntimeSQLException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jepria.oauth.session.SessionFieldNames.*;

public class SessionDaoImpl implements Dao {
  
  private String jndiName = "jdbc/RFInfoDS";
  
  public SessionDaoImpl(){};
  
  public SessionDaoImpl(String jndName) {
    this.jndiName = jndName;
  }

  protected Db getDb() {
    return new Db(jndiName);
  }

  //language=Oracle
  private String findSqlQuery = "declare " +
      "rc sys_refcursor;" +
      "sessionId integer := ?;" +
      "authCode varchar2(64) := ?;" +
      "operatorId integer := ?;" +
      "clientCode varchar2(32) := ?;" +
      "redirectUri varchar2(4000) := ?;" +
      "accessTokenId varchar2(64) := ?;" +
      "refreshTokenId varchar2(64) := ?;" +
      "sessionTokenId varchar2(64) := ?;" +
      "isBlocked integer := ?;" +
      "hasToken integer := ?;" +
    "begin " +
      "open rc for select " +
          "ss.SESSION_ID," +
          "cl.CLIENT_CODE as CLIENT_ID," +
          "cl.CLIENT_NAME," +
          "ss.REDIRECT_URI," +
          "ss.AUTHORIZATION_CODE," +
          "ss.DATE_INS," +
          "ss.SESSION_TOKEN_ID," +
          "ss.SESSION_TOKEN_DATE_INS," +
          "ss.SESSION_TOKEN_DATE_FINISH," +
          "ss.ACCESS_TOKEN_ID," +
          "ss.ACCESS_TOKEN_DATE_FINISH," +
          "ss.ACCESS_TOKEN_DATE_INS," +
          "ss.REFRESH_TOKEN_ID," +
          "ss.REFRESH_TOKEN_DATE_INS," +
          "ss.REFRESH_TOKEN_DATE_FINISH," +
          "ss.OPERATOR_ID," +
          "op.OPERATOR_NAME," +
          "op.LOGIN as OPERATOR_LOGIN," +
          "ss.CODE_CHALLENGE," +
          "ss.IS_BLOCKED " +
        "from OA_SESSION ss " +
          "left join OP_OPERATOR op on ss.OPERATOR_ID = op.OPERATOR_ID  " +
          "left join OA_CLIENT cl on ss.CLIENT_ID = cl.CLIENT_ID " +
        "where " +
          "(ss.SESSION_ID = sessionId or sessionId is null) " +
          "and (ss.AUTHORIZATION_CODE like authCode or authCode is null) " +
          "and (ss.OPERATOR_ID = operatorId or operatorId is null) " +
          "and (cl.CLIENT_CODE like clientCode or clientCode is null) " +
          "and (ss.REDIRECT_URI like redirectUri or redirectUri is null) " +
          "and (ss.SESSION_TOKEN_ID like sessionTokenId or sessionTokenId is null) " +
          "and (ss.REFRESH_TOKEN_ID like refreshTokenId or refreshTokenId is null) " +
          "and ((hasToken = 0 and ss.ACCESS_TOKEN_ID is null) " +
            "or (hasToken = 1 " +
                "and (accessTokenId is null and ss.ACCESS_TOKEN_ID is not null " +
                  "or (accessTokenId is not null and ss.ACCESS_TOKEN_ID like accessTokenId)))" +
            "or hasToken is null) " +
          "and (ss.IS_BLOCKED = isBlocked or isBlocked is null); " +
      "? := rc; " +
    "end;";


  private ResultSetMapper mapper = new ResultSetMapper<SessionDto>() {
    @Override
    public void map(ResultSet rs, SessionDto dto) throws SQLException {
      dto.setSessionId(getInteger(rs, SESSION_ID));
      dto.setAuthorizationCode(rs.getString(AUTHORIZATION_CODE));
      dto.setDateIns(getTimestamp(rs, DATE_INS));
      dto.setRedirectUri(rs.getString(REDIRECT_URI));
      OptionDto<Integer> operator = new OptionDto<>();
      operator.setName(rs.getString(OPERATOR_NAME));
      operator.setValue(getInteger(rs, OPERATOR_ID));
      dto.setOperator(operator.getValue() != null ? operator : null);
      dto.setOperatorLogin(rs.getString(OPERATOR_LOGIN));
      OptionDto<String> client = new OptionDto<>();
      client.setValue(rs.getString(CLIENT_ID));
      client.setName(rs.getString(CLIENT_NAME));
      dto.setClient(client);
      dto.setAccessTokenId(rs.getString(ACCESS_TOKEN_ID));
      dto.setAccessTokenDateIns(getTimestamp(rs, ACCESS_TOKEN_DATE_INS));
      dto.setAccessTokenDateFinish(getTimestamp(rs, ACCESS_TOKEN_DATE_FINiSH));
      dto.setBlocked(getBoolean(rs, IS_BLOCKED));
      dto.setSessionTokenId(rs.getString(SESSION_TOKEN_ID));
      dto.setSessionTokenDateIns(getTimestamp(rs, SESSION_TOKEN_DATE_INS));
      dto.setSessionTokenDateFinish(getTimestamp(rs, SESSION_TOKEN_DATE_FINISH));
      dto.setRefreshTokenId(rs.getString(REFRESH_TOKEN_ID));
      dto.setRefreshTokenDateIns(getTimestamp(rs, REFRESH_TOKEN_DATE_INS));
      dto.setRefreshTokenDateFinish(getTimestamp(rs, REFRESH_TOKEN_DATE_FINISH));
      dto.setCodeChallenge(rs.getString(CODE_CHALLENGE));
    }
  };

  @Override
  public List<SessionDto> find(Object template, Integer operatorId) {
    SessionSearchDto dto = (SessionSearchDto) template;
    Db db = getDb();
    CallableStatement statement = db.prepare(findSqlQuery);
    List<SessionDto> result = new ArrayList<>();
    try {
      if (dto.getSessionId() != null) statement.setInt(1, dto.getSessionId());
      else statement.setNull(1, OracleTypes.INTEGER);
      statement.setString(2, dto.getAuthorizationCode());
      if (dto.getOperatorId() != null) statement.setInt(3, dto.getOperatorId());
      else statement.setNull(3, OracleTypes.INTEGER);
      statement.setString(4, dto.getClientId());
      statement.setString(5, dto.getRedirectUri());
      statement.setString(6, dto.getAccessTokenId());
      statement.setString(7, dto.getRefreshTokenId());
      statement.setString(8, dto.getSessionTokenId());
      if (dto.getBlocked() != null) statement.setInt(9, dto.getBlocked() ? 1 : 0);
      else statement.setNull(9, OracleTypes.INTEGER);
      if (dto.getHasToken() != null) statement.setInt(10, dto.getHasToken() ? 1 : 0);
      else statement.setNull(10, OracleTypes.INTEGER);
      statement.registerOutParameter(11, OracleTypes.CURSOR);
      statement.executeQuery();

      try (ResultSet rs = (ResultSet) statement.getObject(11)) {
        while (rs.next()) {
          SessionDto resultDto = new SessionDto();
          mapper.map(rs, resultDto);
          result.add(resultDto);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeSQLException(e);
    } catch (Throwable th) {
      th.printStackTrace();
      throw th;
    } finally {
      db.closeAll();
    }
    return result;
  }

  @Override
  public List<SessionDto> findByPrimaryKey(Map<String, ?> primaryKeyMap, Integer operatorId) {
    Db db = getDb();
    CallableStatement statement = db.prepare(findSqlQuery);
    List<SessionDto> result = new ArrayList<>();
    try {
      statement.setInt(1, (Integer) primaryKeyMap.get(SESSION_ID));
      statement.setNull(2, OracleTypes.VARCHAR);
      statement.setNull(3, OracleTypes.INTEGER);
      statement.setNull(4, OracleTypes.VARCHAR);
      statement.setNull(5, OracleTypes.VARCHAR);
      statement.setNull(6, OracleTypes.VARCHAR);
      statement.setNull(7, OracleTypes.VARCHAR);
      statement.setNull(8, OracleTypes.VARCHAR);
      statement.setNull(9, OracleTypes.INTEGER);
      statement.setNull(10, OracleTypes.INTEGER);
      statement.registerOutParameter(11, OracleTypes.CURSOR);
      statement.executeQuery();

      try (ResultSet rs = (ResultSet) statement.getObject(11)) {
        while (rs.next()) {
          SessionDto resultDto = new SessionDto();
          mapper.map(rs, resultDto);
          result.add(resultDto);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeSQLException(e);
    } catch (Throwable th) {
      th.printStackTrace();
      throw th;
    } finally {
      db.closeAll();
    }
    return result;
  }

  @Override
  public Object create(Object record, Integer operatorId) {
    Db db = getDb();
    SessionCreateDto dto = (SessionCreateDto) record;
    //language=Oracle
    String insertSqlQuery = "declare " +
        "authCode varchar2(64) := ?;" +
        "redirectUri varchar2(4000) := ?;" +
        "clientId varchar(32) := ?;" +
        "operatorId integer := ?;" +
        "accessTokenId varchar2(50) := ?;" +
        "accessTokenDateIns date := ?;" +
        "accessTokenDateFinish date := ?;" +
        "sessionId varchar2(50) := ?;" +
        "sessionDateIns date := ?;" +
        "sessionDateFinish date := ?;" +
        "refreshTokenId varchar2(50) := ?;" +
        "refreshTokenDateIns date := ?;" +
        "refreshTokenDateFinish date := ?;" +
        "codeChallenge varchar2(128) := ?;" +
        "clientCount integer;" +
        "err_num NUMBER;" +
        "err_msg VARCHAR2(100);" +
      "begin " +
        "select count(*) into clientCount from OA_CLIENT cl where cl.CLIENT_CODE like clientId; " +
        "if clientId is not null and clientCount <> 1 then " +
          "raise_application_error(-20001, 'Нет клиентского приложения с указанным ID'); " +
        "end if;" +
        "insert into OA_SESSION(" +
          "AUTHORIZATION_CODE, " +
          "REDIRECT_URI, " +
          "CLIENT_ID, " +
          "OPERATOR_ID, " +
          "ACCESS_TOKEN_ID, " +
          "ACCESS_TOKEN_DATE_INS, " +
          "ACCESS_TOKEN_DATE_FINISH, " +
          "SESSION_TOKEN_ID," +
          "SESSION_TOKEN_DATE_INS, " +
          "SESSION_TOKEN_DATE_FINISH, " +
          "REFRESH_TOKEN_ID, " +
          "REFRESH_TOKEN_DATE_INS, " +
          "REFRESH_TOKEN_DATE_FINISH, " +
          "CODE_CHALLENGE)" +
        "values (" +
          "authCode, " +
          "redirectUri, " +
          "(select cl.CLIENT_ID from OA_CLIENT cl where cl.CLIENT_CODE like clientId), " +
          "operatorId, " +
          "accessTokenId, " +
          "accessTokenDateIns, " +
          "accessTokenDateFinish, " +
          "sessionId, " +
          "sessionDateIns, " +
          "sessionDateFinish, " +
          "refreshTokenId, " +
          "refreshTokenDateIns, " +
          "refreshTokenDateFinish, " +
          "codeChallenge); " +
        "? := OA_SESSION_SEQ.currval;" +
      "exception " +
        "when others then " +
          "err_num := SQLCODE; " +
          "err_msg := SUBSTR(SQLERRM, 1, 100); " +
          "if instr(err_msg, 'OA_SESSION_FK_CLIENT_URI') <> 0 then " +
            "raise_application_error(-20002, concat('Указанный URI не содержится в WHITELIST ', redirectUri)); " +
          "end if; " +
          "if instr(err_msg, 'OA_SESSION_FK_CLIENT') <> 0 or err_num = -20001 then " +
            "raise_application_error(-20001, 'Нет клиентского приложения с указанным ID'); " +
          "end if; " +
          "raise_application_error(-20150, err_msg); " +
      "end ;";
    CallableStatement insertStatement = db.prepare(insertSqlQuery);
    Integer result;
    try {
      insertStatement.setString(1, dto.getAuthorizationCode());
      insertStatement.setString(2, dto.getRedirectUri());
      insertStatement.setString(3, dto.getClientId());
      if (dto.getOperatorId() != null) {
        insertStatement.setInt(4, dto.getOperatorId());
      } else {
        insertStatement.setNull(4, Types.INTEGER);
      }
      insertStatement.setString(5, dto.getAccessTokenId());
      if (dto.getAccessTokenDateIns() != null) {
        insertStatement.setTimestamp(6, new Timestamp(dto.getAccessTokenDateIns().getTime()));
      } else {
        insertStatement.setNull(6, Types.DATE);
      }
      if (dto.getAccessTokenDateFinish() != null) {
        insertStatement.setTimestamp(7, new Timestamp(dto.getAccessTokenDateFinish().getTime()));
      } else {
        insertStatement.setNull(7, Types.DATE);
      }
      insertStatement.setString(8, dto.getSessionTokenId());
      if (dto.getSessionTokenDateIns() != null) {
        insertStatement.setTimestamp(9, new Timestamp(dto.getSessionTokenDateIns().getTime()));
      } else {
        insertStatement.setNull(9, Types.DATE);
      }
      if (dto.getSessionTokenDateFinish() != null) {
        insertStatement.setTimestamp(10, new Timestamp(dto.getSessionTokenDateFinish().getTime()));
      } else {
        insertStatement.setNull(10, Types.DATE);
      }
      insertStatement.setString(11, dto.getRefreshTokenId());
      if (dto.getRefreshTokenDateIns() != null) {
        insertStatement.setTimestamp(12, new Timestamp(dto.getRefreshTokenDateIns().getTime()));
      } else {
        insertStatement.setNull(12, Types.DATE);
      }
      if (dto.getRefreshTokenDateFinish() != null) {
        insertStatement.setTimestamp(13, new Timestamp(dto.getRefreshTokenDateFinish().getTime()));
      } else {
        insertStatement.setNull(13, Types.DATE);
      }
      insertStatement.setString(14, dto.getCodeChallenge());
      insertStatement.registerOutParameter(15, OracleTypes.INTEGER);
      insertStatement.execute();
      result = (Integer) insertStatement.getObject(15);
    } catch (SQLException e) {
      e.printStackTrace();
      db.rollback();
      throw new RuntimeSQLException(e);
    } catch (Throwable th) {
      th.printStackTrace();
      db.rollback();
      throw th;
    } finally {
      db.closeAll();
    }
    return result;
  }

  @Override
  public void update(Map<String, ?> primaryKey, Object record, Integer operatorId) {
    Db db = getDb();
    SessionUpdateDto dto = (SessionUpdateDto) record;
    //language=Oracle
    String updateSqlQuery = "UPDATE OA_SESSION t " +
      "SET " +
        "OPERATOR_ID = ?, " +
        "ACCESS_TOKEN_ID = ?, " +
        "ACCESS_TOKEN_DATE_INS = ?, " +
        "ACCESS_TOKEN_DATE_FINISH = ?, " +
        "SESSION_TOKEN_ID = ?," +
        "SESSION_TOKEN_DATE_INS = ?, " +
        "SESSION_TOKEN_DATE_FINISH = ?, " +
        "REFRESH_TOKEN_ID = ?, " +
        "REFRESH_TOKEN_DATE_INS = ?, " +
        "REFRESH_TOKEN_DATE_FINISH = ?, " +
        "IS_BLOCKED = ? " +
      "WHERE t.SESSION_ID = ?";
    CallableStatement updateStatement = db.prepare(updateSqlQuery);
    try {
      if (dto.getOperatorId() != null) {
        updateStatement.setInt(1, dto.getOperatorId());
      } else {
        updateStatement.setNull(1, Types.INTEGER);
      }
      updateStatement.setString(2, dto.getAccessTokenId());
      if (dto.getAccessTokenDateIns() != null) {
        updateStatement.setTimestamp(3, new Timestamp(dto.getAccessTokenDateIns().getTime()));
      } else {
        updateStatement.setNull(3, Types.DATE);
      }
      if (dto.getAccessTokenDateFinish() != null) {
        updateStatement.setTimestamp(4, new Timestamp(dto.getAccessTokenDateFinish().getTime()));
      } else {
        updateStatement.setNull(4, Types.DATE);
      }
      updateStatement.setString(5, dto.getSessionTokenId());
      if (dto.getSessionTokenDateIns() != null) {
        updateStatement.setTimestamp(6, new Timestamp(dto.getSessionTokenDateIns().getTime()));
      } else {
        updateStatement.setNull(6, Types.DATE);
      }
      if (dto.getSessionTokenDateFinish() != null) {
        updateStatement.setTimestamp(7, new Timestamp(dto.getSessionTokenDateFinish().getTime()));
      } else {
        updateStatement.setNull(7, Types.DATE);
      }
      updateStatement.setString(8, dto.getRefreshTokenId());
      if (dto.getRefreshTokenDateIns() != null) {
        updateStatement.setTimestamp(9, new Timestamp(dto.getRefreshTokenDateIns().getTime()));
      } else {
        updateStatement.setNull(9, Types.DATE);
      }
      if (dto.getRefreshTokenDateFinish() != null) {
        updateStatement.setTimestamp(10, new Timestamp(dto.getRefreshTokenDateFinish().getTime()));
      } else {
        updateStatement.setNull(10, Types.DATE);
      }
      updateStatement.setInt(11, dto.getBlocked() != null ? (dto.getBlocked() ? 1 : 0) : 0);
      updateStatement.setInt(12, (Integer) primaryKey.get(SESSION_ID));
      int updatedRecordCount = updateStatement.executeUpdate();
      if (updatedRecordCount == 1) {
        db.commit();
      }
    } catch (SQLException e) {
      e.printStackTrace();
      db.rollback();
      throw new RuntimeSQLException(e);
    } catch (Throwable th) {
      th.printStackTrace();
      db.rollback();
      throw th;
    } finally {
      db.closeAll();
    }
  }

  @Override
  public void delete(Map<String, ?> primaryKey, Integer operatorId) {
    Db db = getDb();
    //language=Oracle
    String updateSqlQuery = "UPDATE OA_SESSION t " +
      "SET IS_BLOCKED = ? " +
      "WHERE t.SESSION_ID = ?";
    CallableStatement updateStatement = db.prepare(updateSqlQuery);
    try {
      updateStatement.setInt(1, 1);
      updateStatement.setInt(2, (Integer) primaryKey.get(SESSION_ID));
      int updatedRecordCount = updateStatement.executeUpdate();
      if (updatedRecordCount == 1) {
        db.commit();
      }
    } catch (SQLException e) {
      e.printStackTrace();
      db.rollback();
      throw new RuntimeSQLException(e);
    } catch (Throwable th) {
      th.printStackTrace();
      db.rollback();
      throw th;
    } finally {
      db.closeAll();
    }
  }
}
