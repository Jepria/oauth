package org.jepria.oauth.key;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.jepria.oauth.JaxRsAdapterTestBase;
import org.junit.jupiter.api.Test;

public class KeyJaxRsAdapterIT extends JaxRsAdapterTestBase {

  private String endpointPath = "/key";
  
  @Test
  public void getActualKey() {
    RestAssured.given()
        .auth()
        .basic(username, password)
        .when()
        .get(baseUrl + endpointPath)
        .then()
        .assertThat()
        .statusCode(200);
  }
  
  @Test
  public void updateKey() {
    RestAssured.given()
        .auth()
        .basic(username, password)
        .contentType(ContentType.JSON)
        .when()
        .post(baseUrl + endpointPath)
        .then()
        .assertThat()
        .statusCode(200);
  }
  
}
