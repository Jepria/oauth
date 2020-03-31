package org.jepria.oauth.session;

import com.google.gson.reflect.TypeToken;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.jepria.oauth.JaxRsAdapterTestBase;
import org.jepria.oauth.model.session.dto.SessionDto;
import org.jepria.oauth.model.session.dto.SessionSearchDto;
import org.jepria.server.data.SearchRequestDto;
import org.jepria.server.service.rest.gson.JsonBindingProvider;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.json.bind.Jsonb;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SessionJaxRsAdapterIT extends JaxRsAdapterTestBase {

  private String endpointPath = "/session";
  private static String sessionId;
  
  @Test
  @Order(1)
  public void searchTest() {
    SearchRequestDto<SessionSearchDto> searchRequestDto = new SearchRequestDto<>();
    SessionSearchDto sessionSearchDto = new SessionSearchDto();
    sessionSearchDto.setBlocked(false);
    searchRequestDto.setTemplate(sessionSearchDto);
    Response postSearchRequestResponse = RestAssured.given()
        .auth()
        .basic(username, password)
        .contentType(ContentType.JSON)
        .header("Cache-Control", "no-cache")
        .body(searchRequestDto)
        .post(baseUrl + endpointPath + "/search");
    postSearchRequestResponse.then().assertThat().statusCode(201);
    Response searchResponse = RestAssured.given()
        .auth()
        .basic(username, password)
        .header("Cache-Control", "no-cache")
        .param("pageSize", "1")
        .param("page", "1")
        .cookie("JSESSIONID", postSearchRequestResponse.getCookie("JSESSIONID"))
        .get(postSearchRequestResponse.getHeader("Location") + "/resultset");
    if (searchResponse.statusCode() == 200) {
      Jsonb jsonb = JsonBindingProvider.getJsonb();
      Type founderListType = new TypeToken<ArrayList<SessionDto>>(){}.getType();
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
          .basic(username, password)
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
          .basic(username, password)
          .delete(baseUrl + endpointPath + "/" + sessionId);
      searchResponse.then().assertThat().statusCode(200);
    }
  }
  
}
