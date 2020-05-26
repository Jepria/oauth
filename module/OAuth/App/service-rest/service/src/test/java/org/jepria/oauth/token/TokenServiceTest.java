package org.jepria.oauth.token;

import org.jepria.oauth.key.KeyService;
import org.jepria.oauth.key.dto.KeyDto;
import org.jepria.oauth.session.SessionService;
import org.jepria.oauth.session.dto.SessionCreateDto;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDto;
import org.jepria.oauth.session.dto.SessionUpdateDto;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.oauth.token.dto.TokenInfoDto;
import org.jepria.oauth.sdk.ResponseType;
import org.jepria.server.data.OptionDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.net.URI;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {
  
  static TokenService tokenService;
  static SessionService sessionService;
  static KeyService keyService;
  
  @BeforeAll
  public static void init(@Mock SessionService sessionService,
                          @Mock KeyService keyService) throws NoSuchAlgorithmException {
    tokenService = new TokenServiceImpl(sessionService, keyService);
    TokenServiceTest.sessionService = sessionService;
    TokenServiceTest.keyService = keyService;
    //sessionService mocks
    when(sessionService.create(isA(SessionCreateDto.class), any())).thenReturn("1");
    when(sessionService.find(any(SessionSearchDto.class), any())).thenAnswer((Answer<List<SessionDto>>) invocationOnMock -> {
      SessionSearchDto template = invocationOnMock.getArgument(0);
      SessionDto sessionDto = new SessionDto();
      sessionDto.setSessionId(1);
      sessionDto.setAuthorizationCode(template.getAuthorizationCode());
      sessionDto.setRedirectUri(template.getRedirectUri());
      sessionDto.setDateIns(new Date((new Date().getTime() - 1000)));
      OptionDto<String> client = new OptionDto<>();
      client.setName(template.getClientId());
      client.setValue(template.getClientId());
      sessionDto.setClient(client);
      OptionDto<Integer> operator = new OptionDto<>();
      operator.setValue(1);
      operator.setName("testUser");
      sessionDto.setOperator(operator);
      sessionDto.setBlocked(false);
      sessionDto.setSessionTokenId(template.getSessionTokenId());
      sessionDto.setSessionTokenDateIns(new Date(new Date().getTime() - 10000));
      sessionDto.setSessionTokenDateFinish(new Date(new Date().getTime() + 100000));
      sessionDto.setAccessTokenId(template.getAccessTokenId());
      sessionDto.setAccessTokenDateIns(new Date(new Date().getTime() - 10000));
      sessionDto.setAccessTokenDateFinish(new Date(new Date().getTime() + 100000));
      sessionDto.setRefreshTokenId(template.getRefreshTokenId());
      sessionDto.setRefreshTokenDateIns(new Date(new Date().getTime() - 10000));
      sessionDto.setRefreshTokenDateFinish(new Date(new Date().getTime() + 100000));
      return Collections.singletonList(sessionDto);
    });
    doNothing().when(sessionService).deleteRecord(any(), any());
    doNothing().when(sessionService).update(any(), isA(SessionUpdateDto.class), any());
    //keyService mocks
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    KeyPair kp = kpg.generateKeyPair();
    Key pub = kp.getPublic();
    Key pvt = kp.getPrivate();
    KeyDto keyDto = new KeyDto();
    Base64.Encoder encoder = Base64.getEncoder();
    keyDto.setKeyId("1");
    keyDto.setActual(true);
    keyDto.setDateIns(new Date());
    keyDto.setPublicKey(encoder.encodeToString(pub.getEncoded()));
    keyDto.setPrivateKey(encoder.encodeToString(pvt.getEncoded()));
    when(keyService.getKeys(isNull(), any())).thenReturn(keyDto);
  }
  
  @Test
  public void authCodeTokenTest() {
    TokenDto tokenDto = tokenService.create("testClient", "authCode", "issuer", URI.create("http://testuri.com"));
    assertNotNull(tokenDto);
    verify(sessionService, atLeast(1)).find(any(), any());
    verify(sessionService, atLeast(1)).update(any(), any(), any());
    verify(keyService, atLeast(1)).getKeys(any(), any());
  }
  
  @Test
  public void implicitTokenTest() {
    TokenDto tokenDto = tokenService.create(ResponseType.TOKEN, "testClient", "authCode", "issuer", URI.create("http://testuri.com"));
    assertNotNull(tokenDto);
    verify(sessionService, atLeast(1)).find(any(), any());
    verify(sessionService, atLeast(1)).update(any(), any(), any());
    verify(keyService, atLeast(1)).getKeys(any(), any());
  }
  
  @Test
  public void passwordTokenTest() {
    TokenDto tokenDto = tokenService.create("testClient", "testUser", 1, "issuer");
    assertNotNull(tokenDto);
    verify(sessionService, atLeast(1)).find(any(), any());
    verify(sessionService, atLeast(1)).update(any(), any(), any());
    verify(keyService, atLeast(1)).getKeys(any(), any());
  }
  
  @Test
  public void clientCredentialsTokenTest() {
    TokenDto tokenDto = tokenService.create("testClient", 1, "issuer");
    assertNotNull(tokenDto);
    verify(sessionService, atLeast(1)).create(any(), any());
    verify(keyService, atLeast(1)).getKeys(any(), any());
  }
  
  @Test
  public void refreshTokenTest() {
    TokenDto tokenDto = tokenService.create("testClient", "testUser", 1, "issuer");
    TokenDto result = tokenService.create("testClient", tokenDto.getRefreshToken(), "issuer");
    assertNotNull(result);
    verify(sessionService, atLeast(1)).find(any(), any());
    verify(sessionService, atLeast(1)).create(any(), any());
    verify(keyService, atLeast(1)).getKeys(any(), any());
  }
  
  @Test
  public void deleteTokenTest() {
    TokenDto tokenDto = tokenService.create("testClient", "testUser", 1, "issuer");
    tokenService.delete("testClient", tokenDto.getAccessToken());
    verify(sessionService, atLeast(1)).deleteRecord(any(), any());
  }
  
  @Test
  public void getTokenInfoTest() {
    TokenDto tokenDto = tokenService.create("testClient", "testUser", 1, "issuer");
    TokenInfoDto tokenInfoDto = tokenService.getTokenInfo("issuer", tokenDto.getAccessToken());
    assertNotNull(tokenInfoDto);
    verify(sessionService, atLeast(1)).find(any(), any());
    verify(keyService, atLeast(1)).getKeys(any(), any());
  }
  
  
}