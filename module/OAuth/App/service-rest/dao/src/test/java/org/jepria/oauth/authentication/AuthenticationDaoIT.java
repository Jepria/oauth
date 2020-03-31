package org.jepria.oauth.authentication;

import org.jepria.oauth.DaoTestBase;
import org.jepria.oauth.dao.authentication.AuthenticationDaoImpl;
import org.jepria.oauth.dao.session.SessionDaoImpl;
import org.jepria.oauth.model.authentication.dao.AuthenticationDao;
import org.jepria.oauth.model.session.dto.SessionCreateDto;
import org.jepria.server.data.Dao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;

import static org.jepria.oauth.dao.session.SessionFieldNames.SESSION_ID;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthenticationDaoIT extends DaoTestBase {
  
  AuthenticationDao dao;
  
  @BeforeEach
  public void beforeTest(){
    dao = new AuthenticationDaoImpl(properties.getProperty("datasource.jndiName"));
  }
  
  @AfterEach
  public void afterTest() {
    dao = null;
  }
  
  @Test
  public void clientAuthTest() {
    dao.loginByClientSecret(properties.getProperty("client.id"), properties.getProperty("client.secret"));
  }
  
  @Test
  public void pkceTest() throws NoSuchAlgorithmException {
    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    SecureRandom sr = SecureRandom.getInstanceStrong();
    byte[] codeVerifierBytes = new byte[32];
    sr.nextBytes(codeVerifierBytes);
    String codeVerifier = encoder.encodeToString(codeVerifierBytes);
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    String codeChallenge = encoder.encodeToString(md.digest(codeVerifier.getBytes()));
    Dao sessionDao = new SessionDaoImpl(properties.getProperty("datasource.jndiName"));
    SessionCreateDto sessionCreateDto = new SessionCreateDto();
    byte[] authCodeBytes = new byte[16];
    sr.nextBytes(authCodeBytes);
    String authCode = encoder.encodeToString(authCodeBytes);
    sessionCreateDto.setAuthorizationCode(authCode);
    sessionCreateDto.setCodeChallenge(codeChallenge);
    Integer sessionId = (Integer) sessionDao.create(sessionCreateDto, 1);
    Boolean result = dao.verifyPKCE(authCode, codeVerifier);
    sessionDao.delete(new HashMap<String, Integer>(){{
      put(SESSION_ID, sessionId);
    }}, 1);
    assertTrue(result);
  }
  
}
