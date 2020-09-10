package org.jepria.oauth.session;

import org.jepria.oauth.DaoTestBase;
import org.jepria.oauth.session.dto.SessionCreateDto;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDto;
import org.jepria.oauth.session.dto.SessionUpdateDto;
import org.jepria.oauth.session.dao.SessionDaoImpl;
import org.jepria.server.data.Dao;
import org.junit.jupiter.api.*;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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
  private static Integer sessionId;
  private static String authCode;
  
  @BeforeEach
  public void beforeTest(){
    dao = new SessionDaoImpl(properties.getProperty("datasource.jndiName"));
  }
  
  @AfterEach
  public void afterTest() {
    dao = null;
  }
  
  @Test
  @Order(1)
  public void createTest() throws NoSuchAlgorithmException {
    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    SecureRandom sr = SecureRandom.getInstanceStrong();
    SessionCreateDto sessionCreateDto = new SessionCreateDto();
    byte[] authCodeBytes = new byte[16];
    sr.nextBytes(authCodeBytes);
    authCode = encoder.encodeToString(authCodeBytes);
    sessionCreateDto.setAuthorizationCode(authCode);
    sessionCreateDto.setClientId(properties.getProperty("client.id"));
    sessionCreateDto.setOperatorId(1);
    sessionCreateDto.setRedirectUri(properties.getProperty("client.redirect_uri"));
    byte[] accessTokenId = new byte[16];
    sr.nextBytes(accessTokenId);
    sessionCreateDto.setAccessTokenId(encoder.encodeToString(accessTokenId));
    sessionCreateDto.setAccessTokenDateIns(new Date());
    sessionCreateDto.setAccessTokenDateFinish(new Date());
    byte[] refreshTokenId = new byte[16];
    sr.nextBytes(refreshTokenId);
    sessionCreateDto.setRefreshTokenId(encoder.encodeToString(refreshTokenId));
    sessionCreateDto.setRefreshTokenDateIns(new Date());
    sessionCreateDto.setRefreshTokenDateFinish(new Date());
    byte[] sessionTokenId = new byte[16];
    sr.nextBytes(sessionTokenId);
    sessionCreateDto.setSessionTokenId(encoder.encodeToString(sessionTokenId));
    sessionCreateDto.setSessionTokenDateIns(new Date());
    sessionCreateDto.setSessionTokenDateFinish(new Date());
    byte[] codeChallenge = new byte[64];
    sr.nextBytes(codeChallenge);
    sessionCreateDto.setCodeChallenge(encoder.encodeToString(codeChallenge));
    sessionId = (Integer) dao.create(sessionCreateDto, 1);
    SessionDto sessionDto = (SessionDto) dao.findByPrimaryKey(new HashMap<String, Integer>(){{
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
  }
  
  @Test
  @Order(2)
  public void findByIdTest() {
    SessionDto sessionDto = (SessionDto) dao.findByPrimaryKey(new HashMap<String, Integer>(){{
      put(SESSION_ID, sessionId);
    }}, 1).get(0);
    assertNotNull(sessionDto);
  }
  
  @Test
  @Order(3)
  public void updateTest() throws NoSuchAlgorithmException {
    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    SecureRandom sr = SecureRandom.getInstanceStrong();
    SessionUpdateDto sessionUpdateDto = new SessionUpdateDto();
    sessionUpdateDto.setOperatorId(7);
    byte[] accessTokenId = new byte[16];
    sr.nextBytes(accessTokenId);
    sessionUpdateDto.setAccessTokenId(encoder.encodeToString(accessTokenId));
    sessionUpdateDto.setAccessTokenDateIns(new Date());
    sessionUpdateDto.setAccessTokenDateFinish(new Date());
    byte[] refreshTokenId = new byte[16];
    sr.nextBytes(refreshTokenId);
    sessionUpdateDto.setRefreshTokenId(encoder.encodeToString(refreshTokenId));
    sessionUpdateDto.setRefreshTokenDateIns(new Date());
    sessionUpdateDto.setRefreshTokenDateFinish(new Date());
    byte[] sessionTokenId = new byte[16];
    sr.nextBytes(sessionTokenId);
    sessionUpdateDto.setSessionTokenId(encoder.encodeToString(sessionTokenId));
    sessionUpdateDto.setSessionTokenDateIns(new Date());
    sessionUpdateDto.setSessionTokenDateFinish(new Date());
    dao.update(new HashMap<String, Integer>(){{
      put(SESSION_ID, sessionId);
    }}, sessionUpdateDto, 1);
    SessionDto sessionDto = (SessionDto) dao.findByPrimaryKey(new HashMap<String, Integer>(){{
      put(SESSION_ID, sessionId);
    }}, 1).get(0);
    assertEquals(sessionDto.getOperator().getValue(), sessionUpdateDto.getOperatorId());
    assertEquals(sessionDto.getAccessTokenId(), sessionUpdateDto.getAccessTokenId());
    assertEquals(sessionDto.getRefreshTokenId(), sessionUpdateDto.getRefreshTokenId());
    assertEquals(sessionDto.getSessionTokenId(), sessionUpdateDto.getSessionTokenId());
  }
  
  @Test
  @Order(4)
  public void searchTest() {
    SessionSearchDto sessionSearchDto = new SessionSearchDto();
    sessionSearchDto.setSessionId(sessionId);
    sessionSearchDto.setAuthorizationCode(authCode);
    sessionSearchDto.setClientId(properties.getProperty("client.id"));
    sessionSearchDto.setOperatorId(7);
    sessionSearchDto.setRedirectUri(properties.getProperty("client.redirect_uri"));
    List<SessionDto> result = (List<SessionDto>) dao.find(sessionSearchDto, 1);
    assertFalse(result.isEmpty());
    assertTrue(result.size() == 1);
  }
  
  @Test
  @Order(5)
  public void deleteTest() {
    dao.delete(new HashMap<String, Integer>(){{
      put(SESSION_ID, sessionId);
    }}, 1);
    List<SessionDto> sessionDtoList = (List<SessionDto>) dao.findByPrimaryKey(new HashMap<String, Integer>(){{
      put(SESSION_ID, sessionId);
    }}, 1);
    assertTrue(sessionDtoList.isEmpty());
  }
  
}
