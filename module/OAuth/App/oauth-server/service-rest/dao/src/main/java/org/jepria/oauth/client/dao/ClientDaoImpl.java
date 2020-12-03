package org.jepria.oauth.client.dao;

import org.jepria.compat.server.dao.ResultSetMapper;
import org.jepria.oauth.client.dto.ClientCreateDto;
import org.jepria.oauth.client.dto.ClientDto;
import org.jepria.oauth.client.dto.ClientSearchDto;
import org.jepria.oauth.client.dto.ClientUpdateDto;
import org.jepria.server.data.DaoSupport;
import org.jepria.server.data.DtoUtil;
import org.jepria.server.data.OptionDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jepria.oauth.client.ClientFieldNames.*;

public class ClientDaoImpl implements ClientDao {

  @Override
  public List<?> find(Object template, Integer operatorId) {
    ClientSearchDto searchTemplate = (ClientSearchDto) template;
    String sqlQuery =
      "begin  "
        + "? := pkg_OAuth.findClient("
        + "clientShortName => ? "
        + ", clientName => ? "
        + ", clientNameEn => ? "
        + ", maxRowCount => ? "
        + ", operatorId => ? "
        + ");"
        + " end;";
    List<ClientDto> records = null;
    records = DaoSupport.getInstance().find(sqlQuery,
      new ResultSetMapper<ClientDto>() {
        @Override
        public void map(ResultSet rs, ClientDto dto) throws SQLException {
          dto.setClientId(rs.getString(CLIENT_SHORT_NAME));
          dto.setClientSecret(rs.getString(CLIENT_SECRET));
          dto.setClientName(rs.getString(CLIENT_NAME));
          dto.setClientNameEn(rs.getString(CLIENT_NAME_EN));
          dto.setApplicationType(rs.getString(APPLICATION_TYPE));
          dto.setGrantTypes(getClientGrantTypes(dto.getClientId(), operatorId));
          Integer clientOperatorId = getInteger(rs, OPERATOR_ID);
          if (clientOperatorId != null) dto.setScope(getClientRoles(clientOperatorId, operatorId));
        }
      }
      , ClientDto.class
      , searchTemplate.getClientId()
      , DtoUtil.startsWith(searchTemplate.getClientName())
      , DtoUtil.startsWith(searchTemplate.getClientNameEn())
      , searchTemplate.getMaxRowCount()
      , operatorId);
    return records;
  }

  @Override
  public List<ClientDto> findByPrimaryKey(Map<String, ?> primaryKeyMap, final Integer operatorId) {
    String sqlQuery =
      "begin  "
        + "? := pkg_OAuth.findClient("
        + "clientShortName => ? "
        + ", clientName => ? "
        + ", clientNameEn => ? "
        + ", maxRowCount => ? "
        + ", operatorId => ? "
        + ");"
        + " end;";
    List<ClientDto> records = null;
    records = DaoSupport.getInstance().find(sqlQuery,
      new ResultSetMapper<ClientDto>() {
        @Override
        public void map(ResultSet rs, ClientDto dto) throws SQLException {
          dto.setClientId(rs.getString(CLIENT_SHORT_NAME));
          dto.setClientSecret(rs.getString(CLIENT_SECRET));
          dto.setClientName(rs.getString(CLIENT_NAME));
          dto.setClientNameEn(rs.getString(CLIENT_NAME_EN));
          dto.setApplicationType(rs.getString(APPLICATION_TYPE));
          dto.setGrantTypes(getClientGrantTypes(dto.getClientId(), operatorId));
          Integer clientOperatorId = getInteger(rs, OPERATOR_ID);
          if (clientOperatorId != null) dto.setScope(getClientRoles(getInteger(rs, OPERATOR_ID), operatorId));
        }
      }
      , ClientDto.class
      , primaryKeyMap.get(CLIENT_ID)
      , null
      , null
      , null
      , operatorId);
    return records;
  }

  @Override
  public Object create(Object record, Integer operatorId) {
    ClientCreateDto dto = (ClientCreateDto) record;
    String sqlQuery =
      "begin "
        + "? := pkg_OAuth.createClient("
        + "clientShortName => ? "
        + ", clientName => ? "
        + ", clientNameEn => ? "
        + ", applicationType => ? "
        + ", grantTypeList => ? "
        + ", roleShortNameList => ? "
        + ", operatorId => ? "
        + ");"
        + "end;";
    DaoSupport.getInstance().create(sqlQuery,
      String.class
      , dto.getClientId()
      , dto.getClientName()
      , dto.getClientNameEn()
      , dto.getApplicationType()
      , dto.getGrantTypes() != null ? dto.getGrantTypes().stream().collect(Collectors.joining(",")) : null
      , dto.getScope() != null ? dto.getScope().stream().collect(Collectors.joining(",")) : null
      , operatorId
    );
    return dto.getClientId();
  }

  @Override
  public void update(Map<String, ?> primaryKey, Object record, Integer operatorId) {
    ClientUpdateDto dto = (ClientUpdateDto) record;
    String sqlQuery =
      "begin "
        + "pkg_OAuth.updateClient("
        + "clientShortName => ? "
        + ", clientName => ? "
        + ", clientNameEn => ? "
        + ", applicationType => ? "
        + ", grantTypeList => ? "
        + ", roleShortNameList => ? "
        + ", operatorId => ? "
        + ");"
        + "end;";
    DaoSupport.getInstance().update(sqlQuery
      , primaryKey.get(CLIENT_ID)
      , dto.getClientName()
      , dto.getClientNameEn()
      , dto.getApplicationType()
      , dto.getGrantTypes() != null ? dto.getGrantTypes().stream().collect(Collectors.joining(",")) : null
      , dto.getScope() != null ? dto.getScope().stream().collect(Collectors.joining(",")) : null
      , operatorId
    );
  }

  @Override
  public void delete(Map<String, ?> primaryKey, Integer operatorId) {
    String sqlQuery =
      "begin "
        + "pkg_OAuth.deleteClient("
        + "clientShortName => ? "
        + ", operatorId => ? "
        + ");"
        + "end;";
    DaoSupport.getInstance().delete(sqlQuery
      , primaryKey.get(CLIENT_ID)
      , operatorId
    );
  }

  @Override
  public List<String> getClientGrantTypes(String clientId, Integer operatorId) {
    String sqlQuery =
      "begin  "
        + "? := pkg_OAuth.getClientGrant("
        + "clientShortName => ? "
        + ", operatorId => ? "
        + ");"
        + " end;";
    List<OptionDto<String>> grantTypes = DaoSupport.getInstance().find(sqlQuery,
      new ResultSetMapper<OptionDto<String>>() {
        @Override
        public void map(ResultSet rs, OptionDto<String> result) throws SQLException {
          result.setValue(rs.getString(GRANT_TYPE));
        }
      }
      , OptionDto.class
      , clientId
      , operatorId);
    return grantTypes.stream().map(OptionDto::getValue).collect(Collectors.toList());
  }

  @Override
  public List<OptionDto<String>> getRoles(String roleName, String roleNameEn, Integer maxRowCount, Integer operatorId) {
    String sqlQuery =
      "begin  "
        + "? := pkg_OAuth.getRoles("
        + "roleName => ? "
        + ", roleNameEn => ? "
        + ", maxRowCount => ? "
        + ", operatorId => ? "
        + ");"
        + " end;";
    List<OptionDto<String>> result = null;
    result = DaoSupport.getInstance().find(sqlQuery,
      new ResultSetMapper<OptionDto<String>>() {
        @Override
        public void map(ResultSet rs, OptionDto<String> result) throws SQLException {
          result.setValue(rs.getString(SHORT_NAME));
          result.setName(rs.getString(ROLE_NAME));
        }
      }
      , OptionDto.class
      , DtoUtil.startsWith(roleName)
      , DtoUtil.startsWith(roleNameEn)
      , maxRowCount
      , operatorId);
    return result;
  }

  private List<OptionDto<String>> getClientRoles(Integer clientOperatorId, Integer operatorId) {
    String sqlQuery =
      "begin  "
        + "? := pkg_operator.getRoles("
          + "operatorId => ? "
//          + ", operatorIdIns => ? "
        + ");"
        + " end;";
    List<OptionDto<String>> result;
    result = DaoSupport.getInstance().find(sqlQuery,
      new ResultSetMapper<OptionDto<String>>() {
        @Override
        public void map(ResultSet rs, OptionDto<String> result) throws SQLException {
          result.setValue(rs.getString(SHORT_NAME));
          result.setName(rs.getString(ROLE_NAME));
        }
      }
      , OptionDto.class
      , clientOperatorId);
    return result;
  }
}
