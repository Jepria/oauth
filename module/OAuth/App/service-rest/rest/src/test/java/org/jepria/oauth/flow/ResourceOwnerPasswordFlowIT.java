package org.jepria.oauth.flow;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.jepria.oauth.JaxRsAdapterTestBase;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.oauth.token.dto.TokenInfoDto;
import org.jepria.oauth.sdk.GrantType;
import org.jepria.oauth.sdk.util.URIUtil;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceOwnerPasswordFlowIT extends JaxRsAdapterTestBase {
  
  private static String tokenEndpoint = "/token";
  private static String introspectionEndpoint = "/token/introspect";
  private static String revocationEndpoint = "/token/revoke";
  
  @Test
  public void passwordFlowTest() throws UnsupportedEncodingException {
    Map<String, String> params = new HashMap<>();
    params.put("grant_type", GrantType.PASSWORD);
    params.put("client_id", properties.getProperty("client.id"));
    params.put("username", username);
    params.put("password", password);
    Response response = RestAssured.given()
        .body(URIUtil.serializeParameters(params, "UTF-8"))
        .contentType(ContentType.URLENC)
        .post(baseUrl + tokenEndpoint);
    response.then().assertThat().statusCode(200);
    TokenDto tokenDto = response.body().as(TokenDto.class);
    assertNotNull(tokenDto);
    //проверим полученый токен
    Response tokenInfoResponse = RestAssured.given()
        .auth().preemptive().basic(properties.getProperty("client.id"), properties.getProperty("client.secret"))
        .body("token=" + tokenDto.getAccessToken())
        .contentType(ContentType.URLENC)
        .post(baseUrl + introspectionEndpoint);
    tokenInfoResponse.then().assertThat().statusCode(200);
    TokenInfoDto tokenInfoDto = tokenInfoResponse.as(TokenInfoDto.class);
    assertTrue(tokenInfoDto.getActive());
    //revoke token
    Response revokeTokenResponse = RestAssured.given()
        .auth().preemptive().basic(properties.getProperty("client.id"), properties.getProperty("client.secret"))
        .body("token=" + tokenDto.getAccessToken())
        .contentType(ContentType.URLENC)
        .post(baseUrl + revocationEndpoint);
    revokeTokenResponse.then().assertThat().statusCode(200);
  }
  
  @Test
  public void wrongCredentialsTest() throws UnsupportedEncodingException {
    Map<String, String> params = new HashMap<>();
    params.put("grant_type", GrantType.PASSWORD);
    params.put("username", "wrong");
    params.put("password", "user");
    Response response = RestAssured.given()
        .body(URIUtil.serializeParameters(params, "UTF-8"))
        .contentType(ContentType.URLENC)
        .post(baseUrl + tokenEndpoint);
    response.then().assertThat().statusCode(401);
  }
}
