package org.jepria.oauth.client.dao;

import com.technology.jep.jepria.server.dao.ResultSetMapper;
import com.technology.jep.jepria.server.db.Db;
import oracle.jdbc.OracleConnection;
import oracle.sql.ArrayDescriptor;
import org.jepria.oauth.client.ClientConstants;
import org.jepria.oauth.client.dto.*;
import org.jepria.server.data.DaoSupport;
import org.jepria.server.data.OptionDto;
import org.jepria.server.data.RuntimeSQLException;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.jepria.oauth.client.ClientFieldNames.*;
import static org.jepria.oauth.sdk.GrantType.AUTHORIZATION_CODE;
import static org.jepria.oauth.sdk.GrantType.IMPLICIT;

public class ClientDaoImpl implements ClientDao {

  private Db getDb() {
    return new Db("jdbc/RFInfoDS");
  }

  //language=Oracle
  private String findSqlQuery =
      "select ct.client_id, ct.client_code, ct.client_secret, ct.client_name, ct.client_name_en, " +
        "ct.application_type_code, apt.application_type_name, ct.token_auth_method_code, tm.token_auth_method_name " +
      "from OA_CLIENT ct " +
        "inner join OA_APPLICATION_TYPE apt " +
          "on ct.application_type_code = apt.application_type_code " +
        "inner join OA_TOKEN_AUTH_METHOD tm " +
          "on ct.token_auth_method_code = tm.token_auth_method_code " +
      "where ct.is_deleted = 0 ";
  String clientCodeClause = "and ct.CLIENT_CODE = ? ";
  String clientNameClause = "and ct.CLIENT_NAME like ? ";
  String clientNameEnClause = "and ct.CLIENT_NAME_EN like ? ";

  private ResultSetMapper mapper = new ResultSetMapper<ClientDto>() {
    @Override
    public void map(ResultSet rs, ClientDto dto) throws SQLException {
      dto.setClientId(rs.getString(CLIENT_CODE));
      dto.setClientSecret(rs.getString(CLIENT_SECRET));
      dto.setClientName(rs.getString(CLIENT_NAME));
      dto.setClientNameEn(rs.getString(CLIENT_NAME_EN));
      OptionDto<String> applicationType = new OptionDto<>();
      applicationType.setName(rs.getString(APPLICATION_TYPE_NAME));
      applicationType.setValue(rs.getString(APPLICATION_TYPE_CODE));
      dto.setApplicationType(applicationType);
      OptionDto<String> tokenAuthMethod = new OptionDto<>();
      tokenAuthMethod.setName(rs.getString(TOKEN_AUTH_METHOD_NAME));
      tokenAuthMethod.setValue(rs.getString(TOKEN_AUTH_METHOD_CODE));
      dto.setTokenAuthMethod(tokenAuthMethod);
      dto.setGrantTypes(getClientGrantTypes(getInteger(rs, CLIENT_ID)));
      dto.setResponseTypes(getClientResponseTypes(getInteger(rs, CLIENT_ID)));
    }
  };


  @Override
  public List<?> find(Object template, Integer operatorId) {
    ClientSearchDto dto = (ClientSearchDto) template;
    Db db = getDb();
    ArrayList<String> clauses = new ArrayList<>();
    if (dto.getClientId() != null) clauses.add(clientCodeClause);
    if (dto.getClientName() != null) clauses.add(clientNameClause);
    if (dto.getClientNameEn() != null) clauses.add(clientNameEnClause);
    CallableStatement statement = null;
    List<ClientDto> result = null;
    String findSqlQuery = this.findSqlQuery;
    try {
      if (!clauses.isEmpty()) {
        for(String clause : clauses) {
          findSqlQuery += clause;
        }
        statement = db.prepare(findSqlQuery);
        int index = 1;
        if (dto.getClientId() != null) statement.setString(index++, dto.getClientId());
        if (dto.getClientName() != null) statement.setString(index++, dto.getClientName() + "%");
        if (dto.getClientNameEn() != null) statement.setString(index++, dto.getClientNameEn() + "%");
      }
      if (statement == null) db.prepare(findSqlQuery);
      statement.executeQuery();
      ResultSet rs = statement.getResultSet();
      result = new ArrayList<>();
      while (rs.next()) {
        ClientDto resultDto = new ClientDto();
        mapper.map(rs, resultDto);
        result.add(resultDto);
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
  public List<ClientDto> findByPrimaryKey(Map<String, ?> primaryKeyMap, Integer operatorId) {
    Db db = getDb();
    String findSqlQuery = this.findSqlQuery + clientCodeClause;
    CallableStatement statement = db.prepare(findSqlQuery);
    List<ClientDto> result = null;
    try {
      statement.setString(1, (String) primaryKeyMap.get(CLIENT_ID));
      statement.executeQuery();
      ResultSet rs = statement.getResultSet();
      result = new ArrayList<>();
      while (rs.next()) {
        ClientDto resultDto = new ClientDto();
        mapper.map(rs, resultDto);
        result.add(resultDto);
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
    //language=Oracle
    ClientCreateDto dto = (ClientCreateDto) record;
    String insertSqlQuery = "insert into OA_CLIENT(CLIENT_CODE, CLIENT_SECRET, CLIENT_NAME, CLIENT_NAME_EN, APPLICATION_TYPE_CODE, TOKEN_AUTH_METHOD_CODE, OPERATOR_ID_INS) " +
      "values (?, ?, ?, ?, ?, ?, ?)";
    CallableStatement insertStatement = db.prepare(insertSqlQuery);
    try {
      insertStatement.setString(1, dto.getClientId());
      insertStatement.setString(2, dto.getClientSecret());
      insertStatement.setString(3, dto.getClientName());
      insertStatement.setString(4, dto.getClientNameEn());
      insertStatement.setString(5, dto.getApplicationType());
      insertStatement.setString(6, dto.getTokenAuthMethod());
      insertStatement.setInt(7, operatorId);
      int addedRecordCount = insertStatement.executeUpdate();
      if (addedRecordCount == 1) {
        //language=Oracle
        String sqlGetIndex = "select OA_CLIENT_SEQ.currval from dual";
        CallableStatement getIndexStatement = db.prepare(sqlGetIndex);
        ResultSet rs = getIndexStatement.executeQuery();
        Integer clientId = null;
        if (rs.next()) {
          clientId = rs.getInt(1);
        }
        /*
         Add grantTypes
         */
        //language=Oracle
        String insertGrantSqlString = "insert into OA_CLIENT_GRANT_TYPE (CLIENT_ID, GRANT_TYPE_CODE) values (?,?)";
        CallableStatement insertGrantTypeStatement = db.prepare(insertGrantSqlString);
        if (dto.getGrantTypes() != null && dto.getGrantTypes().size() != 0) {
          for (String grantType : dto.getGrantTypes()) {
            insertGrantTypeStatement.setInt(1, clientId);
            insertGrantTypeStatement.setString(2, grantType);
            insertGrantTypeStatement.addBatch();
          }
          insertGrantTypeStatement.executeBatch();
        } else {
          insertGrantTypeStatement.setInt(1, clientId);
          insertGrantTypeStatement.setString(2, dto.getApplicationType().equalsIgnoreCase(ClientConstants.WEB) ? AUTHORIZATION_CODE : IMPLICIT);
          insertGrantTypeStatement.executeQuery();
        }
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
    return dto.getClientId();
  }

  @Override
  public void update(Map<String, ?> primaryKey, Object record, Integer operatorId) {
    Db db = getDb();
    ClientUpdateDto dto = (ClientUpdateDto) record;
    //language=Oracle
    String updateSqlQuery = "UPDATE OA_CLIENT t " +
      "SET CLIENT_NAME = ?, CLIENT_NAME_EN = ?, APPLICATION_TYPE_CODE = ?, TOKEN_AUTH_METHOD_CODE = ? " +
      "WHERE t.CLIENT_CODE = ?";
    CallableStatement updateStatement = db.prepare(updateSqlQuery);
    try {
      updateStatement.setString(1, dto.getClientName());
      updateStatement.setString(2, dto.getClientNameEn());
      updateStatement.setString(3, dto.getApplicationType());
      updateStatement.setString(4, dto.getTokenAuthMethod());
      updateStatement.setString(5, (String) primaryKey.get(CLIENT_ID));
      updateStatement.executeUpdate();
      /*
       update grantTypes
      */
      if (dto.getGrantTypes() != null && dto.getGrantTypes().size() != 0) {
        String arrayParams = "";
        for (String grantType: dto.getGrantTypes()) {
          arrayParams += ",?";
        }
        arrayParams = arrayParams.replaceFirst(",", "");
        String deleteGrantsSqlString = "delete from OA_CLIENT_GRANT_TYPE cgt " +
          "where cgt.client_id in (" +
              "select ct.client_id " +
              "from OA_CLIENT ct " +
              "where ct.CLIENT_CODE = ?) " +
            "and cgt.grant_type_code not in (" + arrayParams + ")";
        CallableStatement deleteGrantTypeStatement = db.prepare(deleteGrantsSqlString);
        deleteGrantTypeStatement.setString(1, (String) primaryKey.get(CLIENT_ID));
        int index = 2;
        for (String grantType: dto.getGrantTypes()) {
          deleteGrantTypeStatement.setString(index++, grantType);
        }
        deleteGrantTypeStatement.executeQuery();
        String mergeGrantsSqlQuery = "merge into OA_CLIENT_GRANT_TYPE cgt " +
          "using (select ct.client_id, gt.grant_type_code " +
            "from OA_CLIENT ct " +
              "cross join OA_GRANT_TYPE gt " +
              "where gt.grant_type_code in (" + arrayParams + ") and ct.client_code = ?) src " +
            "on (cgt.client_id = src.client_id and cgt.grant_type_code = src.grant_type_code) " +
          "when not matched then " +
            "insert (cgt.client_id, cgt.grant_type_code) " +
            "values (src.client_id, src.grant_type_code) ";
        CallableStatement mergeGrantsStatement = db.prepare(mergeGrantsSqlQuery);
        index = 1;
        for (String grantType: dto.getGrantTypes()) {
          mergeGrantsStatement.setString(index++, grantType);
        }
        mergeGrantsStatement.setString(index, (String) primaryKey.get(CLIENT_ID));
        mergeGrantsStatement.executeUpdate();
      } else {
        //language=Oracle
        String deleteGrantsSqlString = "delete from OA_CLIENT_GRANT_TYPE cgt " +
          "where cgt.client_id in (" +
              "select ct.client_id " +
              "from OA_CLIENT ct " +
              "where ct.CLIENT_CODE = ?)";
        CallableStatement deleteGrantTypeStatement = db.prepare(deleteGrantsSqlString);
        deleteGrantTypeStatement.setString(1, (String) primaryKey.get(CLIENT_ID));
        deleteGrantTypeStatement.execute();
        //language=Oracle
        String insertGrantSqlString = "insert into OA_CLIENT_GRANT_TYPE (CLIENT_ID, GRANT_TYPE_CODE) " +
          "values ((" +
            "select ct.client_id " +
            "from OA_CLIENT ct " +
            "where ct.client_code = ?" +
          "), ?)";
        CallableStatement insertGrantTypeStatement = db.prepare(insertGrantSqlString);
        insertGrantTypeStatement.setString(1, (String) primaryKey.get(CLIENT_ID));
        insertGrantTypeStatement.setString(2, dto.getApplicationType().equalsIgnoreCase(ClientConstants.WEB) ? AUTHORIZATION_CODE : IMPLICIT);
        insertGrantTypeStatement.executeQuery();
      }
      db.commit();
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
    String updateSqlQuery = "UPDATE OA_CLIENT t " +
      "SET IS_DELETED = ? " +
      "WHERE t.CLIENT_CODE = ?";
    CallableStatement updateStatement = db.prepare(updateSqlQuery);
    try {
      updateStatement.setInt(1, 1);
      updateStatement.setString(2, (String) primaryKey.get(CLIENT_ID));
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

  private List<OptionDto<String>> getClientResponseTypes(Integer clientId) {
    //language=Oracle
    String sqlString = "select grtc.RESPONSE_TYPE_CODE, grtc.RESPONSE_TYPE_NAME " +
      "from OA_CLIENT_GRANT_TYPE cgt " +
      "inner join (select grt.grant_type_code, grt.response_type_code, rt.response_type_name  " +
                 "from OA_GRANT_RESPONSE_TYPE grt " +
                 "inner join OA_RESPONSE_TYPE rt on grt.response_type_code = rt.response_type_code) grtc on cgt.grant_type_code = grtc.grant_type_code " +
      "where cgt.CLIENT_ID = ?";
    Db db = getDb();
    CallableStatement statement = db.prepare(sqlString);
    List<OptionDto<String>> result = null;
    try {
      statement.setInt(1, clientId);
      statement.executeQuery();
      ResultSet rs = statement.getResultSet();
      result = new ArrayList<>();
      while (rs.next()) {
        OptionDto<String> responseTypeDto = new OptionDto<String>();
        responseTypeDto.setValue(rs.getString(RESPONSE_TYPE_CODE));
        responseTypeDto.setName(rs.getString(RESPONSE_TYPE_NAME));
        result.add(responseTypeDto);
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

  private List<OptionDto<String>> getClientGrantTypes(Integer clientId) {
    //language=Oracle
    String sqlString = "select cgt.grant_type_code, gt.grant_type_name " +
      "from OA_CLIENT_GRANT_TYPE cgt " +
      "inner join OA_GRANT_TYPE gt on cgt.grant_type_code = gt.grant_type_code " +
      "where cgt.CLIENT_ID = ?";
    Db db = getDb();
    CallableStatement statement = db.prepare(sqlString);
    List<OptionDto<String>> result = null;
    try {
      statement.setInt(1, clientId);
      statement.executeQuery();
      ResultSet rs = statement.getResultSet();
      result = new ArrayList<>();
      while (rs.next()) {
        OptionDto<String> grantTypeDto = new OptionDto<String>();
        grantTypeDto.setValue(rs.getString(GRANT_TYPE_CODE));
        grantTypeDto.setName(rs.getString(GRANT_TYPE_NAME));
        result.add(grantTypeDto);
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
  public List<OptionDto<String>> getGrantType() {
    //language=Oracle
    String sqlString = "select * from OA_GRANT_TYPE";
    Db db = getDb();
    CallableStatement statement = db.prepare(sqlString);
    List<OptionDto<String>> result = null;
    try {
      statement.executeQuery();
      ResultSet rs = statement.getResultSet();
      result = new ArrayList<>();
      while (rs.next()) {
        OptionDto<String> grantTypeDto = new OptionDto<String>();
        grantTypeDto.setValue(rs.getString(GRANT_TYPE_CODE));
        grantTypeDto.setName(rs.getString(GRANT_TYPE_NAME));
        result.add(grantTypeDto);
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
  public List<OptionDto<String>> getGrantResponseType(List<String> grantTypeCodes) {
    if (grantTypeCodes == null || grantTypeCodes.size() == 0) {
      return Collections.emptyList();
    }
    List<OptionDto<String>> result = null;
    Db db = getDb();
    try {
      String arrayParams = "";
      for (String grantType: grantTypeCodes) {
        arrayParams += ",?";
      }
      arrayParams = arrayParams.replaceFirst(",", "");
      String sqlString = "select grtc.response_type_code, rt.response_type_name " +
        "from (select grt.grant_type_code, gt.grant_type_name, grt.response_type_code " +
          "from OA_GRANT_RESPONSE_TYPE grt " +
          "inner join OA_GRANT_TYPE gt on grt.grant_type_code = gt.grant_type_code " +
          "where grt.grant_type_code in (" + arrayParams + ")) grtc " +
        "inner join OA_RESPONSE_TYPE rt on grtc.response_type_code = rt.response_type_code";
      CallableStatement statement = db.prepare(sqlString);
      int index = 1;
      for (String grantType: grantTypeCodes) {
        statement.setString(index++, grantType);
      }
      statement.executeQuery();
      ResultSet rs = statement.getResultSet();
      result = new ArrayList<>();
      while (rs.next()) {
        OptionDto<String> dto = new OptionDto<String>();
        dto.setValue(rs.getString(RESPONSE_TYPE_CODE));
        dto.setName(rs.getString(RESPONSE_TYPE_NAME));
        result.add(dto);
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
}
