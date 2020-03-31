package org.jepria.oauth.client;

import org.jepria.oauth.DaoTestBase;
import org.jepria.oauth.dao.client.ClientDaoImpl;
import org.jepria.oauth.dao.client.ClientFieldNames;
import org.jepria.oauth.dao.clienturi.ClientUriDaoImpl;
import org.jepria.oauth.model.client.dao.ClientDao;
import org.jepria.oauth.model.client.dto.ClientCreateDto;
import org.jepria.oauth.model.client.dto.ClientDto;
import org.jepria.oauth.model.client.dto.ClientSearchDto;
import org.jepria.oauth.model.client.dto.ClientUpdateDto;
import org.jepria.oauth.model.clienturi.dto.ClientUriCreateDto;
import org.jepria.oauth.model.clienturi.dto.ClientUriDto;
import org.jepria.oauth.sdk.ApplicationType;
import org.jepria.server.data.Dao;
import org.junit.jupiter.api.*;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import static org.jepria.oauth.dao.clienturi.ClientUriFieldNames.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientControlIT extends DaoTestBase {
  
  Logger logger = Logger.getLogger(ClientControlIT.class.getCanonicalName());
  
  private ClientDao clientDao;
  private Dao clientUriDao;
  private static String clientId;
  private static Integer clientUriId;
  
  @BeforeEach
  public void beforeTest(){
    clientDao = new ClientDaoImpl(properties.getProperty("datasource.jndiName"));
    clientUriDao = new ClientUriDaoImpl(properties.getProperty("datasource.jndiName"));
  }
  
  @AfterEach
  public void afterTest() {
    clientDao = null;
    clientUriDao = null;
  }
  
  @Test
  @Order(1)
  public void createTest() throws NoSuchAlgorithmException {
    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    SecureRandom sr = SecureRandom.getInstanceStrong();
    byte[] clientIdBytes = new byte[16];
    sr.nextBytes(clientIdBytes);
    clientId = encoder.encodeToString(clientIdBytes);
    ClientCreateDto clientCreateDto = new ClientCreateDto();
    clientCreateDto.setClientId(clientId);
    byte[] clientSecretBytes = new byte[16];
    sr.nextBytes(clientSecretBytes);
    clientCreateDto.setClientSecret(encoder.encodeToString(clientSecretBytes));
    clientCreateDto.setClientName("created" + new Date().getTime());
    clientCreateDto.setClientNameEn("createdEn" + new Date().getTime());
    clientCreateDto.setApplicationType(ApplicationType.WEB);
    clientCreateDto.setGrantTypes(ApplicationType.getApplicationGrantTypes(ApplicationType.WEB));
    clientDao.create(clientCreateDto, 1);
    ClientDto clientDto = (ClientDto) clientDao.findByPrimaryKey(new HashMap<String, String>(){{
      put(ClientFieldNames.CLIENT_ID, clientId);
    }}, 1).get(0);
    assertEquals(clientId, clientDto.getClientId());
    assertEquals(clientCreateDto.getClientSecret(), clientDto.getClientSecret());
    assertEquals(clientCreateDto.getClientName(), clientDto.getClientName());
    assertEquals(clientCreateDto.getClientNameEn(), clientDto.getClientNameEn());
    assertEquals(clientCreateDto.getApplicationType(), clientDto.getApplicationType());
    assertTrue(clientCreateDto.getGrantTypes().size() == clientDto.getGrantTypes().size()
    && clientCreateDto.getGrantTypes().containsAll(clientDto.getGrantTypes())
    && clientDto.getGrantTypes().containsAll(clientCreateDto.getGrantTypes()));
  }
  
  @Test
  @Order(2)
  public void findByIdTest() {
    ClientDto clientDto = (ClientDto) clientDao.findByPrimaryKey(new HashMap<String, String>(){{
      put(ClientFieldNames.CLIENT_ID, clientId);
    }}, 1).get(0);
    assertNotNull(clientDto);
  }
  
  @Test
  @Order(3)
  public void updateTest() {
    ClientUpdateDto clientUpdateDto = new ClientUpdateDto();
    clientUpdateDto.setClientName("updated" + new Date().getTime());
    clientUpdateDto.setClientNameEn("updatedEn" + new Date().getTime());
    clientUpdateDto.setApplicationType(ApplicationType.NATIVE);
    clientUpdateDto.setGrantTypes(ApplicationType.getApplicationGrantTypes(ApplicationType.NATIVE));
    clientDao.update(new HashMap<String, String>(){{
      put(ClientFieldNames.CLIENT_ID, clientId);
    }}, clientUpdateDto, 1);
    ClientDto clientDto = (ClientDto) clientDao.findByPrimaryKey(new HashMap<String, String>(){{
      put(ClientFieldNames.CLIENT_ID, clientId);
    }}, 1).get(0);
    assertEquals(clientId, clientDto.getClientId());
    assertEquals(clientUpdateDto.getClientName(), clientDto.getClientName());
    assertEquals(clientUpdateDto.getClientNameEn(), clientDto.getClientNameEn());
    assertEquals(clientUpdateDto.getApplicationType(), clientDto.getApplicationType());
    assertTrue(clientUpdateDto.getGrantTypes().size() == clientDto.getGrantTypes().size()
        && clientUpdateDto.getGrantTypes().containsAll(clientDto.getGrantTypes())
        && clientDto.getGrantTypes().containsAll(clientUpdateDto.getGrantTypes()));
  }
  
  @Test
  @Order(4)
  public void searchTest() {
    ClientSearchDto clientSearchDto = new ClientSearchDto();
    clientSearchDto.setClientId(clientId);
    clientSearchDto.setClientName("updated");
    clientSearchDto.setClientNameEn("updatedEn");
    List<ClientDto> result = (List<ClientDto>) clientDao.find(clientSearchDto, 1);
    assertFalse(result.isEmpty());
    assertTrue(result.size() == 1);
  }
  
  @Test
  @Order(5)
  public void addClientUriTest() {
    ClientUriCreateDto clientUriCreateDto = new ClientUriCreateDto();
    clientUriCreateDto.setClientId(clientId);
    clientUriCreateDto.setClientUri("http://server/test");
    clientUriId = (Integer) clientUriDao.create(clientUriCreateDto, 1);
    ClientUriDto clientUriDto = (ClientUriDto) clientUriDao.findByPrimaryKey(new HashMap<String, Object>(){{
      put(ClientFieldNames.CLIENT_ID, clientId);
      put(CLIENT_URI_ID, clientUriId);
    }}, 1).get(0);
    assertEquals(clientUriDto.getClientId(), clientId);
    assertEquals(clientUriDto.getClientUri(), clientUriCreateDto.getClientUri());
  }
  
  @Test
  @Order(6)
  public void findClientUriTest() {
    ClientUriDto clientUriDto = (ClientUriDto) clientUriDao.findByPrimaryKey(new HashMap<String, Object>(){{
      put(ClientFieldNames.CLIENT_ID, clientId);
      put(CLIENT_URI_ID, clientUriId);
    }}, 1).get(0);
    assertNotNull(clientUriDto);
  }
  
  @Test
  @Order(7)
  public void deleteClientUriTest() {
    clientUriDao.delete(new HashMap<String, Integer>(){{
      put(CLIENT_URI_ID, clientUriId);
    }}, 1);
    List<ClientUriDto> result = (List<ClientUriDto>) clientUriDao.findByPrimaryKey(new HashMap<String, Integer>(){{
      put(CLIENT_URI_ID, clientUriId);
    }}, 1);
    assertTrue(result.isEmpty());
  }
  
  @Test
  @Order(8)
  public void deleteTest() {
    clientDao.delete(new HashMap<String, String>(){{
      put(ClientFieldNames.CLIENT_ID, clientId);
    }}, 1);
    List<ClientDto> result = (List<ClientDto>) clientDao.findByPrimaryKey(new HashMap<String, String>(){{
      put(ClientFieldNames.CLIENT_ID, clientId);
    }}, 1);
    assertTrue(result.isEmpty());
  }
  
}
