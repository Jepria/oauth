package org.jepria.oauth.client.dao;

import oracle.jdbc.OracleTypes;
import org.jepria.compat.server.dao.ResultSetMapper;
import org.jepria.compat.server.db.Db;
import org.jepria.oauth.client.dto.ClientCreateDto;
import org.jepria.oauth.client.dto.ClientDto;
import org.jepria.oauth.client.dto.ClientSearchDto;
import org.jepria.oauth.client.dto.ClientUpdateDto;
import org.jepria.oauth.sdk.ApplicationType;
import org.jepria.server.data.RuntimeSQLException;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jepria.oauth.client.ClientFieldNames.*;

public class ClientDaoImpl implements ClientDao {

  private String jndiName = "jdbc/RFInfoDS";

  public ClientDaoImpl() {
  }

  ;

  public ClientDaoImpl(String jndName) {
    this.jndiName = jndName;
  }

  private Db getDb() {
    return new Db(jndiName);
  }

  //language=Oracle
  private String findSqlQuery = "declare " +
    "rc sys_refcursor;" +
    "shortName varchar(64) := ?;" +
    "clientName varchar(100) := ?;" +
    "clientNameEn varchar(100) := ?;" +
    "begin " +
    "open rc for select " +
    //"ct.client_id," +
    "ct.short_name," +
    "ct.client_secret," +
    "ct.client_name," +
    "ct.client_name_en," +
    "ct.application_type_code " +
    "from OA_CLIENT ct " +
    "where ct.is_deleted = 0 " +
    "and (ct.short_name like shortName or shortName is null) " +
    "and (ct.client_name like clientName or clientName is null) " +
    "and (ct.client_name_en like clientNameEn or clientNameEn is null);" +
    "? := rc; " +
    "end;";

  private ResultSetMapper mapper = new ResultSetMapper<ClientDto>() {
    @Override
    public void map(ResultSet rs, ClientDto dto) throws SQLException {
      dto.setClientId(rs.getString(SHORT_NAME));
      dto.setClientSecret(rs.getString(CLIENT_SECRET));
      dto.setClientName(rs.getString(CLIENT_NAME));
      dto.setClientNameEn(rs.getString(CLIENT_NAME_EN));
      dto.setApplicationType(rs.getString(APPLICATION_TYPE_CODE));
      dto.setGrantTypes(getClientGrantTypes(dto.getClientId()));
    }
  };


  @Override
  public List<?> find(Object template, Integer operatorId) {
    ClientSearchDto dto = (ClientSearchDto) template;
    Db db = getDb();
    CallableStatement statement = db.prepare(findSqlQuery);
    List<ClientDto> result = new ArrayList<>();
    try {
      statement.setString(1, dto.getClientId());
      if (dto.getClientName() != null) {
        statement.setString(2, dto.getClientName() + "%");
      } else {
        statement.setNull(2, OracleTypes.VARCHAR);
      }
      if (dto.getClientNameEn() != null) {
        statement.setString(3, dto.getClientNameEn() + "%");
      } else {
        statement.setNull(3, OracleTypes.VARCHAR);
      }
      statement.registerOutParameter(4, OracleTypes.CURSOR);
      statement.executeQuery();
      try (ResultSet rs = (ResultSet) statement.getObject(4)) {
        while (rs.next()) {
          ClientDto resultDto = new ClientDto();
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
  public List<ClientDto> findByPrimaryKey(Map<String, ?> primaryKeyMap, Integer operatorId) {
    Db db = getDb();
    CallableStatement statement = db.prepare(findSqlQuery);
    List<ClientDto> result = new ArrayList<>();
    try {
      statement.setString(1, (String) primaryKeyMap.get(CLIENT_ID));
      statement.setNull(2, OracleTypes.VARCHAR);
      statement.setNull(3, OracleTypes.VARCHAR);
      statement.registerOutParameter(4, OracleTypes.CURSOR);
      statement.executeQuery();
      try (ResultSet rs = (ResultSet) statement.getObject(4)) {
        while (rs.next()) {
          ClientDto resultDto = new ClientDto();
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
    //language=Oracle
    ClientCreateDto dto = (ClientCreateDto) record;
    String insertSqlQuery = "insert into OA_CLIENT(SHORT_NAME, CLIENT_SECRET, CLIENT_NAME, CLIENT_NAME_EN, APPLICATION_TYPE_CODE, OPERATOR_ID_INS) " +
      "values (?, ?, ?, ?, ?, ?)";
    CallableStatement insertStatement = db.prepare(insertSqlQuery);
    try {
      insertStatement.setString(1, dto.getClientId());
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] salt = new byte[16];
      random.nextBytes(salt);
      md.update(salt);
      byte[] secret = new byte[32];
      random.nextBytes(secret);
      insertStatement.setString(2, DatatypeConverter.printHexBinary(md.digest(secret)));
      insertStatement.setString(3, dto.getClientName());
      insertStatement.setString(4, dto.getClientNameEn());
      insertStatement.setString(5, dto.getApplicationType());
      insertStatement.setInt(6, operatorId);
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
        CallableStatement insertGrantTypeStatement;
        if (dto.getGrantTypes() != null && dto.getGrantTypes().size() != 0) {
          String insertGrantSqlString = "insert into OA_CLIENT_GRANT_TYPE (CLIENT_ID, GRANT_TYPE_CODE) values (?,?)";
          insertGrantTypeStatement = db.prepare(insertGrantSqlString);
          for (String grantType : dto.getGrantTypes()) {
            insertGrantTypeStatement.setInt(1, clientId);
            insertGrantTypeStatement.setString(2, grantType);
            insertGrantTypeStatement.addBatch();
          }
          insertGrantTypeStatement.executeBatch();
        } else {
          String insertGrantSqlString =
            "insert into OA_CLIENT_GRANT_TYPE (CLIENT_ID, GRANT_TYPE_CODE) " +
              "values (?,?)";
          insertGrantTypeStatement = db.prepare(insertGrantSqlString);
          for (String grantType : ApplicationType.getApplicationGrantTypes(dto.getApplicationType())) {
            insertGrantTypeStatement.setInt(1, clientId);
            insertGrantTypeStatement.setString(2, grantType);
            insertGrantTypeStatement.addBatch();
          }
          insertGrantTypeStatement.executeBatch();
        }
        db.commit();
      }
    } catch (SQLException e) {
      e.printStackTrace();
      db.rollback();
      throw new RuntimeSQLException(e);
    } catch (NoSuchAlgorithmException | NoSuchProviderException th) {
      th.printStackTrace();
      db.rollback();
      throw new RuntimeException(th);
    } finally {
      db.closeAll();
    }
    return dto.getClientId();
  }

  @Override
  public void update(Map<String, ?> primaryKey, Object record, Integer operatorId) {
    Db db = getDb();
    ClientUpdateDto dto = (ClientUpdateDto) record;
    try {
      /*
      check applicationTypeCode diff.
      if incoming value is new, clear client grant types
       */
      ClientDto old = findByPrimaryKey(primaryKey, operatorId).get(0);
      if (!old.getApplicationType().equals(dto.getApplicationType())) {
        //language=Oracle
        String deleteGrantsSqlString = "delete from OA_CLIENT_GRANT_TYPE cgt " +
          "where cgt.client_id = (select ct.client_id from OA_CLIENT ct where ct.short_name like ?)";
        CallableStatement deleteGrantTypeStatement = db.prepare(deleteGrantsSqlString);
        deleteGrantTypeStatement.setString(1, (String) primaryKey.get(CLIENT_ID));
        deleteGrantTypeStatement.executeQuery();
      }

      //language=Oracle
      String updateSqlQuery = "UPDATE OA_CLIENT t " +
        "SET CLIENT_NAME = ?, CLIENT_NAME_EN = ?, APPLICATION_TYPE_CODE = ? " +
        "WHERE t.SHORT_NAME = ?";
      CallableStatement updateStatement = db.prepare(updateSqlQuery);

      updateStatement.setString(1, dto.getClientName());
      updateStatement.setString(2, dto.getClientNameEn());
      updateStatement.setString(3, dto.getApplicationType());
      updateStatement.setString(4, (String) primaryKey.get(CLIENT_ID));
      updateStatement.executeUpdate();
      /*
       update grantTypes
      */
      //language=Oracle
      String deleteGrantsSqlString = "delete from OA_CLIENT_GRANT_TYPE cgt " +
        "where cgt.client_id in (" +
        "select ct.client_id " +
        "from OA_CLIENT ct " +
        "where ct.SHORT_NAME = ?)";
      CallableStatement deleteGrantTypeStatement = db.prepare(deleteGrantsSqlString);
      deleteGrantTypeStatement.setString(1, (String) primaryKey.get(CLIENT_ID));
      deleteGrantTypeStatement.execute();

      //language=Oracle
      String insertGrantSqlString =
        "insert into OA_CLIENT_GRANT_TYPE (CLIENT_ID, GRANT_TYPE_CODE) " +
          "values ((select ct.client_id from OA_CLIENT ct where ct.SHORT_NAME = ?) ,?)";
      CallableStatement insertGrantTypeStatement = db.prepare(insertGrantSqlString);
      List<String> grantTypes;
      if (dto.getGrantTypes() != null && dto.getGrantTypes().size() > 0) {
        grantTypes = dto.getGrantTypes();
      } else {
        grantTypes = ApplicationType.getApplicationGrantTypes(dto.getApplicationType());
      }
      for (String grantType : grantTypes) {
        insertGrantTypeStatement.setString(1, (String) primaryKey.get(CLIENT_ID));
        insertGrantTypeStatement.setString(2, grantType);
        insertGrantTypeStatement.addBatch();
      }
      insertGrantTypeStatement.executeBatch();
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
      "WHERE t.SHORT_NAME = ?";
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


  @Override
  public List<String> getClientGrantTypes(String clientId) {
    //language=Oracle
    String sqlString = "select cgt.grant_type_code " +
      "from OA_CLIENT_GRANT_TYPE cgt " +
      "inner join OA_CLIENT clt on clt.CLIENT_ID = cgt.CLIENT_ID " +
      "where clt.SHORT_NAME like ?";
    Db db = getDb();
    CallableStatement statement = db.prepare(sqlString);
    List<String> result = new ArrayList<>();
    try {
      statement.setString(1, clientId);
      statement.executeQuery();
      ResultSet rs = statement.getResultSet();
      while (rs.next()) {
        result.add(rs.getString(GRANT_TYPE_CODE));
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
