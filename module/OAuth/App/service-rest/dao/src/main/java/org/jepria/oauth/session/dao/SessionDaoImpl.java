package org.jepria.oauth.session.dao;

import org.jepria.compat.server.dao.ResultSetMapper;
import org.jepria.oauth.session.OperatorOptions;
import org.jepria.oauth.session.dto.SessionCreateDto;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDto;
import org.jepria.oauth.session.dto.SessionUpdateDto;
import org.jepria.server.data.DaoSupport;
import org.jepria.server.data.OptionDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.jepria.oauth.session.SessionFieldNames.*;

public class SessionDaoImpl implements SessionDao {

  @Override
  public List<SessionDto> find(Object template, Integer operatorId) {

    SessionSearchDto searchTemplate = (SessionSearchDto) template;
    String sqlQuery =
      "begin  "
        + "? := pkg_OAuth.findSession("
        + "sessionId => ? "
        + ", authCode => ? "
        + ", clientShortName => ? "
        + ", redirectUri => ? "
        + ", operatorId => ? "
        + ", codeChallenge => '' "
        + ", accessToken => ? "
        + ", refreshToken => ? "
        + ", sessionToken => ? "
        + ", maxRowCount => ? "
        + ", operatorIdIns => ? "
        + ");"
        + " end;";
    List<SessionDto> records = null;
    records = DaoSupport.getInstance().find(sqlQuery,
      new ResultSetMapper<SessionDto>() {
        @Override
        public void map(ResultSet rs, SessionDto dto) throws SQLException {
          dto.setSessionId(getInteger(rs, SESSION_ID));
          dto.setAuthorizationCode(rs.getString(AUTHORIZATION_CODE));
          dto.setDateIns(getTimestamp(rs, DATE_INS));
          dto.setRedirectUri(rs.getString(REDIRECT_URI));
          OptionDto<Integer> operator = new OptionDto<>();
//          operator.setName(rs.getString(OPERATOR_NAME)); TODO
          operator.setName("test");
          operator.setValue(getInteger(rs, OPERATOR_ID));
          dto.setOperator(operator.getValue() != null ? operator : null);
          dto.setOperatorLogin("test");
//          dto.setOperatorLogin(rs.getString(OPERATOR_LOGIN)); TODO
          OptionDto<String> client = new OptionDto<>();
          client.setValue(rs.getString(CLIENT_SHORT_NAME));
//          client.setName(rs.getString(CLIENT_NAME));
          client.setName("test");
          dto.setClient(client);
          dto.setAccessTokenId(rs.getString(ACCESS_TOKEN_ID));
          dto.setAccessTokenDateIns(getTimestamp(rs, ACCESS_TOKEN_DATE_INS));
          dto.setAccessTokenDateFinish(getTimestamp(rs, ACCESS_TOKEN_DATE_FINiSH));
          dto.setSessionTokenId(rs.getString(SESSION_TOKEN_ID));
          dto.setSessionTokenDateIns(getTimestamp(rs, SESSION_TOKEN_DATE_INS));
          dto.setSessionTokenDateFinish(getTimestamp(rs, SESSION_TOKEN_DATE_FINISH));
          dto.setRefreshTokenId(rs.getString(REFRESH_TOKEN_ID));
          dto.setRefreshTokenDateIns(getTimestamp(rs, REFRESH_TOKEN_DATE_INS));
          dto.setRefreshTokenDateFinish(getTimestamp(rs, REFRESH_TOKEN_DATE_FINISH));
          dto.setCodeChallenge(rs.getString(CODE_CHALLENGE));
        }
      }
      , SessionDto.class
      , searchTemplate.getSessionId()
      , searchTemplate.getAuthorizationCode()
      , searchTemplate.getClientId()
      , searchTemplate.getRedirectUri()
      , searchTemplate.getOperatorId()
      , searchTemplate.getAccessTokenId()
      , searchTemplate.getRefreshTokenId()
      , searchTemplate.getSessionTokenId()
      , searchTemplate.getMaxRowCount()
      , operatorId);
    return records;
  }

  @Override
  public List<SessionDto> findByPrimaryKey(Map<String, ?> primaryKeyMap, Integer operatorId) {

    String sqlQuery =
      "begin  "
        + "? := pkg_OAuth.findSession("
        + "sessionId => ? "
        + ", authCode => ? "
        + ", clientShortName => ? "
        + ", redirectUri => ? "
        + ", operatorId => ? "
        + ", codeChallenge => ? "
        + ", accessToken => ? "
        + ", refreshToken => ? "
        + ", sessionToken => ? "
        + ", maxRowCount => ? "
        + ", operatorIdIns => ? "
        + ");"
        + " end;";
    List<SessionDto> records = null;
    records = DaoSupport.getInstance().find(sqlQuery,
      new ResultSetMapper<SessionDto>() {
        @Override
        public void map(ResultSet rs, SessionDto dto) throws SQLException {
          dto.setSessionId(getInteger(rs, SESSION_ID));
          dto.setAuthorizationCode(rs.getString(AUTHORIZATION_CODE));
          dto.setDateIns(getTimestamp(rs, DATE_INS));
          dto.setRedirectUri(rs.getString(REDIRECT_URI));
          OptionDto<Integer> operator = new OptionDto<>();
//          operator.setName(rs.getString(OPERATOR_NAME)); TODO
          operator.setName("test");
          operator.setValue(getInteger(rs, OPERATOR_ID));
          dto.setOperator(operator.getValue() != null ? operator : null);
          dto.setOperatorLogin("test");
//          dto.setOperatorLogin(rs.getString(OPERATOR_LOGIN)); TODO
          OptionDto<String> client = new OptionDto<>();
          client.setValue(rs.getString(CLIENT_SHORT_NAME));
//          client.setName(rs.getString(CLIENT_NAME));
          client.setName("test");
          dto.setClient(client);
          dto.setAccessTokenId(rs.getString(ACCESS_TOKEN_ID));
          dto.setAccessTokenDateIns(getTimestamp(rs, ACCESS_TOKEN_DATE_INS));
          dto.setAccessTokenDateFinish(getTimestamp(rs, ACCESS_TOKEN_DATE_FINiSH));
          dto.setSessionTokenId(rs.getString(SESSION_TOKEN_ID));
          dto.setSessionTokenDateIns(getTimestamp(rs, SESSION_TOKEN_DATE_INS));
          dto.setSessionTokenDateFinish(getTimestamp(rs, SESSION_TOKEN_DATE_FINISH));
          dto.setRefreshTokenId(rs.getString(REFRESH_TOKEN_ID));
          dto.setRefreshTokenDateIns(getTimestamp(rs, REFRESH_TOKEN_DATE_INS));
          dto.setRefreshTokenDateFinish(getTimestamp(rs, REFRESH_TOKEN_DATE_FINISH));
          dto.setCodeChallenge(rs.getString(CODE_CHALLENGE));
        }
      }
      , SessionDto.class
      , primaryKeyMap.get(SESSION_ID)
      , null
      , null
      , null
      , null
      , null
      , null
      , null
      , null
      , null
      , operatorId);
    return records;
  }

  @Override
  public Object create(Object record, Integer operatorId) {
    SessionCreateDto dto = (SessionCreateDto) record;

    String sqlQuery =
      "begin  "
        + "? := pkg_OAuth.createSession("
        + "authCode => ? "
        + ", clientShortName => ? "
        + ", redirectUri => ? "
        + ", operatorId => ? "
        + ", codeChallenge => ? "
        + ", accessToken => ? "
        + ", accessTokenDateIns => ? "
        + ", accessTokenDateFinish => ? "
        + ", refreshToken => ? "
        + ", refreshTokenDateIns => ? "
        + ", refreshTokenDateFinish => ? "
        + ", sessionToken => ? "
        + ", sessionTokenDateIns => ? "
        + ", sessionTokenDateFinish => ? "
        + ", operatorIdIns => ? "
        + ");"
        + " end;";
    return DaoSupport.getInstance().create(sqlQuery
      , Integer.class
      , dto.getAuthorizationCode()
      , dto.getClientId()
      , dto.getRedirectUri()
      , dto.getOperatorId()
      , dto.getCodeChallenge()
      , dto.getAccessTokenId()
      , dto.getAccessTokenDateIns()
      , dto.getAccessTokenDateFinish()
      , dto.getRefreshTokenId()
      , dto.getRefreshTokenDateIns()
      , dto.getRefreshTokenDateFinish()
      , dto.getSessionTokenId()
      , dto.getSessionTokenDateIns()
      , dto.getSessionTokenDateFinish()
      , operatorId);
  }

  @Override
  public void update(Map<String, ?> primaryKey, Object record, Integer operatorId) {
    SessionUpdateDto dto = (SessionUpdateDto) record;
    String sqlQuery =
      "begin  "
        + "pkg_OAuth.updateSession("
        + "sessionId => ? "
        + ", authCode => ? "
        + ", clientShortName => ? "
        + ", redirectUri => ? "
        + ", operatorId => ? "
        + ", codeChallenge => ? "
        + ", accessToken => ? "
        + ", accessTokenDateIns => ? "
        + ", accessTokenDateFinish => ? "
        + ", refreshToken => ? "
        + ", refreshTokenDateIns => ? "
        + ", refreshTokenDateFinish => ? "
        + ", sessionToken => ? "
        + ", sessionTokenDateIns => ? "
        + ", sessionTokenDateFinish => ? "
        + ", operatorIdIns => ? "
        + ");"
        + " end;";
    DaoSupport.getInstance().update(sqlQuery
      , primaryKey.get(SESSION_ID)
      , dto.getAuthorizationCode()
      , dto.getClientId()
      , dto.getRedirectUri()
      , dto.getOperatorId()
      , dto.getCodeChallenge()
      , dto.getAccessTokenId()
      , dto.getAccessTokenDateIns()
      , dto.getAccessTokenDateFinish()
      , dto.getRefreshTokenId()
      , dto.getRefreshTokenDateIns()
      , dto.getRefreshTokenDateFinish()
      , dto.getSessionTokenId()
      , dto.getSessionTokenDateIns()
      , dto.getSessionTokenDateFinish()
      , operatorId);
  }

  @Override
  public void delete(Map<String, ?> primaryKey, Integer operatorId) {
    String sqlQuery =
      "begin  "
        + "pkg_OAuth.blockSession("
        + "sessionId => ? "
        + ", operatorId => ? "
        + ");"
        + " end;";
    DaoSupport.getInstance().delete(sqlQuery
      , primaryKey.get(SESSION_ID)
      , operatorId);
  }

  @Override
  public List<OptionDto<String>> getOperators(String operatorName, Integer maxRowCount) {
    String sqlQuery =
        " begin "
            + " ? := pkg_operator.getOperator("
            + "operatorName => ?"
            + ", maxRowCount => ? "
            + ");"
            + " end;";

    return DaoSupport.getInstance().find(
        sqlQuery,
        new ResultSetMapper<OptionDto<String>>() {
          public void map(ResultSet rs, OptionDto<String> dto) throws SQLException {
            dto.setValue(rs.getString(OperatorOptions.OPERATOR_ID));
            dto.setName(rs.getString(OperatorOptions.OPERATOR_NAME));
          }
        },
        OptionDto.class,
        operatorName + "%",
        maxRowCount
    );
  }
}
