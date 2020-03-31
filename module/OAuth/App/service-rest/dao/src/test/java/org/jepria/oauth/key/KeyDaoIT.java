package org.jepria.oauth.key;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.jepria.oauth.DaoTestBase;
import org.jepria.oauth.dao.key.KeyDaoImpl;
import org.jepria.oauth.model.key.dao.KeyDao;
import org.jepria.oauth.model.key.dto.KeyCreateDto;
import org.jepria.oauth.model.key.dto.KeyDto;
import org.junit.jupiter.api.*;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Logger;

public class KeyDaoIT extends DaoTestBase {
  
  Logger logger = Logger.getLogger(KeyDaoIT.class.getCanonicalName());
  
  private KeyDao dao;
  
  @BeforeEach
  public void beforeTest(){
    dao = new KeyDaoImpl(properties.getProperty("datasource.jndiName"));
  }
  
  @AfterEach
  public void afterTest() {
    dao = null;
  }
  
  @Test
  public void setKeyTest() throws NoSuchAlgorithmException {
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
  }
  
}
