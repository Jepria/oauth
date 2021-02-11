package org.jepria.oauth.authentication;

import org.jepria.compat.server.dao.CallContext;
import org.jepria.oauth.DaoTestBase;
import org.jepria.oauth.authentication.dao.AuthenticationDao;
import org.jepria.oauth.authentication.dao.AuthenticationDaoImpl;
//import org.jepria.oauth.session.dao.SessionDaoImpl;
import org.jepria.oauth.session.dto.SessionCreateDto;
import org.jepria.server.data.Dao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;

//import static org.jepria.oauth.session.SessionFieldNames.SESSION_ID;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthenticationDaoIT extends DaoTestBase {

  AuthenticationDao dao;

  @BeforeEach
  public void beforeTest() {
    dao = new AuthenticationDaoImpl();
  }

  @AfterEach
  public void afterTest() {
    dao = null;
  }

  @Test
  public void clientAuthTest() throws SQLException {
    try {
      CallContext.begin(properties.getProperty("datasource.jndiName"), AuthenticationDaoIT.class.getCanonicalName());
      dao.loginByClientSecret(properties.getProperty("client.id"), properties.getProperty("client.secret"));
    } finally {
      CallContext.rollback();
      CallContext.end();
    }
  }

  @Test
  public void pkceTest() throws NoSuchAlgorithmException, SQLException {
    try {
      CallContext.begin(properties.getProperty("datasource.jndiName"), AuthenticationDaoIT.class.getCanonicalName());
      Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
      SecureRandom sr = SecureRandom.getInstanceStrong();
      byte[] codeVerifierBytes = new byte[32];
      sr.nextBytes(codeVerifierBytes);
      String codeVerifier = encoder.encodeToString(codeVerifierBytes);
      
      MessageDigest cryptoProvider = MessageDigest.getInstance("SHA-256");
      byte[] hash = cryptoProvider.digest(codeVerifier.getBytes());
      StringBuffer hexString = new StringBuffer();
      for (int i = 0; i < hash.length; i++) {
        String hex = Integer.toHexString(0xff & hash[i]);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
      }
      String codeChallenge = hexString.toString();
      
//      Dao sessionDao = new SessionDaoImpl();
//      SessionCreateDto sessionCreateDto = new SessionCreateDto();
//      byte[] authCodeBytes = new byte[16];
//      sr.nextBytes(authCodeBytes);
//      String authCode = encoder.encodeToString(authCodeBytes);
//      sessionCreateDto.setAuthorizationCode(authCode);
//      sessionCreateDto.setClientId(properties.getProperty("client.id"));
//      sessionCreateDto.setCodeChallenge(codeChallenge);
//
//      Integer sessionId = (Integer) sessionDao.create(sessionCreateDto, 1);
//
//      Boolean result = dao.verifyPKCE(authCode, codeVerifier);
//
//      sessionDao.delete(new HashMap<String, Integer>() {{
//        put(SESSION_ID, sessionId);
//      }}, 1);
//      assertTrue(result);
    } finally {
      CallContext.rollback();
      CallContext.end();
    }
  }

}
