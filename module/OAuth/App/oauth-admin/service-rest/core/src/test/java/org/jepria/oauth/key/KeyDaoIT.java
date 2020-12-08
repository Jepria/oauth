package org.jepria.oauth.key;

import org.jepria.compat.server.dao.CallContext;
import org.jepria.oauth.DaoTestBase;
import org.jepria.oauth.key.dao.KeyDao;
import org.jepria.oauth.key.dao.KeyDaoImpl;
import org.jepria.oauth.key.dto.KeyCreateDto;
import org.jepria.oauth.key.dto.KeyDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyDaoIT extends DaoTestBase {

  Logger logger = Logger.getLogger(KeyDaoIT.class.getCanonicalName());

  private KeyDao dao;

  @BeforeEach
  public void beforeTest() {
    dao = new KeyDaoImpl();
  }

  @AfterEach
  public void afterTest() {
    dao = null;
  }

  @Test
  public void setKeyTest() throws NoSuchAlgorithmException, SQLException {
    try {
      CallContext.begin(properties.getProperty("datasource.jndiName"), KeyDaoIT.class.getCanonicalName());
      KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
      kpg.initialize(2048);
      KeyPair kp = kpg.generateKeyPair();
      Key pub = kp.getPublic();
      Key pvt = kp.getPrivate();
      KeyCreateDto keyCreateDto = new KeyCreateDto();
      Base64.Encoder encoder = Base64.getEncoder();
      keyCreateDto.setPublicKey(encoder.encodeToString(pub.getEncoded()));
      keyCreateDto.setPrivateKey(encoder.encodeToString(pvt.getEncoded()));
      String keyId = dao.setKeys(keyCreateDto, 1);
      KeyDto keyDto = dao.getKeys(keyId, 1);
      assertEquals(keyDto.getPublicKey(), keyCreateDto.getPublicKey());
      assertEquals(keyDto.getPrivateKey(), keyCreateDto.getPrivateKey());
    } finally {
      CallContext.rollback();
      CallContext.end();
    }
  }

}
