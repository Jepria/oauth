package org.jepria.oauth.authorization.dao;

import com.technology.jep.jepria.server.dao.ResultSetMapper;
import com.technology.jep.jepria.server.db.Db;
import oracle.jdbc.OracleTypes;
import org.jepria.oauth.authorization.dto.AuthRequestCreateDto;
import org.jepria.oauth.authorization.dto.AuthRequestDto;
import org.jepria.oauth.authorization.dto.AuthRequestSearchDtoLocal;
import org.jepria.oauth.authorization.dto.AuthRequestUpdateDto;
import org.jepria.server.data.OptionDto;
import org.jepria.server.data.RuntimeSQLException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jepria.oauth.authorization.AuthorizationFieldNames.*;

public class AuthorizationDaoImpl implements AuthorizationDao {

  protected Db getDb() {
    return new Db("jdbc/RFInfoDS");
  }

  //language=Oracle
  private String findSqlQuery = "declare " +
      "rc sys_refcursor;" +
      "authRequestId integer := ?;" +
      "authCode varchar2(64) := ?;" +
      "operatorId integer := ?;" +
      "clientCode varchar2(32) := ?;" +
      "redirectUri varchar2(4000) := ?;" +
      "tokenId varchar2(64) := ?;" +
      "isBlocked integer := ?;" +
      "hasToken integer := ?;" +
      "sessionId varchar2(64) := ?;" +
    "begin " +
      "open rc for select " +
          "ar.AUTH_REQUEST_ID," +
          "cl.CLIENT_CODE as CLIENT_ID," +
          "cl.CLIENT_NAME," +
          "ar.REDIRECT_URI," +
          "ar.AUTHORIZATION_CODE," +
          "ar.DATE_INS," +
          "ar.SESSION_ID," +
          "ar.TOKEN_ID," +
          "ar.TOKEN_DATE_INS," +
          "ar.OPERATOR_ID," +
          "op.OPERATOR_NAME," +
          "op.LOGIN as OPERATOR_LOGIN," +
          "ar.IS_BLOCKED " +
        "from OA_AUTH_REQUEST ar " +
          "left join OP_OPERATOR op on ar.OPERATOR_ID = op.OPERATOR_ID  " +
          "inner join OA_CLIENT cl on ar.CLIENT_ID = cl.CLIENT_ID " +
        "where " +
          "(ar.AUTH_REQUEST_ID = authRequestId or authRequestId is null) " +
          "and (ar.AUTHORIZATION_CODE like authCode or authCode is null) " +
          "and (ar.OPERATOR_ID = operatorId or operatorId is null) " +
          "and (cl.CLIENT_CODE like clientCode or clientCode is null) " +
          "and (ar.REDIRECT_URI like redirectUri or redirectUri is null) " +
          "and (ar.SESSION_ID like sessionId or sessionId is null) " +
          "and ((hasToken = 0 and (ar.TOKEN_ID like tokenId or tokenId is null)) " +
            "or (hasToken = 1 and ar.TOKEN_ID is not null )) " +
          "and (ar.IS_BLOCKED = isBlocked or isBlocked is null); " +
      "? := rc; " +
    "end;";


  private ResultSetMapper mapper = new ResultSetMapper<AuthRequestDto>() {
    @Override
    public void map(ResultSet rs, AuthRequestDto dto) throws SQLException {
      dto.setAuthRequestId(getInteger(rs, AUTH_REQUEST_ID));
      dto.setAuthorizationCode(rs.getString(AUTHORIZATION_CODE));
      dto.setDateIns(getTimestamp(rs, DATE_INS));
      dto.setRedirectUri(rs.getString(REDIRECT_URI));
      OptionDto<Integer> operator = new OptionDto<>();
      operator.setName(rs.getString(OPERATOR_NAME));
      operator.setValue(getInteger(rs, OPERATOR_ID));
      dto.setOperator(operator);
      dto.setOperatorLogin(rs.getString(OPERATOR_LOGIN));
      OptionDto<String> client = new OptionDto<>();
      client.setValue(rs.getString(CLIENT_ID));
      client.setName(rs.getString(CLIENT_NAME));
      dto.setClient(client);
      dto.setTokenId(rs.getString(TOKEN_ID));
      dto.setTokenDateIns(getTimestamp(rs, TOKEN_DATE_INS));
      dto.setBlocked(getBoolean(rs, IS_BLOCKED));
      dto.setSessionId(rs.getString(SESSION_ID));
    }
  };

  @Override
  public List<AuthRequestDto> find(Object template, Integer operatorId) {
    AuthRequestSearchDtoLocal dto = (AuthRequestSearchDtoLocal) template;
    Db db = getDb();
    CallableStatement statement = db.prepare(findSqlQuery);
    List<AuthRequestDto> result = new ArrayList<>();
    try {
      if (dto.getAuthRequestId() != null) statement.setInt(1, dto.getAuthRequestId());
      else statement.setNull(1, OracleTypes.INTEGER);
      statement.setString(2, dto.getAuthorizationCode());
      if (dto.getOperatorId() != null) statement.setInt(3, dto.getOperatorId());
      else statement.setNull(3, OracleTypes.INTEGER);
      statement.setString(4, dto.getClientId());
      statement.setString(5, dto.getRedirectUri());
      statement.setString(6, dto.getTokenId());
      if (dto.getBlocked() != null) statement.setInt(7, dto.getBlocked() ? 1 : 0);
      else statement.setNull(7, OracleTypes.INTEGER);
      if (dto.getHasToken() != null) statement.setInt(8, dto.getHasToken() ? 1 : 0);
      else statement.setInt(8, 0);
      statement.setString(9, dto.getSessionId());
      statement.registerOutParameter(10, OracleTypes.CURSOR);
      statement.executeQuery();

      try (ResultSet rs = (ResultSet) statement.getObject(10)) {
        while (rs.next()) {
          AuthRequestDto resultDto = new AuthRequestDto();
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
  public List<AuthRequestDto> findByPrimaryKey(Map<String, ?> primaryKeyMap, Integer operatorId) {
    Db db = getDb();
    CallableStatement statement = db.prepare(findSqlQuery);
    List<AuthRequestDto> result = new ArrayList<>();
    try {
      statement.setInt(1, (Integer) primaryKeyMap.get(AUTH_REQUEST_ID));
      statement.setNull(2, OracleTypes.VARCHAR);
      statement.setNull(3, OracleTypes.INTEGER);
      statement.setNull(4, OracleTypes.VARCHAR);
      statement.setNull(5, OracleTypes.VARCHAR);
      statement.setNull(6, OracleTypes.VARCHAR);
      statement.setNull(7, OracleTypes.INTEGER);
      statement.setInt(8, 0);
      statement.setNull(9, OracleTypes.VARCHAR);
      statement.registerOutParameter(10, OracleTypes.CURSOR);
      statement.executeQuery();

      try (ResultSet rs = (ResultSet) statement.getObject(10)) {
        while (rs.next()) {
          AuthRequestDto resultDto = new AuthRequestDto();
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
    AuthRequestCreateDto dto = (AuthRequestCreateDto) record;
    //language=Oracle
    String insertSqlQuery = "declare " +
        "authCode varchar2(64) := ?;" +
        "redirectUri varchar2(4000) := ?;" +
        "clientId varchar(32) := ?;" +
        "operatorId integer := ?;" +
        "tokenId varchar2(50) := ?;" +
        "tokenDateIns date := ?;" +
        "sessionId varchar2(50) := ?;" +
        "clientCount integer;" +
        "err_num NUMBER;" +
        "err_msg VARCHAR2(100);" +
      "begin " +
        "select count(*) into clientCount from OA_CLIENT cl where cl.CLIENT_CODE like clientId; " +
        "if clientCount <> 1 then " +
          "raise_application_error(-20001, 'Нет клиентского приложения с указанным ID'); " +
        "end if;" +
        "insert into OA_AUTH_REQUEST(AUTHORIZATION_CODE, REDIRECT_URI, CLIENT_ID, OPERATOR_ID, TOKEN_ID, TOKEN_DATE_INS, SESSION_ID)" +
        "values (authCode, redirectUri, (select cl.CLIENT_ID from OA_CLIENT cl where cl.CLIENT_CODE like clientId), operatorId, tokenId, tokenDateIns, sessionId); " +
        "? := OA_AUTH_REQUEST_SEQ.currval;" +
      "exception " +
        "when others then " +
          "err_num := SQLCODE; " +
          "err_msg := SUBSTR(SQLERRM, 1, 100); " +
          "if instr(err_msg, 'OA_AUTH_REQUEST_FK_CLIENT_URI') <> 0 then " +
            "raise_application_error(-20002, concat('Указанный URI не содержится в WHITELIST ', redirectUri)); " +
          "end if; " +
          "if instr(err_msg, 'OA_AUTH_REQUEST_FK_CLIENT') <> 0 or err_num = -20001 then " +
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
        insertStatement.setNull(4, java.sql.Types.INTEGER);
      }
      insertStatement.setString(5, dto.getTokenId());
      if (dto.getTokenDateIns() != null) {
        insertStatement.setDate(6, new Date(dto.getTokenDateIns().getTime()));
      } else {
        insertStatement.setNull(6, Types.DATE);
      }
      insertStatement.setString(7, dto.getSessionId());
      insertStatement.registerOutParameter(8, OracleTypes.INTEGER);
      insertStatement.execute();
      result = (Integer) insertStatement.getObject(8);
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
    AuthRequestUpdateDto dto = (AuthRequestUpdateDto) record;
    //language=Oracle
    String updateSqlQuery = "UPDATE OA_AUTH_REQUEST t " +
      "SET OPERATOR_ID = ?, TOKEN_ID = ?, TOKEN_DATE_INS = ?, IS_BLOCKED = ?, SESSION_ID = ? " +
      "WHERE t.AUTH_REQUEST_ID = ?";
    CallableStatement updateStatement = db.prepare(updateSqlQuery);
    try {
      if (dto.getOperatorId() != null) {
        updateStatement.setInt(1, dto.getOperatorId());
      } else {
        updateStatement.setNull(1, java.sql.Types.INTEGER);
      }
      updateStatement.setString(2, dto.getTokenId());
      if (dto.getTokenDateIns() != null) {
        updateStatement.setDate(3, new Date(dto.getTokenDateIns().getTime()));
      } else {
        updateStatement.setNull(3, Types.DATE);
      }
      updateStatement.setInt(4, dto.getBlocked() != null ? (dto.getBlocked() ? 1 : 0) : 0);
      updateStatement.setString(5, dto.getSessionId());
      updateStatement.setInt(6, (Integer) primaryKey.get(AUTH_REQUEST_ID));
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
  public void blockAuthRequest(Integer authRequestId) {
    Db db = getDb();
    //language=Oracle
    String updateSqlQuery = "UPDATE OA_AUTH_REQUEST t " +
      "SET IS_BLOCKED = ? " +
      "WHERE t.AUTH_REQUEST_ID = ?";
    CallableStatement updateStatement = db.prepare(updateSqlQuery);
    try {
      updateStatement.setInt(1, 1);
      updateStatement.setInt(2, authRequestId);
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
    throw new UnsupportedOperationException();
  }
}
