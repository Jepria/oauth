package org.jepria.oauth.clienturi.dao;

import org.jepria.compat.server.dao.ResultSetMapper;
import org.jepria.compat.server.db.Db;
import org.jepria.oauth.clienturi.dto.ClientUriCreateDto;
import org.jepria.oauth.clienturi.dto.ClientUriDto;
import org.jepria.oauth.clienturi.dto.ClientUriSearchDto;
import org.jepria.server.data.Dao;
import org.jepria.server.data.DaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.jepria.oauth.clienturi.ClientUriFieldNames.*;


public class ClientUriDaoImpl implements Dao {

  @Override
  public List<ClientUriDto> find(Object template, Integer operatorId) {
    ClientUriSearchDto searchTemplate = (ClientUriSearchDto) template;
    String sqlQuery =
      "begin  "
        + "? := pkg_OAuth.findClientUri("
        + "clientUriId => ? "
        + ", clientShortName => ? "
        + ", operatorId => ? "
        + ");"
        + " end;";
    List<ClientUriDto> records = null;
    records = DaoSupport.getInstance().find(sqlQuery,
      new ResultSetMapper<ClientUriDto>() {
        @Override
        public void map(ResultSet rs, ClientUriDto dto) throws SQLException {
          dto.setClientUriId(getInteger(rs, CLIENT_URI_ID));
          dto.setClientUri(rs.getString(CLIENT_URI));
          dto.setClientId(rs.getString(CLIENT_SHORT_NAME));
        }
      }
      , ClientUriDto.class
      , searchTemplate.getClientUriId()
      , searchTemplate.getClientId()
      , operatorId);
    return records;
  }

  @Override
  public List<ClientUriDto> findByPrimaryKey(Map<String, ?> primaryKeyMap, Integer operatorId) {
    String sqlQuery =
      "begin  "
        + "? := pkg_OAuth.findClientUri("
        + "clientUriId => ? "
        + ", clientShortName => ? "
        + ", operatorId => ? "
        + ");"
        + " end;";
    List<ClientUriDto> records = null;
    records = DaoSupport.getInstance().find(sqlQuery,
      new ResultSetMapper<ClientUriDto>() {
        @Override
        public void map(ResultSet rs, ClientUriDto dto) throws SQLException {
          dto.setClientUriId(getInteger(rs, CLIENT_URI_ID));
          dto.setClientUri(rs.getString(CLIENT_URI));
          dto.setClientId(rs.getString(CLIENT_SHORT_NAME));
        }
      }
      , ClientUriDto.class
      , primaryKeyMap.get(CLIENT_URI_ID)
      , null
      , operatorId);
    return records;
  }

  @Override
  public Object create(Object record, Integer operatorId) {
    ClientUriCreateDto dto = (ClientUriCreateDto) record;
    String sqlQuery =
      "begin  "
        + "? := pkg_OAuth.createClientUri("
        + "clientShortName => ? "
        + ", clientUri => ? "
        + ", operatorId => ? "
        + ");"
        + " end;";
    return DaoSupport.getInstance().create(sqlQuery,
      Integer.class
      , dto.getClientId()
      , dto.getClientUri()
      , operatorId);
  }

  @Override
  public void update(Map<String, ?> primaryKey, Object record, Integer operatorId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(Map<String, ?> primaryKey, Integer operatorId) {
    String sqlQuery =
      "begin  "
        + "pkg_OAuth.deleteClientUri("
        + "clientUriId => ? "
        + ", operatorId => ? "
        + ");"
        + " end;";
    DaoSupport.getInstance().delete(sqlQuery
      , primaryKey.get(CLIENT_URI_ID)
      , operatorId);
  }
}
