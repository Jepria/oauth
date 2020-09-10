package org.jepria.oauth.key;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.jepria.oauth.JaxRsAdapterTestBase;
import org.jepria.oauth.sdk.TokenResponse;
import org.jepria.server.service.rest.gson.JsonBindingProvider;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KeyJaxRsAdapterIT extends JaxRsAdapterTestBase {

  private String endpointPath = "/key";
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
  public void getActualKey() {
    Response response = RestAssured.given()
      .auth()
      .preemptive().oauth2(accessToken)
      .when()
      .get(baseUrl + endpointPath);
    response.then().assertThat().statusCode(200);
  }

  @Test
  @Order(2)
  public void updateKey() {
    Response response = RestAssured.given()
      .auth()
      .preemptive().oauth2(accessToken)
      .contentType(ContentType.JSON)
      .when()
      .post(baseUrl + endpointPath);
    response.then().assertThat().statusCode(200);
  }

}
