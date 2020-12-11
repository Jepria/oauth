package org.jepria.oauth.client;

import org.jepria.compat.server.dao.CallContext;
import org.jepria.oauth.DaoTestBase;
import org.jepria.oauth.client.dao.ClientDao;
import org.jepria.oauth.client.dao.ClientDaoImpl;
import org.jepria.oauth.client.dto.ClientCreateDto;
import org.jepria.oauth.client.dto.ClientDto;
import org.jepria.oauth.client.dto.ClientSearchDto;
import org.jepria.oauth.client.dto.ClientUpdateDto;
import org.jepria.oauth.clienturi.dao.ClientUriDaoImpl;
import org.jepria.oauth.clienturi.dto.ClientUriCreateDto;
import org.jepria.oauth.clienturi.dto.ClientUriDto;
import org.jepria.oauth.sdk.ApplicationType;
import org.jepria.server.data.Dao;
import org.junit.jupiter.api.*;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import static org.jepria.oauth.clienturi.ClientUriFieldNames.CLIENT_URI_ID;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientDaoIT extends DaoTestBase {

  Logger logger = Logger.getLogger(ClientDaoIT.class.getCanonicalName());

  private ClientDao clientDao;
  private Dao clientUriDao;

  @BeforeEach
  public void beforeTest() {
    clientDao = new ClientDaoImpl();
    clientUriDao = new ClientUriDaoImpl();
  }

  @AfterEach
  public void afterTest() {
    clientDao = null;
    clientUriDao = null;
  }

  @Test
  @Order(1)
  public void createTest() throws NoSuchAlgorithmException, SQLException {
    try {
      CallContext.begin(properties.getProperty("datasource.jndiName"), ClientDaoIT.class.getCanonicalName());
      Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
      SecureRandom sr = SecureRandom.getInstanceStrong();
      byte[] clientIdBytes = new byte[16];
      sr.nextBytes(clientIdBytes);
      String clientId = encoder.encodeToString(clientIdBytes);
      ClientCreateDto clientCreateDto = new ClientCreateDto();
      clientCreateDto.setClientId(clientId);
      byte[] clientSecretBytes = new byte[16];
      sr.nextBytes(clientSecretBytes);
      clientCreateDto.setClientName("created" + new Date().getTime());
      clientCreateDto.setClientNameEn("createdEn" + new Date().getTime());
      clientCreateDto.setApplicationType(ApplicationType.WEB);
      clientCreateDto.setGrantTypes(ApplicationType.getApplicationGrantTypes(ApplicationType.WEB));
      clientDao.create(clientCreateDto, 1);
      ClientDto clientDto = (ClientDto) clientDao.findByPrimaryKey(new HashMap<String, String>() {{
        put(ClientFieldNames.CLIENT_ID, clientId);
      }}, 1).get(0);
      assertEquals(clientId, clientDto.getClientId());
      assertNotNull(clientDto.getClientSecret());
      assertEquals(clientCreateDto.getClientName(), clientDto.getClientName());
      assertEquals(clientCreateDto.getClientNameEn(), clientDto.getClientNameEn());
      assertEquals(clientCreateDto.getApplicationType(), clientDto.getApplicationType());
      assertTrue(clientCreateDto.getGrantTypes().size() == clientDto.getGrantTypes().size()
        && clientCreateDto.getGrantTypes().containsAll(clientDto.getGrantTypes())
        && clientDto.getGrantTypes().containsAll(clientCreateDto.getGrantTypes()));
    } finally {
      CallContext.rollback();
      CallContext.end();
    }
  }

  @Test
  @Order(2)
  public void findByIdTest() throws SQLException {
    try {
      CallContext.begin(properties.getProperty("datasource.jndiName"), ClientDaoIT.class.getCanonicalName());
      ClientDto clientDto = (ClientDto) clientDao.findByPrimaryKey(new HashMap<String, String>() {{
        put(ClientFieldNames.CLIENT_ID, properties.getProperty("client.id"));
      }}, 1).get(0);
      assertNotNull(clientDto);
    } finally {
      CallContext.rollback();
      CallContext.end();
    }
  }

  @Test
  @Order(3)
  public void updateTest() throws SQLException {
    try {
      CallContext.begin(properties.getProperty("datasource.jndiName"), ClientDaoIT.class.getCanonicalName());
      ClientUpdateDto clientUpdateDto = new ClientUpdateDto();
      clientUpdateDto.setClientName("updated" + new Date().getTime());
      clientUpdateDto.setClientNameEn("updatedEn" + new Date().getTime());
      clientUpdateDto.setApplicationType(ApplicationType.NATIVE);
      clientUpdateDto.setGrantTypes(ApplicationType.getApplicationGrantTypes(ApplicationType.NATIVE));
      clientDao.update(new HashMap<String, String>() {{
        put(ClientFieldNames.CLIENT_ID, properties.getProperty("client.id"));
      }}, clientUpdateDto, 1);
      ClientDto clientDto = (ClientDto) clientDao.findByPrimaryKey(new HashMap<String, String>() {{
        put(ClientFieldNames.CLIENT_ID, properties.getProperty("client.id"));
      }}, 1).get(0);
      assertEquals(properties.getProperty("client.id"), clientDto.getClientId());
      assertEquals(clientUpdateDto.getClientName(), clientDto.getClientName());
      assertEquals(clientUpdateDto.getClientNameEn(), clientDto.getClientNameEn());
      assertEquals(clientUpdateDto.getApplicationType(), clientDto.getApplicationType());
      assertTrue(clientUpdateDto.getGrantTypes().size() == clientDto.getGrantTypes().size()
        && clientUpdateDto.getGrantTypes().containsAll(clientDto.getGrantTypes())
        && clientDto.getGrantTypes().containsAll(clientUpdateDto.getGrantTypes()));
    } finally {
      CallContext.rollback();
      CallContext.end();
    }
  }

  @Test
  @Order(4)
  public void searchTest() throws SQLException {
    try {
      CallContext.begin(properties.getProperty("datasource.jndiName"), ClientDaoIT.class.getCanonicalName());
      ClientSearchDto clientSearchDto = new ClientSearchDto();
      clientSearchDto.setClientId(properties.getProperty("client.id"));
      List<ClientDto> result = (List<ClientDto>) clientDao.find(clientSearchDto, 1);
      assertFalse(result.isEmpty());
      assertTrue(result.size() == 1);
    } finally {
      CallContext.rollback();
      CallContext.end();
    }
  }

  @Test
  @Order(5)
  public void addClientUriTest() throws SQLException {
    try {
      CallContext.begin(properties.getProperty("datasource.jndiName"), ClientDaoIT.class.getCanonicalName());
      ClientUriCreateDto clientUriCreateDto = new ClientUriCreateDto();
      clientUriCreateDto.setClientId(properties.getProperty("client.id"));
      clientUriCreateDto.setClientUri("http://server/test");
      Integer clientUriId = (Integer) clientUriDao.create(clientUriCreateDto, 1);
      ClientUriDto clientUriDto = (ClientUriDto) clientUriDao.findByPrimaryKey(new HashMap<String, Object>() {{
        put(ClientFieldNames.CLIENT_ID, properties.getProperty("client.id"));
        put(CLIENT_URI_ID, clientUriId);
      }}, 1).get(0);
      assertEquals(clientUriDto.getClientId(), properties.getProperty("client.id"));
      assertEquals(clientUriDto.getClientUri(), clientUriCreateDto.getClientUri());
    } finally {
      CallContext.rollback();
      CallContext.end();
    }
  }

  @Test
  @Order(6)
  public void deleteClientUriTest() throws SQLException {
    try {
      CallContext.begin(properties.getProperty("datasource.jndiName"), ClientDaoIT.class.getCanonicalName());
      ClientUriCreateDto clientUriCreateDto = new ClientUriCreateDto();
      clientUriCreateDto.setClientId(properties.getProperty("client.id"));
      clientUriCreateDto.setClientUri("http://server/test");
      Integer clientUriId = (Integer) clientUriDao.create(clientUriCreateDto, 1);
      clientUriDao.delete(new HashMap<String, Integer>() {{
        put(CLIENT_URI_ID, clientUriId);
      }}, 1);
      List<ClientUriDto> result = (List<ClientUriDto>) clientUriDao.findByPrimaryKey(new HashMap<String, Integer>() {{
        put(CLIENT_URI_ID, clientUriId);
      }}, 1);
      assertTrue(result.isEmpty());
    } finally {
      CallContext.rollback();
      CallContext.end();
    }
  }

  @Test
  @Order(8)
  public void deleteTest() throws SQLException {
    try {
      CallContext.begin(properties.getProperty("datasource.jndiName"), ClientDaoIT.class.getCanonicalName());
      clientDao.delete(new HashMap<String, String>() {{
        put(ClientFieldNames.CLIENT_ID, properties.getProperty("client.id"));
      }}, 1);
      List<ClientDto> result = (List<ClientDto>) clientDao.findByPrimaryKey(new HashMap<String, String>() {{
        put(ClientFieldNames.CLIENT_ID, properties.getProperty("client.id"));
      }}, 1);
      assertTrue(result.isEmpty());
    } finally {
      CallContext.rollback();
      CallContext.end();
    }
  }

}
