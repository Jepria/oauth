package org.jepria.oauth.key.dao;

import com.technology.jep.jepria.server.dao.ResultSetMapper;
import com.technology.jep.jepria.server.db.Db;
import org.jepria.oauth.key.dto.KeyCreateDto;
import org.jepria.oauth.key.dto.KeyDto;
import org.jepria.server.data.RuntimeSQLException;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.jepria.oauth.key.KeyFieldNames.*;

public class KeyDaoImpl implements KeyDao {
  
  private String jndiName = "jdbc/RFInfoDS";

  public KeyDaoImpl(){};
  
  public KeyDaoImpl(String jndName) {
    this.jndiName = jndName;
  }

  private Db getDb() {
    return new Db(jndiName);
  }
  
  private ResultSetMapper mapper = new ResultSetMapper<KeyDto>() {
    @Override
    public void map(ResultSet rs, KeyDto dto) throws SQLException {
      dto.setKeyId(rs.getString(KEY_ID));
      dto.setPublicKey(rs.getString(PUBLIC_KEY));
      dto.setPrivateKey(rs.getString(PRIVATE_KEY));
      dto.setActual(rs.getInt(IS_ACTUAL) == 1 ? Boolean.TRUE : Boolean.FALSE);
      dto.setDateIns(rs.getTimestamp(DATE_INS));
    }
  };
  
  @Override
  public String setKeys(KeyCreateDto keyCreateDto, Integer operatorId) {
    Db db = getDb();
    try {
      //language=Oracle
      String updateSqlQuery = "update oa_key t " +
          "set t.IS_ACTUAL = 0 " +
          "where t.IS_ACTUAL = 1";
      CallableStatement updateStatement = db.prepare(updateSqlQuery);
      updateStatement.executeUpdate();
      //language=Oracle
      String insertSqlQuery = "insert into oa_key (PUBLIC_KEY, PRIVATE_KEY) " +
          "values (?, ?)";
      CallableStatement insertStatement = db.prepare(insertSqlQuery);
      insertStatement.setString(1, keyCreateDto.getPublicKey());
      insertStatement.setString(2, keyCreateDto.getPrivateKey());
      int addedRecordCount = insertStatement.executeUpdate();
      String result = null;
      if (addedRecordCount == 1) {
        //language=Oracle
        String sqlGetIndex = "select oa_key_seq.currval from dual";
        CallableStatement getIndexStatement = db.prepare(sqlGetIndex);
        ResultSet rs = getIndexStatement.executeQuery();
        if (rs.next()) {
          result = String.valueOf(rs.getInt(1));
        }
      }
      return result;
    } catch (SQLException sqlException) {
      sqlException.printStackTrace();
      throw new RuntimeSQLException(sqlException);
    } finally {
      db.closeAll();
    }
  }
  
  @Override
  public KeyDto getKeys(String keyId, Integer operatorId) {
    //language=Oracle
    String sqlQuery = "select * from oa_key t where t.KEY_ID = ? or t.IS_ACTUAL = 1";
    Db db = getDb();
    try {
      CallableStatement statement = db.prepare(sqlQuery);
      statement.setString(1, keyId);
      List<KeyDto> keys = new ArrayList<>();
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          KeyDto keyDto = new KeyDto();
          mapper.map(rs, keyDto);
          keys.add(keyDto);
        }
      }
      if (keys.size() == 1) {
        return keys.get(0);
      } else {
        return null;
      }
    } catch (SQLException sqlException) {
      sqlException.printStackTrace();
      throw new RuntimeSQLException(sqlException);
    } finally {
      db.closeAll();
    }
    
  }
}
