package org.jepria.oauth.authorization.dao;

import com.technology.jep.jepria.server.dao.ResultSetMapper;
import com.technology.jep.jepria.server.db.Db;
import org.jepria.oauth.authorization.dto.AuthRequestCreateDto;
import org.jepria.oauth.authorization.dto.AuthRequestDto;
import org.jepria.oauth.authorization.dto.AuthRequestSearchDto;
import org.jepria.oauth.authorization.dto.AuthRequestUpdateDto;
import org.jepria.server.data.OptionDto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jepria.oauth.authorization.AuthorizationFieldNames.*;

public class AuthorizationDaoImpl implements AuthorizationDao {

  private Db db;

  protected Db getDb() {
    if (db == null) {
      db = new Db("jdbc/RFInfoDS");
    }
    return db;
  }

  //language=Oracle
  private String findSqlQuery =
    "select ar.AUTH_REQUEST_ID, ar.CLIENT_CODE, cl.CLIENT_NAME, ar.REDIRECT_URI, ar.AUTHORIZATION_CODE, ar.DATE_INS, ar.TOKEN_ID," +
      "ar.TOKEN_DATE_INS, ar.OPERATOR_ID, op.OPERATOR_NAME, op.LOGIN as OPERATOR_LOGIN, ar.IS_BLOCKED " +
    "from OA_AUTH_REQUEST ar left join OP_OPERATOR op on ar.OPERATOR_ID = op.OPERATOR_ID " +
      "inner join OA_CLIENT cl on ar.CLIENT_CODE = cl.CLIENT_CODE";
  String authRequestIdClause = "ar.AUTH_REQUEST_ID = ?";
  String authCodeClause = "ar.AUTHORIZATION_CODE like ?";
  String operatorClause =  "ar.OPERATOR_ID = ?";
  String clientCodeClause = "ar.CLIENT_CODE like ?";
  String redirectUriClause = "ar.REDIRECT_URI like ?";
  String tokenIdClause = "ar.TOKEN_ID like ?";
  String isBlockedClause = "ar.IS_BLOCKED = ?";


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
      client.setValue(rs.getString(CLIENT_CODE));
      client.setName(rs.getString(CLIENT_NAME));
      dto.setClient(client);
      dto.setTokenId(rs.getString(TOKEN_ID));
      dto.setTokenDateIns(getTimestamp(rs, TOKEN_DATE_INS));
      dto.setBlocked(getBoolean(rs, IS_BLOCKED));
    }
  };

  @Override
  public List<AuthRequestDto> find(Object template, Integer operatorId) {
    AuthRequestSearchDto dto = (AuthRequestSearchDto) template;
    Db db = getDb();
    ArrayList<String> clauses = new ArrayList<>();
    if (dto.getAuthRequestId() != null) clauses.add(authRequestIdClause);
    if (dto.getAuthorizationCode() != null) clauses.add(authCodeClause);
    if (dto.getOperatorId() != null) clauses.add(operatorClause);
    if (dto.getClientCode() != null) clauses.add(clientCodeClause);
    if (dto.getTokenId() != null) clauses.add(tokenIdClause);
    if (dto.getBlocked() != null) clauses.add(isBlockedClause);
    if (dto.getRedirectUri() != null) clauses.add(redirectUriClause);
    CallableStatement statement = null;
    List<AuthRequestDto> result = null;
    String findSqlQuery = this.findSqlQuery;
    try {
      if (!clauses.isEmpty()) {
        for(String clause : clauses) {
          findSqlQuery += " and " + clause;
        }
        findSqlQuery = findSqlQuery.replaceFirst("and", "where");
        statement = db.prepare(findSqlQuery);
        int index = 1;
        if (dto.getAuthRequestId() != null) statement.setInt(index++, dto.getAuthRequestId());
        if (dto.getAuthorizationCode() != null) statement.setString(index++, dto.getAuthorizationCode());
        if (dto.getOperatorId() != null) statement.setInt(index++, dto.getOperatorId());
        if (dto.getClientCode() != null) statement.setString(index++, dto.getClientCode());
        if (dto.getTokenId() != null) statement.setString(index++, dto.getTokenId());
        if (dto.getBlocked() != null) statement.setInt(index++, dto.getBlocked() ? 1 : 0);
        if (dto.getRedirectUri() != null) statement.setString(index++, dto.getRedirectUri());
      }
      if (statement == null) db.prepare(findSqlQuery);
      statement.executeQuery();

      ResultSet rs = statement.getResultSet();
      result = new ArrayList<>();
      while (rs.next()) {
        AuthRequestDto resultDto = new AuthRequestDto();
        mapper.map(rs, resultDto);
        result.add(resultDto);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      db.closeAll();
    }
    return result;
  }

  @Override
  public List<AuthRequestDto> findByPrimaryKey(Map<String, ?> primaryKeyMap, Integer operatorId) {
    Db db = getDb();
    findSqlQuery += " where " + authRequestIdClause;
    CallableStatement statement = db.prepare(findSqlQuery);
    List<AuthRequestDto> result = null;
    try {
      statement.setInt(1, (Integer) primaryKeyMap.get(AUTH_REQUEST_ID));
      statement.executeQuery();
      ResultSet rs = statement.getResultSet();
      result = new ArrayList<>();
      while (rs.next()) {
        AuthRequestDto resultDto = new AuthRequestDto();
        mapper.map(rs, resultDto);
        result.add(resultDto);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      db.closeAll();
    }
    return result;
  }

  @Override
  public Object create(Object record, Integer operatorId) {
    Db db = getDb();
    //language=Oracle
    AuthRequestCreateDto dto = (AuthRequestCreateDto) record;
    String insertSqlQuery = "insert into OA_AUTH_REQUEST(AUTHORIZATION_CODE, REDIRECT_URI, CLIENT_CODE, OPERATOR_ID, TOKEN_ID, TOKEN_DATE_INS) " +
      "values (?, ?, ?, ?, ?, ?)";
    CallableStatement insertStatement = db.prepare(insertSqlQuery);
    Integer result = null;
    try {
      insertStatement.setString(1, dto.getAuthorizationCode());
      insertStatement.setString(2, dto.getRedirectUri());
      insertStatement.setString(3, dto.getClientCode());
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
      int addedRecordCount = insertStatement.executeUpdate();
      if (addedRecordCount == 1) {
        //language=Oracle
        String sqlGetIndex = "SELECT OA_AUTH_REQUEST_SEQ.currval FROM dual";
        CallableStatement getIndexStatement = db.prepare(sqlGetIndex);
        ResultSet rs = getIndexStatement.executeQuery();
        if (rs.next()) {
          result = rs.getInt(1);
        }
        db.commit();
      }
    } catch (SQLException e) {
      e.printStackTrace();
      db.rollback();
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
      "SET OPERATOR_ID = ?, TOKEN_ID = ?, TOKEN_DATE_INS = ?, IS_BLOCKED = ? " +
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
      updateStatement.setInt(5, (Integer) primaryKey.get(AUTH_REQUEST_ID));
      int updatedRecordCount = updateStatement.executeUpdate();
      if (updatedRecordCount == 1) {
        db.commit();
      }
    } catch (SQLException e) {
      e.printStackTrace();
      db.rollback();
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
    } finally {
      db.closeAll();
    }
  }

  @Override
  public void delete(Map<String, ?> primaryKey, Integer operatorId) {
    throw new UnsupportedOperationException();
  }
}
