package org.jepria.oauth.session;

import org.jepria.oauth.authentication.dto.SessionTokenDto;
import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.key.KeyService;
import org.jepria.oauth.key.dto.KeyDto;
import org.jepria.oauth.main.Utils;
import org.jepria.oauth.sdk.token.Encryptor;
import org.jepria.oauth.sdk.token.Token;
import org.jepria.oauth.sdk.token.rsa.EncryptorRSA;
import org.jepria.oauth.session.dao.SessionDao;
import org.jepria.oauth.session.dto.*;
import org.jepria.server.service.security.Credential;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.jepria.oauth.sdk.OAuthConstants.ACCESS_DENIED;
import static org.jepria.oauth.sdk.OAuthConstants.SERVER_ERROR;

public class LoginConfirmService extends SessionServiceImpl {
  protected final KeyService keyService;
  
  public LoginConfirmService(SessionDao dao, KeyService keyService) {
    super(dao, new SessionRecordDefinition());
    this.keyService = keyService;
  }
  
  public SessionTokenDto confirm(String sessionId,
                                 String username,
                                 Integer operatorId,
                                 String redirectUri,
                                 String clientId,
                                 String host,
                                 Credential credential,
                                 Integer sessionTokenLifeTime) {
    SessionSearchDto sessionSearchDto = new SessionSearchDto();
    sessionSearchDto.setSessionId(sessionId);
    sessionSearchDto.setRedirectUri(redirectUri);
    sessionSearchDto.setClientId(clientId);
    sessionSearchDto.setMaxRowCount(1);
    List<SessionDto> sessionDtoList = find(sessionSearchDto, credential);
  
    if (sessionDtoList.size() == 1) {
      SessionDto sessionDto = sessionDtoList.get(0);
      if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - sessionDto.getDateIns().getTime()) > 10) {
        throw new OAuthRuntimeException(ACCESS_DENIED, "Authorization code not found or has expired");
      }
      if (sessionDto.getOperator() != null) {
        throw new OAuthRuntimeException(ACCESS_DENIED, "Request has already passed authentication");
      }
      if (sessionDto.getAccessTokenId() != null) {
        throw new OAuthRuntimeException(ACCESS_DENIED, "Request is finished");
      }
    
      KeyDto keyDto = keyService.getKeys(null, Utils.serverCredential);
      Token sessionToken = Utils.generateToken(username, null, operatorId, host, keyDto.getPrivateKey(), sessionTokenLifeTime);
    
      SessionUpdateDto sessionUpdateDto = new SessionUpdateDto();
      sessionUpdateDto.setAuthorizationCode(sessionDto.getAuthorizationCode());
      sessionUpdateDto.setClientId(sessionDto.getClient().getValue());
      sessionUpdateDto.setRedirectUri(sessionDto.getRedirectUri());
      sessionUpdateDto.setOperatorId(operatorId);
      sessionUpdateDto.setCodeChallenge(sessionDto.getCodeChallenge());
      sessionUpdateDto.setSessionTokenId(sessionToken.getJti());
      sessionUpdateDto.setSessionTokenDateIns(sessionToken.getIssueTime());
      sessionUpdateDto.setSessionTokenDateFinish(sessionToken.getExpirationTime());
      update(sessionId, sessionUpdateDto, credential);
      
      SessionCreateDto sessionCreateDto = new SessionCreateDto();
      sessionCreateDto.setAuthorizationCode(sessionToken.getJti());
      sessionCreateDto.setClientId(sessionDto.getClient().getValue());
      sessionCreateDto.setOperatorId(operatorId);
      sessionCreateDto.setSessionTokenId(sessionToken.getJti());
      sessionCreateDto.setSessionTokenDateIns(sessionToken.getIssueTime());
      sessionCreateDto.setSessionTokenDateFinish(sessionToken.getExpirationTime());
      create(sessionCreateDto, Utils.serverCredential);
    
      SessionTokenDto sessionTokenDto = new SessionTokenDto();
      sessionTokenDto.setExpirationDate(sessionToken.getExpirationTime());
    
      // Encrypt token with public key
      Encryptor encryptor = new EncryptorRSA(keyDto.getPublicKey());
      try {
        sessionToken = encryptor.encrypt(sessionToken);
      } catch (ParseException e) {
        throw new OAuthRuntimeException(SERVER_ERROR, e);
      }
      sessionTokenDto.setToken(sessionToken.asString());
      sessionTokenDto.setAuthorizationCode(sessionDto.getAuthorizationCode());
      return sessionTokenDto;
    }
    return null;
  }
  
  public SessionTokenDto confirm(String sessionId,
                                 LoginConfirmDto dto,
                                 String host,
                                 Credential credential,
                                 Integer sessionTokenLifeTime) {
    return confirm(sessionId, dto.getUsername(), dto.getOperatorId(), dto.getRedirectUri(), dto.getClientId(), host, credential, sessionTokenLifeTime);
  }
  
}
