package org.jepria.oauth.session;

import org.jepria.compat.server.dao.CallContext;
import org.jepria.oauth.DaoTestBase;
import org.jepria.oauth.session.dao.SessionDaoImpl;
import org.jepria.oauth.session.dto.SessionCreateDto;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDto;
import org.jepria.oauth.session.dto.SessionUpdateDto;
import org.jepria.server.data.Dao;
import org.junit.jupiter.api.*;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import static org.jepria.oauth.session.SessionFieldNames.SESSION_ID;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SessionDaoIT extends DaoTestBase {

  Logger logger = Logger.getLogger(SessionDaoIT.class.getCanonicalName());

  private Dao dao;

  @BeforeEach
  public void beforeTest() {
    dao = new SessionDaoImpl();
  }

  @AfterEach
  public void afterTest() {
    dao = null;
  }

  @Test
  public void sessionControlTest() throws SQLException {
    try {
      CallContext.begin(properties.getProperty("datasource.jndiName"), SessionDaoIT.class.getCanonicalName());
      //create
      Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
      SecureRandom sr = new SecureRandom();
      SessionCreateDto sessionCreateDto = new SessionCreateDto();
      byte[] authCodeBytes = new byte[16];
      sr.nextBytes(authCodeBytes);
      String authCode = encoder.encodeToString(authCodeBytes);
      sessionCreateDto.setAuthorizationCode(authCode);
      sessionCreateDto.setClientId(properties.getProperty("client.id"));
      sessionCreateDto.setOperatorId(1);
      sessionCreateDto.setRedirectUri(properties.getProperty("client.redirect_uri"));
      byte[] accessTokenId = new byte[16];
      sr.nextBytes(accessTokenId);
      sessionCreateDto.setAccessTokenId(encoder.encodeToString(accessTokenId));
      sessionCreateDto.setAccessTokenDateIns(new Date());
      sessionCreateDto.setAccessTokenDateFinish(new Date(new Date().getTime() + 1000000));
      byte[] refreshTokenId = new byte[16];
      sr.nextBytes(refreshTokenId);
      sessionCreateDto.setRefreshTokenId(encoder.encodeToString(refreshTokenId));
      sessionCreateDto.setRefreshTokenDateIns(new Date());
      sessionCreateDto.setRefreshTokenDateFinish(new Date(new Date().getTime() + 1000000));
      byte[] sessionTokenId = new byte[16];
      sr.nextBytes(sessionTokenId);
      sessionCreateDto.setSessionTokenId(encoder.encodeToString(sessionTokenId));
      sessionCreateDto.setSessionTokenDateIns(new Date());
      sessionCreateDto.setSessionTokenDateFinish(new Date(new Date().getTime() + 1000000));
      byte[] codeChallenge = new byte[64];
      sr.nextBytes(codeChallenge);
      sessionCreateDto.setCodeChallenge(encoder.encodeToString(codeChallenge));
      String sessionId = (String) dao.create(sessionCreateDto, 1);
      SessionDto sessionDto = (SessionDto) dao.findByPrimaryKey(new HashMap<String, String>() {{
        put(SESSION_ID, sessionId);
      }}, 1).get(0);
      assertEquals(sessionDto.getAuthorizationCode(), sessionCreateDto.getAuthorizationCode());
      assertEquals(sessionDto.getClient().getValue(), sessionCreateDto.getClientId());
      assertEquals(sessionDto.getOperator().getValue(), sessionCreateDto.getOperatorId());
      assertEquals(sessionDto.getRedirectUri(), sessionCreateDto.getRedirectUri());
      assertEquals(sessionDto.getAccessTokenId(), sessionCreateDto.getAccessTokenId());
      assertEquals(sessionDto.getRefreshTokenId(), sessionCreateDto.getRefreshTokenId());
      assertEquals(sessionDto.getSessionTokenId(), sessionCreateDto.getSessionTokenId());
      assertEquals(sessionDto.getCodeChallenge(), sessionCreateDto.getCodeChallenge());

      //update
      SessionUpdateDto sessionUpdateDto = new SessionUpdateDto();
      sessionUpdateDto.setAuthorizationCode(sessionDto.getAuthorizationCode());
      sessionUpdateDto.setCodeChallenge(sessionDto.getCodeChallenge());
      sessionUpdateDto.setRedirectUri(sessionDto.getRedirectUri());
      sessionUpdateDto.setClientId(sessionDto.getClient().getValue());
      sessionUpdateDto.setAuthorizationCode(sessionDto.getAuthorizationCode());
      sessionUpdateDto.setOperatorId(7);
      byte[] newAccessTokenId = new byte[16];
      sr.nextBytes(accessTokenId);
      sessionUpdateDto.setAccessTokenId(encoder.encodeToString(newAccessTokenId));
      sessionUpdateDto.setAccessTokenDateIns(new Date());
      sessionUpdateDto.setAccessTokenDateFinish(new Date(new Date().getTime() + 1000000));
      byte[] newRefreshTokenId = new byte[16];
      sr.nextBytes(refreshTokenId);
      sessionUpdateDto.setRefreshTokenId(encoder.encodeToString(newRefreshTokenId));
      sessionUpdateDto.setRefreshTokenDateIns(new Date());
      sessionUpdateDto.setRefreshTokenDateFinish(new Date(new Date().getTime() + 1000000));
      byte[] newSessionTokenId = new byte[16];
      sr.nextBytes(sessionTokenId);
      sessionUpdateDto.setSessionTokenId(encoder.encodeToString(newSessionTokenId));
      sessionUpdateDto.setSessionTokenDateIns(new Date());
      sessionUpdateDto.setSessionTokenDateFinish(new Date(new Date().getTime() + 1000000));
      dao.update(new HashMap<String, String>() {{
        put(SESSION_ID, sessionId);
      }}, sessionUpdateDto, 1);

      //search
      SessionSearchDto sessionSearchDto = new SessionSearchDto();
      sessionSearchDto.setSessionId(sessionId);
      sessionSearchDto.setAuthorizationCode(authCode);
      sessionSearchDto.setClientId(properties.getProperty("client.id"));
      sessionSearchDto.setOperatorId(7);
      sessionSearchDto.setRedirectUri(properties.getProperty("client.redirect_uri"));
      List<SessionDto> result = (List<SessionDto>) dao.find(sessionSearchDto, 1);
      assertFalse(result.isEmpty());
      assertTrue(result.size() == 1);
      assertEquals(result.get(0).getOperator().getValue(), sessionUpdateDto.getOperatorId());
      assertEquals(result.get(0).getAccessTokenId(), sessionUpdateDto.getAccessTokenId());
      assertEquals(result.get(0).getRefreshTokenId(), sessionUpdateDto.getRefreshTokenId());
      assertEquals(result.get(0).getSessionTokenId(), sessionUpdateDto.getSessionTokenId());

      //block
      dao.delete(new HashMap<String, String>() {{
        put(SESSION_ID, sessionId);
      }}, 1);
      result = (List<SessionDto>) dao.findByPrimaryKey(new HashMap<String, String>() {{
        put(SESSION_ID, sessionId);
      }}, 1);
      assertTrue(result.isEmpty());
    } finally {
      CallContext.rollback();
      CallContext.end();
    }
  }

}
