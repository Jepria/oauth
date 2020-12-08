package org.jepria.oauth.key.dao;

import org.jepria.compat.server.dao.ResultSetMapper;
import org.jepria.oauth.key.dto.KeyCreateDto;
import org.jepria.oauth.key.dto.KeyDto;
import org.jepria.server.data.DaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.jepria.oauth.key.KeyFieldNames.*;

public class KeyDaoImpl implements KeyDao {
  
  @Override
  public String setKeys(KeyCreateDto keyCreateDto, Integer operatorId) {
    String sqlQuery =
      "begin  "
        + "? := pkg_OAuth.setKey("
        + "publicKey => ? "
        + ", privateKey => ? "
        + ", operatorId => ? "
        + ");"
        + " end;";
    return DaoSupport.getInstance().create(sqlQuery
      , String.class
      , keyCreateDto.getPublicKey()
      , keyCreateDto.getPrivateKey()
      , operatorId);
  }
  
  @Override
  public KeyDto getKeys(String keyId, Integer operatorId) {
    String sqlQuery =
      "begin  "
        + "? := pkg_OAuth.getKey("
        + "isExpired => ? "
        + ", operatorId => ? "
        + ");"
        + " end;";
    List<KeyDto> records = null;
    records = DaoSupport.getInstance().find(sqlQuery,
      new ResultSetMapper<KeyDto>() {
        @Override
        public void map(ResultSet rs, KeyDto dto) throws SQLException {
          dto.setKeyId(rs.getString(KEY_ID));
          dto.setPublicKey(rs.getString(PUBLIC_KEY));
          dto.setPrivateKey(rs.getString(PRIVATE_KEY));
          dto.setActual(rs.getInt(IS_EXPIRED) == 1 ? Boolean.FALSE : Boolean.TRUE);
          dto.setDateIns(rs.getTimestamp(DATE_INS));
        }
      }
      , KeyDto.class
      ,null
      , operatorId);
    if (records.size() == 1) {
      return records.get(0);
    } else {
      return null;
    }
  }
}
