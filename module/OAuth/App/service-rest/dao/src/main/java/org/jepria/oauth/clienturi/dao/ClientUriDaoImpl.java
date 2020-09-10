package org.jepria.oauth.clienturi.dao;

import org.jepria.compat.server.dao.ResultSetMapper;
import org.jepria.compat.server.db.Db;
import org.jepria.oauth.clienturi.dto.ClientUriCreateDto;
import org.jepria.oauth.clienturi.dto.ClientUriDto;
import org.jepria.oauth.clienturi.dto.ClientUriSearchDto;
import org.jepria.server.data.Dao;
import org.jepria.server.data.RuntimeSQLException;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jepria.oauth.clienturi.ClientUriFieldNames.*;
import static org.jepria.oauth.session.SessionFieldNames.CLIENT_SHORT_NAME;


public class ClientUriDaoImpl implements Dao {
  
  private String jndiName = "jdbc/RFInfoDS";
  
  public ClientUriDaoImpl(){};
  
  public ClientUriDaoImpl(String jndName) {
    this.jndiName = jndName;
  }


  private Db getDb() {
    return new Db(jndiName);
  }

  private ResultSetMapper mapper = new ResultSetMapper<ClientUriDto>() {
    @Override
    public void map(ResultSet rs, ClientUriDto dto) throws SQLException {
      dto.setClientUriId(getInteger(rs, CLIENT_URI_ID));
      dto.setClientUri(rs.getString(CLIENT_URI));
      dto.setClientId(rs.getString(CLIENT_SHORT_NAME));
    }
  };


  @Override
  public List<ClientUriDto> find(Object template, Integer operatorId) {
    //language=Oracle
    String findSqlQuery =
      "select ct.SHORT_NAME as CLIENT_SHORT_NAME, cu.client_uri_id, cu.client_uri " +
        "from OA_CLIENT_URI cu " +
          "inner join OA_CLIENT ct " +
            "on cu.client_id = ct.client_id " +
        "where ct.short_name like ?";
    ClientUriSearchDto dto = (ClientUriSearchDto) template;
    Db db = getDb();
    CallableStatement statement = db.prepare(findSqlQuery);
    List<ClientUriDto> result = null;
    try {
      statement.setString(1, dto.getClientId());
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
  public List<ClientUriDto> findByPrimaryKey(Map<String, ?> primaryKeyMap, Integer operatorId) {
    Db db = getDb();
    //language=Oracle
    String findSqlQuery =
      "select ct.SHORT_NAME as CLIENT_SHORT_NAME, cu.client_uri_id, cu.client_uri " +
        "from OA_CLIENT_URI cu " +
        "inner join OA_CLIENT ct " +
        "      on cu.client_id = ct.client_id " +
        "where ct.short_name = ? and cu.client_uri_id = ?";
    CallableStatement statement = db.prepare(findSqlQuery);
    List<ClientUriDto> result = null;
    try {
      statement.setString(1, (String) primaryKeyMap.get(CLIENT_SHORT_NAME));
      statement.setInt(2, (Integer) primaryKeyMap.get(CLIENT_URI_ID));
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
    ClientUriCreateDto dto = (ClientUriCreateDto) record;
    //language=Oracle
    String insertSqlQuery = "insert into OA_CLIENT_URI(CLIENT_ID, CLIENT_URI, OPERATOR_ID_INS) " +
      "values ((" +
        "select ct.client_id " +
        "from OA_CLIENT ct " +
        "where ct.short_name = ?" +
      "), ?, ?)";
    CallableStatement insertStatement = db.prepare(insertSqlQuery);
    Integer result = null;
    try {
      insertStatement.setString(1, dto.getClientId());
      insertStatement.setString(2, dto.getClientUri());
      insertStatement.setInt(3, operatorId);
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
    } catch (Throwable th) {
      th.printStackTrace();
      db.rollback();
      throw th;
    } finally {
      db.closeAll();
    }
  }
}
