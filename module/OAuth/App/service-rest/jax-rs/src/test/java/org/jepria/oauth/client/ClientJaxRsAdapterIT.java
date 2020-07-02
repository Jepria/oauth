package org.jepria.oauth.client;

import com.google.gson.reflect.TypeToken;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.jepria.oauth.JaxRsAdapterTestBase;
import org.jepria.oauth.client.dto.ClientCreateDto;
import org.jepria.oauth.client.dto.ClientDto;
import org.jepria.oauth.client.dto.ClientSearchDto;
import org.jepria.oauth.client.dto.ClientUpdateDto;
import org.jepria.oauth.clienturi.dto.ClientUriCreateDto;
import org.jepria.oauth.clienturi.dto.ClientUriDto;
import org.jepria.oauth.sdk.ApplicationType;
import org.jepria.oauth.sdk.TokenResponse;
import org.jepria.server.data.SearchRequestDto;
import org.jepria.server.service.rest.gson.JsonBindingProvider;
import org.junit.jupiter.api.*;

import javax.json.bind.Jsonb;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientJaxRsAdapterIT extends JaxRsAdapterTestBase {

  private String clientEndpointPath = "/client";
  private String clientUriEndpointPath = "/client-uri";
  private static String createdUrl;
  private static String clientId;
  private static String clientUriId;
  private static String accessToken;

  @BeforeAll
  public static void login() {
    Response response =
      RestAssured.given()
        .auth().preemptive().basic(properties.getProperty("client.id"), properties.getProperty("client.secret"))
        .contentType("application/x-www-form-urlencoded").log().all()
        .formParam("grant_type", "password")
        .formParam("username", username)
        .formParam("password", password)
        .when()
        .post(baseUrl + "/token");
    response.then().assertThat().statusCode(200);
    TokenResponse tokenResponse = JsonBindingProvider.getJsonb().fromJson(response.getBody().asString(), TokenResponse.class);
    accessToken = tokenResponse.getAccessToken();
  }

  @Test
  @Order(1)
  public void createClientTest() {
    ClientCreateDto clientCreateDto = new ClientCreateDto();
    clientCreateDto.setClientName("createdTestClient_" + new Date().getTime());
    clientCreateDto.setClientNameEn("createdTestClientEn_ " + new Date().getTime());
    clientCreateDto.setApplicationType(ApplicationType.WEB);
    clientCreateDto.setGrantTypes(ApplicationType.getApplicationGrantTypes(ApplicationType.WEB));
    Response response = RestAssured.given()
      .auth()
      .preemptive().oauth2(accessToken)
      .contentType(ContentType.JSON)
      .body(clientCreateDto)
      .post(baseUrl + clientEndpointPath);
    response.then().assertThat().statusCode(201);
    createdUrl = response.getHeader("Location");
  }

  @Test
  @Order(2)
  public void getClientByIdTest() {
    Response response = RestAssured.given()
      .auth()
      .preemptive().oauth2(accessToken)
      .get(createdUrl);
    response.then().assertThat().statusCode(200);
    ClientDto clientDto = JsonBindingProvider.getJsonb().fromJson(response.getBody().asString(), ClientDto.class);
    assertNotNull(clientDto);
    clientId = clientDto.getClientId();
  }

  @Test
  @Order(3)
  public void createClientUriTest() {
    ClientUriCreateDto clientUriCreateDto = new ClientUriCreateDto();
    clientUriCreateDto.setClientUri("http://test-client.com");
    Response response = RestAssured.given()
      .auth()
      .preemptive().oauth2(accessToken)
      .contentType(ContentType.JSON)
      .body(clientUriCreateDto)
      .post(baseUrl + clientEndpointPath + "/" + clientId + clientUriEndpointPath);
    response.then().assertThat().statusCode(201);
  }

  @Test
  @Order(4)
  public void findClientUriTest() {
    Response response = RestAssured.given()
      .auth()
      .preemptive().oauth2(accessToken)
      .get(baseUrl + clientEndpointPath + "/" + clientId + clientUriEndpointPath);
    response.then().assertThat().statusCode(200);
    Jsonb jsonb = JsonBindingProvider.getJsonb();
    Type founderListType = new TypeToken<ArrayList<ClientUriDto>>() {
    }.getType();
    List<ClientUriDto> result = jsonb.fromJson(response.getBody().asString(), founderListType);
    assertFalse(result.isEmpty());
    clientUriId = result.get(0).getClientUriId().toString();
  }

  @Test
  @Order(5)
  public void deleteClientUriTest() {
    Response response = RestAssured.given()
      .auth()
      .preemptive().oauth2(accessToken)
      .delete(baseUrl + clientEndpointPath + "/" + clientId + clientUriEndpointPath + "/" + clientUriId);
    response.then().assertThat().statusCode(200);
  }

  @Test
  @Order(6)
  public void updateClientTest() {
    ClientUpdateDto clientUpdateDto = new ClientUpdateDto();
    clientUpdateDto.setClientName("updatedClient_" + new Date().getTime());
    clientUpdateDto.setClientNameEn("updatedClientEn_" + new Date().getTime());
    clientUpdateDto.setApplicationType(ApplicationType.NATIVE);
    clientUpdateDto.setGrantTypes(ApplicationType.getApplicationGrantTypes(ApplicationType.NATIVE));
    Response response = RestAssured.given()
      .auth()
      .preemptive().oauth2(accessToken)
      .contentType(ContentType.JSON)
      .body(clientUpdateDto)
      .put(baseUrl + clientEndpointPath + "/" + clientId);
    response.then().assertThat().statusCode(200);
  }

  @Test
  @Order(7)
  public void findClientTest() {
    ClientSearchDto clientSearchDto = new ClientSearchDto();
    clientSearchDto.setClientName("updatedClient");
    clientSearchDto.setClientNameEn("updatedClientEn");
    clientSearchDto.setClientId(clientId);
    SearchRequestDto<ClientSearchDto> searchRequestDto = new SearchRequestDto<>();
    searchRequestDto.setTemplate(clientSearchDto);
    Response postSearchRequestResponse = RestAssured.given()
      .auth()
      .preemptive().oauth2(accessToken)
      .contentType(ContentType.JSON)
      .header("Cache-Control", "no-cache")
      .body(searchRequestDto)
      .post(baseUrl + clientEndpointPath + "/search");
    postSearchRequestResponse.then().assertThat().statusCode(201);
    Response searchResponse = RestAssured.given()
      .auth()
      .preemptive().oauth2(accessToken)
      .header("Cache-Control", "no-cache")
      .param("pageSize", "1")
      .param("page", "1")
      .cookie("JSESSIONID", postSearchRequestResponse.getCookie("JSESSIONID"))
      .get(postSearchRequestResponse.getHeader("Location") + "/resultset");
    searchResponse.then().assertThat().statusCode(200);
    Jsonb jsonb = JsonBindingProvider.getJsonb();
    Type founderListType = new TypeToken<ArrayList<ClientDto>>() {
    }.getType();
    List<ClientDto> result = jsonb.fromJson(searchResponse.getBody().asString(), founderListType);
    assertFalse(result.isEmpty());
  }

  @Test
  @Order(8)
  public void deleteClientTest() {
    Response response = RestAssured.given()
      .auth()
      .preemptive().oauth2(accessToken)
      .delete(baseUrl + clientEndpointPath + "/" + clientId);
    response.then().assertThat().statusCode(200);
  }

}
