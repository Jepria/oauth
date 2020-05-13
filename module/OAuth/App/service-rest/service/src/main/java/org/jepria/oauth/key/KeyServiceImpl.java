package org.jepria.oauth.key;

import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.key.dao.KeyDao;
import org.jepria.oauth.key.dto.KeyCreateDto;
import org.jepria.oauth.key.dto.KeyDto;
import org.jepria.server.data.RuntimeSQLException;
import org.jepria.server.service.security.Credential;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

import static org.jepria.oauth.sdk.OAuthConstants.SERVER_ERROR;

public class KeyServiceImpl implements KeyService {
  
  private KeyDao dao;
  
  public KeyServiceImpl(KeyDao dao) {
    this.dao = dao;
  }
  
  @Override
  public String setKeys(Credential credential) {
    try {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
      kpg.initialize(2048);
      KeyPair kp = kpg.generateKeyPair();
      Key pub = kp.getPublic();
      Key pvt = kp.getPrivate();
      KeyCreateDto keyCreateDto = new KeyCreateDto();
      Base64.Encoder encoder = Base64.getEncoder();
      keyCreateDto.setPublicKey(encoder.encodeToString(pub.getEncoded()));
      keyCreateDto.setPrivateKey(encoder.encodeToString(pvt.getEncoded()));
      String keyId = dao.setKeys(keyCreateDto, credential.getOperatorId());
      return keyId;
    } catch (NoSuchAlgorithmException e) {
      throw new OAuthRuntimeException(SERVER_ERROR, e);
    } catch (RuntimeSQLException ex) {
      SQLException sqlException = ex.getSQLException();
      throw new OAuthRuntimeException(SERVER_ERROR, sqlException);
    }
  }
  
  @Override
  public KeyDto getKeys(String keyId, Credential credential) {
    try {
      KeyDto keyDto = dao.getKeys(keyId, credential.getOperatorId());
      if (keyDto == null || !keyDto.getActual() || new Date().after(addMonth(keyDto.getDateIns(), 3))) {
        //Срок действия запрошенного ключа истек
        if (keyId == null) {
          //если был запрошен последний актуальный ключ, то обновить его и получить новую пару.
          keyDto = dao.getKeys(setKeys(credential), credential.getOperatorId());
        } else {
          //иначе запросить последний актуальный ключ
          keyDto = dao.getKeys(null, credential.getOperatorId());
        }
      }
      return keyDto;
    } catch (RuntimeSQLException ex) {
      SQLException sqlException = ex.getSQLException();
      throw new OAuthRuntimeException(SERVER_ERROR, sqlException);
    }
    
  }
  
  private Date addMonth(Date date, int month) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.MONTH, month);
    return calendar.getTime();
  }
}
