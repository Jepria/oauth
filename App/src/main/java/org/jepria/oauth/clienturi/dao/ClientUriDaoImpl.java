package org.jepria.oauth.clienturi.dao;

import com.technology.jep.jepria.server.dao.ResultSetMapper;
import com.technology.jep.jepria.server.db.Db;
import org.jepria.oauth.clienturi.dto.ClientUriCreateDto;
import org.jepria.oauth.clienturi.dto.ClientUriDto;
import org.jepria.oauth.clienturi.dto.ClientUriSearchDtoLocal;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jepria.oauth.clienturi.ClientUriFieldNames.*;

public class ClientUriDaoImpl implements ClientUriDao {

  private Db db;

  private Db getDb() {
    if (db == null) {
      db = new Db("jdbc/RFInfoDS");
    }
    return db;
  }

  //language=Oracle
  private String findSqlQuery =
    "select oclu.CLIENT_URI_ID, oclu.CLIENT_ID, oclu.CLIENT_URI " +
      "from OA_CLIENT_URI oclu";

  private ResultSetMapper mapper = new ResultSetMapper<ClientUriDto>() {
    @Override
    public void map(ResultSet rs, ClientUriDto dto) throws SQLException {
      dto.setClientUriId(getInteger(rs, CLIENT_URI_ID));
      dto.setClientId(getInteger(rs, CLIENT_ID));
      dto.setClientUri(rs.getString(CLIENT_URI));
    }
  };


  @Override
  public List<ClientUriDto> find(Object template, Integer operatorId) {
    ClientUriSearchDtoLocal dto = (ClientUriSearchDtoLocal) template;
    Db db = getDb();
    CallableStatement statement = db.prepare(findSqlQuery + " where oclu.CLIENT_ID = ?");
    List<ClientUriDto> result = null;
    try {
      statement.setInt(1, dto.getClientId());
      statement.executeQuery();
      ResultSet rs = statement.getResultSet();
      result = new ArrayList<>();
      while (rs.next()) {
        ClientUriDto resultDto = new ClientUriDto();
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
  public List<ClientUriDto> findByPrimaryKey(Map<String, ?> primaryKeyMap, Integer operatorId) {
    Db db = getDb();
    CallableStatement statement = db.prepare(findSqlQuery + " where oclu.CLIENT_URI_ID = ?");
    List<ClientUriDto> result = null;
    try {
      statement.setInt(1, (Integer) primaryKeyMap.get(CLIENT_URI_ID));
      statement.executeQuery();
      ResultSet rs = statement.getResultSet();
      result = new ArrayList<>();
      while (rs.next()) {
        ClientUriDto resultDto = new ClientUriDto();
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
    ClientUriCreateDto dto = (ClientUriCreateDto) record;
    //language=Oracle
    String insertSqlQuery = "insert into OA_CLIENT_URI(CLIENT_ID, CLIENT_URI, OPERATOR_ID) " +
      "values (?, ?, ?)";
    CallableStatement insertStatement = db.prepare(insertSqlQuery);
    Integer result = null;
    try {
      insertStatement.setInt(1, dto.getClientId());
      insertStatement.setString(2, dto.getClientUri());
      insertStatement.setInt(3, 1);
      if (insertStatement.executeUpdate() == 1) {
        //language=Oracle
        String sqlGetIndex = "SELECT OA_CLIENT_URI_SEQ.currval FROM dual";
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
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(Map<String, ?> primaryKey, Integer operatorId) {
    Db db = getDb();
    //language=Oracle
    String deleteSqlQuery = "delete from OA_CLIENT_URI t " +
      "where t.CLIENT_URI_ID = ?";
    CallableStatement deleteStatement = db.prepare(deleteSqlQuery);
    try {
      deleteStatement.setInt(1, (Integer) primaryKey.get(CLIENT_URI_ID));
      deleteStatement.execute();
      db.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      db.rollback();
    } finally {
      db.closeAll();
    }
  }
}
