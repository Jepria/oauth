package org.jepria.oauth.authentication;

import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.model.authentication.AuthenticationService;
import org.jepria.oauth.model.authentication.dao.AuthenticationDao;
import org.jepria.oauth.model.clienturi.ClientUriService;
import org.jepria.oauth.model.clienturi.dto.ClientUriDto;
import org.jepria.oauth.model.clienturi.dto.ClientUriSearchDto;
import org.jepria.oauth.model.key.KeyService;
import org.jepria.oauth.model.key.dto.KeyDto;
import org.jepria.oauth.model.session.SessionService;
import org.jepria.oauth.model.session.dto.SessionDto;
import org.jepria.oauth.model.session.dto.SessionSearchDto;
import org.jepria.oauth.model.session.dto.SessionUpdateDto;
import org.jepria.oauth.sdk.token.Encryptor;
import org.jepria.oauth.sdk.token.Signer;
import org.jepria.oauth.sdk.token.Token;
import org.jepria.oauth.sdk.token.TokenImpl;
import org.jepria.oauth.sdk.token.rsa.EncryptorRSA;
import org.jepria.oauth.sdk.token.rsa.SignerRSA;
import org.jepria.oauth.service.authentication.AuthenticationServiceImpl;
import org.jepria.server.data.OptionDto;
import org.jepria.server.data.RuntimeSQLException;
import org.jepria.server.service.security.Credential;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.jepria.oauth.sdk.OAuthConstants.SERVER_ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
  
  static AuthenticationService authenticationService;
  static AuthenticationDao dao;
  static SessionService sessionService;
  static ClientUriService clientUriService;
  static KeyService keyService;
  
  @BeforeAll
  public static void init(@Mock AuthenticationDao dao,
                          @Mock SessionService sessionService,
                          @Mock ClientUriService clientUriService,
                          @Mock KeyService keyService) throws NoSuchAlgorithmException {
    AuthenticationServiceTest.dao = dao;
    AuthenticationServiceTest.sessionService = sessionService;
    AuthenticationServiceTest.clientUriService = clientUriService;
    AuthenticationServiceTest.keyService = keyService;
    
    authenticationService = new AuthenticationServiceImpl(dao, sessionService, clientUriService, keyService);
    //dao mocks
    when(dao.loginByClientSecret(eq("test"), eq("testSecret"))).thenReturn(1);
    when(dao.loginByClientSecret(eq("test"), isNull())).thenReturn(null);
    when(dao.loginByClientSecret(not(eq("test")), not(eq("testSecret")))).then((Answer<Void>) invocationOnMock -> {throw new RuntimeSQLException(new SQLException("", "", 20003));});
    when(dao.loginByPassword(eq("testUser"), eq("testPassword"))).thenReturn(1);
    when(dao.loginByPassword(not(eq("testUser")), not(eq("testPassword")))).thenReturn(null);
    when(dao.verifyPKCE(anyString(), eq("testCodeVerifier"))).thenReturn(true);
    when(dao.verifyPKCE(anyString(), not(eq("testCodeVerifier")))).thenReturn(false);
    //sessionService mocks
    when(sessionService.find(any(SessionSearchDto.class), any())).thenAnswer((Answer<List<SessionDto>>) invocationOnMock -> {
      SessionSearchDto template = invocationOnMock.getArgument(0);
      SessionDto sessionDto = new SessionDto();
      sessionDto.setSessionId(1);
      sessionDto.setAuthorizationCode("authCode");
      sessionDto.setRedirectUri("http://redirecturi");
      sessionDto.setDateIns(new Date((new Date().getTime() - 1000)));
      OptionDto<String> client = new OptionDto<>();
      client.setName("testClientName");
      client.setValue("testClient");
      sessionDto.setClient(client);
      if (sessionDto.getAuthorizationCode().equals(template.getAuthorizationCode())
          && sessionDto.getRedirectUri().equals(template.getRedirectUri())
          && sessionDto.getClient().getValue().equals(template.getClientId())) {
        return Collections.singletonList(sessionDto);
      } else if ("sessionToken".equals(template.getSessionTokenId())) {
        sessionDto.setSessionTokenId("sessionToken");
        return Collections.singletonList(sessionDto);
      } else {
        return Collections.emptyList();
      }
    });
    doNothing().when(sessionService).deleteRecord(any(), any());
    doNothing().when(sessionService).update(any(), isA(SessionUpdateDto.class), any());
    //clientUriService mocks
    when(clientUriService.findClientUri(isA(ClientUriSearchDto.class), any())).thenAnswer((Answer<List<ClientUriDto>>) invocationOnMock -> {
      ClientUriSearchDto template = invocationOnMock.getArgument(0);
      if ("testClient".equals(template.getClientId())) {
        ClientUriDto clientUriDto = new ClientUriDto();
        clientUriDto.setClientId(template.getClientId());
        clientUriDto.setClientUriId(1);
        clientUriDto.setClientUri("http://redirecturi");
        return Collections.singletonList(clientUriDto);
      } else {
        return Collections.emptyList();
      }
    });
    //keyServiceMocks
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
  public void loginByPasswordTest() {
    assertEquals(authenticationService.loginByPassword("testUser", "testPassword"), 1);
    assertThrows(OAuthRuntimeException.class, () -> {
      authenticationService.loginByPassword("123", "123");
    });
  }
  
  @Test
  public void loginByCredentials() {
    assertEquals(authenticationService.loginByClientSecret("test", "testSecret"), 1);
    assertThrows(OAuthRuntimeException.class, () -> {
      authenticationService.loginByClientSecret("test", null);
    });
    assertThrows(OAuthRuntimeException.class, () -> {
      authenticationService.loginByClientSecret("123", "123");
    });
  }
  
  @Test
  public void loginByPkce() {
    assertThrows(OAuthRuntimeException.class, () -> {
      authenticationService.loginByAuthorizationCode("test", "test", "wrongCodeVerifier");
    });
    assertDoesNotThrow(() -> authenticationService.loginByAuthorizationCode("test", "test", "testCodeVerifier"));
  }
  
  @Test
  public void authenticationTest() {
    String sessionToken = authenticationService.authenticate("authCode",
        "http://redirecturi",
        "testClient",
        "testUser",
        "testPassword",
        "issuer");
    assertNotNull(sessionToken);
    verify(sessionService, atLeast(1)).find(any(SessionSearchDto.class), any());
    verify(sessionService, times(1)).update(isA(String.class), any(SessionUpdateDto.class), any());
  }
  
  @Test
  public void logoutTest() throws ParseException, InvalidKeySpecException, NoSuchAlgorithmException {
    KeyDto keyDto = keyService.getKeys(null, new Credential() {
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
    /**
     * Create token with JWT lib
     */
    Token token = new TokenImpl("sessionToken", Collections.EMPTY_LIST, "testUser" + ":" + 1,
        "issuer", new Date(new Date().getTime() + 10000), new Date());
    /**
     * Sign token with private key
     */
    Signer signer = new SignerRSA(keyDto.getPrivateKey());
    token = signer.sign(token);
    Encryptor encryptor = new EncryptorRSA(keyDto.getPublicKey());
    try {
      token = encryptor.encrypt(token);
    } catch (ParseException e) {
      throw new OAuthRuntimeException(SERVER_ERROR, e);
    }
    authenticationService.logout("testClient", "http://redirecturi", token.asString(), "issuer");
    verify(sessionService, atLeast(1)).find(isA(SessionSearchDto.class), any());
    verify(clientUriService, times(1)).findClientUri(isA(ClientUriSearchDto.class), any());
    verify(sessionService, times(1)).deleteRecord(isA(String.class), any());
  }
}
