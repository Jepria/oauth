package org.jepria.oauth.key;

import org.jepria.oauth.model.key.KeyService;
import org.jepria.oauth.model.key.dao.KeyDao;
import org.jepria.oauth.model.key.dto.KeyCreateDto;
import org.jepria.oauth.model.key.dto.KeyDto;
import org.jepria.oauth.service.key.KeyServiceImpl;
import org.jepria.server.service.security.Credential;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KeyServiceTest {
  
  static KeyService keyService;
  
  @BeforeAll
  public static void init(@Mock KeyDao keyDao) {
    keyService = new KeyServiceImpl(keyDao);
    
    KeyDto keyDto = new KeyDto();
    
    when(keyDao.getKeys(anyString(), anyInt())).thenAnswer((Answer<KeyDto>) invocationOnMock -> {
      KeyDto result = new KeyDto();
      String keyId = invocationOnMock.getArgument(0, String.class);
      if (keyId != null && keyId == keyDto.getKeyId()) {
        return keyDto;
      } else {
        SecureRandom sr = SecureRandom.getInstanceStrong();
        keyId = String.valueOf(sr.nextInt());
        result.setKeyId(keyId);
        result.setPublicKey("publicKey");
        result.setPublicKey("privateKey");
        result.setDateIns(new Date(new Date().getTime() + 100000));
        result.setActual(true);
        return result;
      }
    });
  
  
    when(keyDao.getKeys(isNull(), anyInt())).thenAnswer((Answer<KeyDto>) invocationOnMock -> {
      KeyDto result = new KeyDto();
      SecureRandom sr = SecureRandom.getInstanceStrong();
      String keyId = String.valueOf(sr.nextInt());
      result.setKeyId(keyId);
      result.setPublicKey("publicKey");
      result.setPublicKey("privateKey");
      result.setDateIns(new Date(new Date().getTime() - 100000));
      result.setActual(false);
      return result;
    });
    
    when(keyDao.setKeys(any(KeyCreateDto.class), anyInt())).thenAnswer((Answer<String>) invocationOnMock -> {
      KeyCreateDto keyCreateDto = invocationOnMock.getArgument(0, KeyCreateDto.class);
      String keyId;
      SecureRandom sr = SecureRandom.getInstanceStrong();
      keyId = String.valueOf(sr.nextInt());
      keyDto.setKeyId(keyId);
      keyDto.setPublicKey(keyCreateDto.getPublicKey());
      keyDto.setPrivateKey(keyCreateDto.getPrivateKey());
      keyDto.setDateIns(new Date(new Date().getTime() + 100000));
      keyDto.setActual(true);
      return keyId;
    });
  }
  
  @Test
  public void getKeysTest() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
    KeyDto result = keyService.getKeys(null, new Credential() {
      @Override
      public int getOperatorId() {
        return 0;
      }
      
      @Override
      public String getUsername() {
        return null;
      }
      
      @Override
      public boolean isUserInRole(String s) {
        return false;
      }
    });
    assertNotNull(result);
    assertTrue(new Date().before(result.getDateIns()));
    assertTrue(result.getActual());
    
    byte[] bytePublicKey = Base64.getDecoder().decode(result.getPublicKey());
    X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(bytePublicKey);
    KeyFactory publicKf = KeyFactory.getInstance("RSA");
    PublicKey publicKey = publicKf.generatePublic(publicSpec);
    
    byte[] bytePrivateKey = Base64.getDecoder().decode(result.getPrivateKey());
    PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(bytePrivateKey);
    KeyFactory privateKf = KeyFactory.getInstance("RSA");
    PrivateKey privateKey = privateKf.generatePrivate(privateSpec);

// create a challenge
    byte[] challenge = new byte[10000];
    ThreadLocalRandom.current().nextBytes(challenge);

// sign using the private key
    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initSign(privateKey);
    sig.update(challenge);
    byte[] signature = sig.sign();

// verify signature using the public key
    sig.initVerify(publicKey);
    sig.update(challenge);
    
    assertTrue(sig.verify(signature));
  }
  
  @Test
  public void setKeysTest() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
    String keyId = keyService.setKeys(new Credential() {
      @Override
      public int getOperatorId() {
        return 0;
      }
  
      @Override
      public String getUsername() {
        return null;
      }
  
      @Override
      public boolean isUserInRole(String s) {
        return false;
      }
    });
    KeyDto result = keyService.getKeys(keyId, new Credential() {
      @Override
      public int getOperatorId() {
        return 0;
      }
  
      @Override
      public String getUsername() {
        return null;
      }
  
      @Override
      public boolean isUserInRole(String s) {
        return false;
      }
    });
    byte[] bytePublicKey = Base64.getDecoder().decode(result.getPublicKey());
    X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(bytePublicKey);
    KeyFactory publicKf = KeyFactory.getInstance("RSA");
    PublicKey publicKey = publicKf.generatePublic(publicSpec);
  
    byte[] bytePrivateKey = Base64.getDecoder().decode(result.getPrivateKey());
    PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(bytePrivateKey);
    KeyFactory privateKf = KeyFactory.getInstance("RSA");
    PrivateKey privateKey = privateKf.generatePrivate(privateSpec);

// create a challenge
    byte[] challenge = new byte[10000];
    ThreadLocalRandom.current().nextBytes(challenge);

// sign using the private key
    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initSign(privateKey);
    sig.update(challenge);
    byte[] signature = sig.sign();

// verify signature using the public key
    sig.initVerify(publicKey);
    sig.update(challenge);
  
    assertTrue(sig.verify(signature));
  }
  
  @AfterAll
  public static void destroy() {
    keyService = null;
  }
  
}
