package org.jepria.oauth.authorization;

import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.client.ClientService;
import org.jepria.oauth.key.KeyService;
import org.jepria.oauth.key.dto.KeyDto;
import org.jepria.oauth.session.SessionService;
import org.jepria.oauth.session.dto.SessionCreateDto;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.sdk.ResponseType;
import org.jepria.oauth.sdk.token.Encryptor;
import org.jepria.oauth.sdk.token.Signer;
import org.jepria.oauth.sdk.token.Token;
import org.jepria.oauth.sdk.token.TokenImpl;
import org.jepria.oauth.sdk.token.rsa.EncryptorRSA;
import org.jepria.oauth.sdk.token.rsa.SignerRSA;
import org.jepria.server.data.OptionDto;
import org.jepria.server.data.RuntimeSQLException;
import org.jepria.server.service.security.Credential;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

import static org.jepria.oauth.sdk.OAuthConstants.SERVER_ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthorizationServiceTest {
  
  static AuthorizationService authorizationService;
  static SessionService sessionService;
  static ClientService clientService;
  static KeyService keyService;
  
  @BeforeAll
  public static void init(@Mock SessionService sessionService,
                          @Mock ClientService clientService,
                          @Mock KeyService keyService) throws NoSuchAlgorithmException {
    AuthorizationServiceTest.sessionService = sessionService;
    AuthorizationServiceTest.clientService = clientService;
    AuthorizationServiceTest.keyService = keyService;
    authorizationService = new AuthorizationServiceImpl(sessionService, clientService, keyService);
    //sessionService mocks
    Map<String, SessionDto> sessions = new HashMap<>();
    when(sessionService.getRecordById(anyString(), any())).thenAnswer((Answer<SessionDto>) invocationOnMock -> sessions.get(invocationOnMock.getArgument(0)));
    when(sessionService.create(isA(SessionCreateDto.class), any())).thenAnswer((Answer<String>) invocationOnMock -> {
      SessionCreateDto sessionCreateDto = invocationOnMock.getArgument(0);
      
      if (!sessionCreateDto.getClientId().equals("testClient")) {
        throw new RuntimeSQLException(new SQLException("", "", 20001));
      }
  
      if (sessionCreateDto.getClientId().equals("testClient") && !sessionCreateDto.getRedirectUri().equals("http://testuri")) {
        throw new RuntimeSQLException(new SQLException("", "", 20002));
      }
      
      SessionDto sessionDto = new SessionDto();
      SecureRandom sr = SecureRandom.getInstanceStrong();
      sessionDto.setSessionId(String.valueOf(sr.nextInt()));
      sessionDto.setDateIns((new Date()));
      sessionDto.setCodeChallenge(sessionCreateDto.getCodeChallenge());
      sessionDto.setAuthorizationCode(sessionCreateDto.getAuthorizationCode());
      OptionDto<String> client = new OptionDto<>();
      client.setValue(sessionCreateDto.getClientId());
      client.setName(sessionCreateDto.getClientId());
      sessionDto.setClient(client);
      OptionDto<Integer> operator = new OptionDto<>();
      operator.setValue(sessionCreateDto.getOperatorId());
      operator.setName("testUser");
      sessionDto.setOperator(operator);
      sessionDto.setRedirectUri(sessionCreateDto.getRedirectUri());
      sessions.put(sessionDto.getSessionId().toString(), sessionDto);
      return sessionDto.getSessionId().toString();
    });
    
    //clientService mocks
    when(clientService.getClientResponseTypes(eq("testClient"))).thenReturn(ResponseType.getResponseTypes());
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
  public void codeAuthorizeTest() {
    SessionDto result = authorizationService.authorize(ResponseType.CODE, "testClient", "http://testuri", "codeChallenge");
    assertNotNull(result);
    assertEquals(result.getClient().getValue(), "testClient");
    assertEquals(result.getRedirectUri(), "http://testuri");
    assertEquals(result.getCodeChallenge(), "codeChallenge");
    verify(sessionService, atLeast(1)).create(isA(SessionCreateDto.class), any());
    verify(sessionService, atLeast(1)).getRecordById(isA(String.class), any());
    verify(clientService, atLeast(1)).getClientResponseTypes(isA(String.class));
  }
  
  @Test
  public void implicitAuthorizeTest() {
    SessionDto result = authorizationService.authorize(ResponseType.CODE, "testClient", "http://testuri", "codeChallenge");
    assertNotNull(result);
    assertEquals(result.getClient().getValue(), "testClient");
    assertEquals(result.getRedirectUri(), "http://testuri");
    assertEquals(result.getCodeChallenge(), "codeChallenge");
    verify(sessionService, atLeast(1)).create(isA(SessionCreateDto.class), any());
    verify(sessionService, atLeast(1)).getRecordById(isA(String.class), any());
    verify(clientService, atLeast(1)).getClientResponseTypes(isA(String.class));
  }
  
  @Test
  public void sessionTokenAuthorizeTest() throws InvalidKeySpecException, NoSuchAlgorithmException, ParseException {
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
    SessionDto result = authorizationService.authorize(ResponseType.CODE, "testClient", "http://testuri", "codeChallenge", token.asString(), "issuer");
    assertNotNull(result);
    assertEquals(result.getClient().getValue(), "testClient");
    assertEquals(result.getRedirectUri(), "http://testuri");
    assertEquals(result.getCodeChallenge(), "codeChallenge");
    verify(sessionService, atLeast(1)).create(isA(SessionCreateDto.class), any());
    verify(sessionService, atLeast(1)).getRecordById(isA(String.class), any());
    verify(clientService, atLeast(1)).getClientResponseTypes(isA(String.class));
    verify(keyService, atLeast(1)).getKeys(isNull(), any());
  }
  
  @Test
  public void incorrectRequestTest() throws InvalidKeySpecException, NoSuchAlgorithmException, ParseException {
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
    Token finalToken = token;
    assertThrows(OAuthRuntimeException.class, () ->  authorizationService.authorize(ResponseType.CODE, "incorrectClient", "http://testuri", "codeChallenge"));
    assertThrows(OAuthRuntimeException.class, () ->  authorizationService.authorize(ResponseType.CODE, "incorrectClient", "http://testuri", "codeChallenge", finalToken.asString(), "issuer"));
    assertThrows(OAuthRuntimeException.class, () ->  authorizationService.authorize(ResponseType.CODE, "testClient", "http://wronguri", "codeChallenge"));
    assertThrows(OAuthRuntimeException.class, () ->  authorizationService.authorize(ResponseType.CODE, "testClient", "http://wronguri", "codeChallenge", finalToken.asString(), "issuer"));
  }
  
  @Test
  public void unsupportedResponseTypeTest() {
    assertThrows(OAuthRuntimeException.class, () ->  authorizationService.authorize("unsupportedResponseType", "testClient", "http://testuri", "codeChallenge"));
  }
}
