package org.jepria.oauth.client.dao;

import com.technology.jep.jepria.server.dao.ResultSetMapper;
import com.technology.jep.jepria.server.db.Db;
import org.jepria.oauth.client.dto.ClientCreateDto;
import org.jepria.oauth.client.dto.ClientDto;
import org.jepria.oauth.client.dto.ClientSearchDto;
import org.jepria.oauth.client.dto.ClientUpdateDto;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jepria.oauth.client.ClientFieldNames.*;

public class ClientDaoImpl implements ClientDao {

  private Db db;

  private Db getDb() {
    if (db == null) {
      db = new Db("jdbc/RFInfoDS");
    }
    return db;
  }

  //language=Oracle
  private String findSqlQuery =
    "select ocl.CLIENT_ID, ocl.CLIENT_CODE, ocl.CLIENT_SECRET, ocl.CLIENT_NAME, ocl.CLIENT_NAME_EN, ocl.IS_DELETED " +
      "from OA_CLIENT ocl";
  String clientIdClause = "ocl.CLIENT_ID = ?";
  String clientCodeClause = "ocl.CLIENT_CODE = ?";
  String clientNameClause = "ocl.CLIENT_NAME like ?";
  String clientNameEnClause = "ocl.CLIENT_NAME_EN like ?";
  String deletedClause = "ocl.IS_DELETED = ?";

//      "where ocl.CLIENT_ID = ? " +
//      "and ocl.CLIENT_CODE = ? " +
//      "and ocl.CLIENT_NAME like ? " +
//      "and ocl.CLIENT_NAME_EN like ? " +
//      "and ocl.IS_DELETED = ?";

  private ResultSetMapper mapper = new ResultSetMapper<ClientDto>() {
    @Override
    public void map(ResultSet rs, ClientDto dto) throws SQLException {
      dto.setClientId(getInteger(rs, CLIENT_ID));
      dto.setClientCode(rs.getString(CLIENT_CODE));
      dto.setClientSecret(rs.getString(CLIENT_SECRET));
      dto.setClientName(rs.getString(CLIENT_NAME));
      dto.setClientNameEn(rs.getString(CLIENT_NAME_EN));
      dto.setDeleted(getBoolean(rs, IS_DELETED));
    }
  };


  @Override
  public List<?> find(Object template, Integer operatorId) {
    ClientSearchDto dto = (ClientSearchDto) template;
    Db db = getDb();
    ArrayList<String> clauses = new ArrayList<>();
    if (dto.getClientId() != null) clauses.add(clientIdClause);
    if (dto.getClientCode() != null) clauses.add(clientCodeClause);
    if (dto.getClientName() != null) clauses.add(clientNameClause);
    if (dto.getClientNameEn() != null) clauses.add(clientNameEnClause);
    if (dto.getDeleted() != null) clauses.add(deletedClause);
    CallableStatement statement = null;
    List<ClientDto> result = null;
    String findSqlQuery = this.findSqlQuery;
    try {
      if (!clauses.isEmpty()) {
        for(String clause : clauses) {
          findSqlQuery += " and " + clause;
        }
        findSqlQuery = findSqlQuery.replaceFirst("and", "where");
        statement = db.prepare(findSqlQuery);
        int index = 1;
        if (dto.getClientId() != null) statement.setInt(index++, dto.getClientId());
        if (dto.getClientCode() != null) statement.setString(index++, dto.getClientCode());
        if (dto.getClientName() != null) statement.setString(index++, dto.getClientName());
        if (dto.getClientNameEn() != null) statement.setString(index++, dto.getClientNameEn());
        if (dto.getDeleted() != null) statement.setInt(index++, dto.getDeleted() ? 1 : 0);
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
    } finally {
      db.closeAll();
    }
    return result;
  }

  @Override
  public List<ClientDto> findByPrimaryKey(Map<String, ?> primaryKeyMap, Integer operatorId) {
    Db db = getDb();
    String findSqlQuery = this.findSqlQuery + " where " + clientIdClause;
    CallableStatement statement = db.prepare(findSqlQuery);
    List<ClientDto> result = null;
    try {
      statement.setInt(1, (Integer) primaryKeyMap.get(CLIENT_ID));
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
    String insertSqlQuery = "insert into OA_CLIENT(CLIENT_CODE, CLIENT_SECRET, CLIENT_NAME, CLIENT_NAME_EN, OPERATOR_ID) " +
      "values (?, ?, ?, ?, ?)";
    CallableStatement insertStatement = db.prepare(insertSqlQuery);
    Integer result = null;
    try {
      insertStatement.setString(1, dto.getClientCode());
      insertStatement.setString(2, dto.getClientSecret());
      insertStatement.setString(3, dto.getClientName());
      insertStatement.setString(4, dto.getClientNameEn());
      insertStatement.setInt(5, 1);
      int addedRecordCount = insertStatement.executeUpdate();
      if (addedRecordCount == 1) {
        //language=Oracle
        String sqlGetIndex = "SELECT OA_CLIENT_SEQ.currval FROM dual";
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
    ClientUpdateDto dto = (ClientUpdateDto) record;
    //language=Oracle
    String updateSqlQuery = "UPDATE OA_CLIENT t " +
      "SET CLIENT_NAME = ?, CLIENT_NAME_EN = ?, IS_DELETED = ? " +
      "WHERE t.CLIENT_ID = ?";
    CallableStatement updateStatement = db.prepare(updateSqlQuery);
    try {
      updateStatement.setString(1, dto.getClientName());
      updateStatement.setString(2, dto.getClientNameEn());
      updateStatement.setString(3, dto.getClientNameEn());
      updateStatement.setInt(4, (Integer) primaryKey.get(CLIENT_ID));
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
    Db db = getDb();
    //language=Oracle
    String updateSqlQuery = "UPDATE OA_CLIENT t " +
      "SET IS_DELETED = ? " +
      "WHERE t.CLIENT_ID = ?";
    CallableStatement updateStatement = db.prepare(updateSqlQuery);
    try {
      updateStatement.setInt(1, 1);
      updateStatement.setInt(2, (Integer) primaryKey.get(CLIENT_ID));
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
}
