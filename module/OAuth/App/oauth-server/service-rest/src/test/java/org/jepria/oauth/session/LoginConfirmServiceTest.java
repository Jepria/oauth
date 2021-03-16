package org.jepria.oauth.session;

import org.jepria.oauth.authentication.dto.SessionTokenDto;
import org.jepria.oauth.key.KeyService;
import org.jepria.oauth.key.dto.KeyDto;
import org.jepria.oauth.main.Utils;
import org.jepria.oauth.session.dao.SessionDao;
import org.jepria.oauth.session.dto.SessionCreateDto;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDto;
import org.jepria.oauth.session.dto.SessionUpdateDto;
import org.jepria.server.data.OptionDto;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginConfirmServiceTest {
  
  static LoginConfirmService sessionService;
  static SessionDao dao;
  
  @BeforeAll
  public static void init(@Mock SessionDao dao,
                          @Mock KeyService keyService) throws NoSuchAlgorithmException {
    LoginConfirmServiceTest.dao = dao;
    sessionService = new LoginConfirmService(dao, keyService);
    //sessionService mocks
    when(dao.find(any(SessionSearchDto.class), any())).thenAnswer((Answer<List<SessionDto>>) invocationOnMock -> {
      SessionSearchDto template = invocationOnMock.getArgument(0);
      SessionDto sessionDto = new SessionDto();
      sessionDto.setSessionId("1");
      sessionDto.setAuthorizationCode("authCode");
      sessionDto.setRedirectUri("http://redirecturi");
      sessionDto.setDateIns(new Date((new Date().getTime() - 1000)));
      OptionDto<String> client = new OptionDto<>();
      client.setName("testClientName");
      client.setValue("testClient");
      sessionDto.setClient(client);
      if (sessionDto.getSessionId().equals(template.getSessionId())
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
    doNothing().when(dao).update(any(), isA(SessionUpdateDto.class), any());
    when(dao.create(isA(SessionCreateDto.class), any())).thenReturn(new Random().nextInt());
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
  public void authenticationTest() {
    SessionTokenDto sessionTokenDto = sessionService.confirm("1",
        "testUser",
        1,
        "http://redirecturi",
        "testClient",
        "testPassword",
        Utils.serverCredential,
        8);
    assertNotNull(sessionTokenDto);
    verify(dao, atLeast(1)).find(any(SessionSearchDto.class), any());
    verify(dao, times(1)).update(any(), any(SessionUpdateDto.class), any());
  }
}
