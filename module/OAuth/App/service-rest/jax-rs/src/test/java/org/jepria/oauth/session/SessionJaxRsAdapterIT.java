package org.jepria.oauth.session;

import com.google.gson.reflect.TypeToken;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.jepria.oauth.JaxRsAdapterTestBase;
import org.jepria.oauth.sdk.TokenResponse;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDto;
import org.jepria.server.data.SearchRequestDto;
import org.jepria.server.service.rest.gson.JsonBindingProvider;
import org.junit.jupiter.api.*;

import javax.json.bind.Jsonb;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SessionJaxRsAdapterIT extends JaxRsAdapterTestBase {

  private String endpointPath = "/session";
  private static String sessionId;
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
  public void searchTest() {
    SearchRequestDto<SessionSearchDto> searchRequestDto = new SearchRequestDto<>();
    SessionSearchDto sessionSearchDto = new SessionSearchDto();
    sessionSearchDto.setBlocked(false);
    searchRequestDto.setTemplate(sessionSearchDto);
    Response postSearchRequestResponse = RestAssured.given()
      .auth()
      .preemptive().oauth2(accessToken)
      .contentType(ContentType.JSON)
      .header("Cache-Control", "no-cache")
      .body(searchRequestDto)
      .post(baseUrl + endpointPath + "/search");
    postSearchRequestResponse.then().assertThat().statusCode(201);
    Response searchResponse = RestAssured.given()
      .auth()
      .preemptive().oauth2(accessToken)
      .header("Cache-Control", "no-cache")
      .param("pageSize", "1")
      .param("page", "1")
      .cookie("JSESSIONID", postSearchRequestResponse.getCookie("JSESSIONID"))
      .get(postSearchRequestResponse.getHeader("Location") + "/resultset");
    if (searchResponse.statusCode() == 200) {
      Jsonb jsonb = JsonBindingProvider.getJsonb();
      Type founderListType = new TypeToken<ArrayList<SessionDto>>() {
      }.getType();
      List<SessionDto> result = jsonb.fromJson(searchResponse.getBody().asString(), founderListType);
      assertFalse(result.isEmpty());
      sessionId = result.get(0).getSessionId().toString();
    } else {
      searchResponse.then().assertThat().statusCode(204);
    }
  }

  @Test
  @Order(2)
  public void getRecordByIdTest() {
    if (sessionId != null) {
      Response searchResponse = RestAssured.given()
        .auth()
        .preemptive().oauth2(accessToken)
        .get(baseUrl + endpointPath + "/" + sessionId);
      searchResponse.then().assertThat().statusCode(200);
    }
  }

  @Test
  @Order(3)
  public void deleteRecordTest() {
    if (sessionId != null) {
      Response searchResponse = RestAssured.given()
        .auth()
        .preemptive().oauth2(accessToken)
        .delete(baseUrl + endpointPath + "/" + sessionId);
      searchResponse.then().assertThat().statusCode(200);
    }
  }

}
