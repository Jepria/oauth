package org.jepria.oauth.flow;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.jepria.oauth.JaxRsAdapterTestBase;
import org.jepria.oauth.model.token.dto.TokenDto;
import org.jepria.oauth.model.token.dto.TokenInfoDto;
import org.jepria.oauth.sdk.GrantType;
import org.jepria.oauth.sdk.OAuthConstants;
import org.jepria.oauth.sdk.ResponseType;
import org.jepria.oauth.sdk.util.URIUtil;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static org.jepria.oauth.sdk.OAuthConstants.ACCESS_DENIED;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthorizationCodeFlowIT extends JaxRsAdapterTestBase {
  
  private static final String authorizationEndpoint = "/authorize";
  private static final String tokenEndpoint = "/token";
  private static final String authenticationEndpoint = "/authenticate";
  private static final String introspectionEndpoint = "/token/introspect";
  private static final String revocationEndpoint = "/token/revoke";
  
  @Test
  public void authCodeAuthorizeTest() throws UnsupportedEncodingException {
    Response authorizeResponse = RestAssured.given()
        .redirects()
        .follow(false)
        .param("response_type", ResponseType.CODE)
        .param("client_id", properties.getProperty("client.id"))
        .param("redirect_uri", Base64.getUrlEncoder().withoutPadding().encodeToString(properties.getProperty("client.redirect_uri").getBytes()))
        .param("state", String.valueOf(new Date().getTime()))
        .get(baseUrl + authorizationEndpoint);
    authorizeResponse.then().assertThat().statusCode(302);
    URI location = URI.create(authorizeResponse.getHeader("Location"));
    String authenticateUrl = baseUrl + authenticationEndpoint + "?" + location.getQuery();
    Response authenticationResponse = RestAssured.given()
        .body("username=" + username + "&password=" + password)
        .contentType(ContentType.URLENC)
        .post(authenticateUrl);
    authenticationResponse.then().assertThat().statusCode(302);
    assertTrue(authenticationResponse.getHeader("Location").startsWith(properties.getProperty("client.redirect_uri")));
    location = URI.create(authenticationResponse.getHeader("Location"));
    String authCode = URIUtil.parseParameters(location.getQuery(), "UTF-8").get(OAuthConstants.CODE);
    assertNotNull(authCode);
    Response tokenResponse = RestAssured.given()
        .auth().preemptive().basic(properties.getProperty("client.id"), properties.getProperty("client.secret"))
        .when()
        .contentType(ContentType.URLENC)
        .body("grant_type=" + GrantType.AUTHORIZATION_CODE +
            "&redirect_uri=" + Base64.getUrlEncoder().withoutPadding().encodeToString(properties.getProperty("client.redirect_uri").getBytes()) +
            "&code=" + authCode)
        .post(baseUrl + tokenEndpoint);
    tokenResponse.then().assertThat().statusCode(200);
    TokenDto tokenDto = tokenResponse.body().as(TokenDto.class);
    assertNotNull(tokenDto);
    //проверим полученый токен
    Response tokenInfoResponse = RestAssured.given()
        .auth().preemptive().basic(properties.getProperty("client.id"), properties.getProperty("client.secret"))
        .when()
        .body("token=" + tokenDto.getAccessToken())
        .contentType(ContentType.URLENC)
        .post(baseUrl + introspectionEndpoint);
    tokenInfoResponse.then().assertThat().statusCode(200);
    TokenInfoDto tokenInfoDto = tokenInfoResponse.as(TokenInfoDto.class);
    assertTrue(tokenInfoDto.getActive());
    //revoke token
    Response revokeTokenResponse = RestAssured.given()
        .auth().preemptive().basic(properties.getProperty("client.id"), properties.getProperty("client.secret"))
        .when()
        .body("token=" + tokenDto.getAccessToken())
        .contentType(ContentType.URLENC)
        .post(baseUrl + revocationEndpoint);
    revokeTokenResponse.then().assertThat().statusCode(200);
  }
  
  @Test
  public void authCodeAuthorizePKCETest() throws UnsupportedEncodingException, NoSuchAlgorithmException {
    SecureRandom sr = SecureRandom.getInstanceStrong();
    byte[] buffer = new byte[16];
    sr.nextBytes(buffer);
    String codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    String codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(md.digest(codeVerifier.getBytes()));
    Response authorizeResponse = RestAssured.given()
        .redirects()
        .follow(false)
        .param("response_type", ResponseType.CODE)
        .param("client_id", properties.getProperty("client.id"))
        .param("redirect_uri", Base64.getUrlEncoder().withoutPadding().encodeToString(properties.getProperty("client.redirect_uri").getBytes()))
        .param("code_challenge", codeChallenge)
        .param("state", String.valueOf(new Date().getTime()))
        .get(baseUrl + authorizationEndpoint);
    authorizeResponse.then().assertThat().statusCode(302);
    URI location = URI.create(authorizeResponse.getHeader("Location"));
    String authenticateUrl = baseUrl + authenticationEndpoint + "?" + location.getQuery();
    Response authenticationResponse = RestAssured.given()
        .body("username=" + username + "&password=" + password)
        .contentType(ContentType.URLENC)
        .post(authenticateUrl);
    authenticationResponse.then().assertThat().statusCode(302);
    assertTrue(authenticationResponse.getHeader("Location").startsWith(properties.getProperty("client.redirect_uri")));
    location = URI.create(authenticationResponse.getHeader("Location"));
    String authCode = URIUtil.parseParameters(location.getQuery(), "UTF-8").get(OAuthConstants.CODE);
    assertNotNull(authCode);
    Response tokenResponse = RestAssured.given()
        .when()
        .contentType(ContentType.URLENC)
        .body("grant_type=" + GrantType.AUTHORIZATION_CODE +
            "&redirect_uri=" + Base64.getUrlEncoder().withoutPadding().encodeToString(properties.getProperty("client.redirect_uri").getBytes()) +
            "&code=" + authCode +
            "&client_id=" + properties.getProperty("client.id") +
            "&code_verifier=" + codeVerifier)
        .post(baseUrl + tokenEndpoint);
    tokenResponse.then().assertThat().statusCode(200);
    TokenDto tokenDto = tokenResponse.body().as(TokenDto.class);
    assertNotNull(tokenDto);
    //проверим полученый токен
    Response tokenInfoResponse = RestAssured.given()
        .auth().preemptive().basic(properties.getProperty("client.id"), properties.getProperty("client.secret"))
        .when()
        .body("token=" + tokenDto.getAccessToken())
        .contentType(ContentType.URLENC)
        .post(baseUrl + introspectionEndpoint);
    tokenInfoResponse.then().assertThat().statusCode(200);
    TokenInfoDto tokenInfoDto = tokenInfoResponse.as(TokenInfoDto.class);
    assertTrue(tokenInfoDto.getActive());
    //revoke token
    Response revokeTokenResponse = RestAssured.given()
        .auth().preemptive().basic(properties.getProperty("client.id"), properties.getProperty("client.secret"))
        .when()
        .body("token=" + tokenDto.getAccessToken())
        .contentType(ContentType.URLENC)
        .post(baseUrl + revocationEndpoint);
    revokeTokenResponse.then().assertThat().statusCode(200);
  }
  
  @Test
  public void wrongAuthorizationRequestTest() throws UnsupportedEncodingException {
    Response authorizeResponse = RestAssured.given()
        .redirects()
        .follow(false)
        .param("state", String.valueOf(new Date().getTime()))
        .get(baseUrl + authorizationEndpoint);
    authorizeResponse.then().assertThat().statusCode(302);
    URI location = URI.create(authorizeResponse.getHeader("Location"));
    Map<String, String> queryParams = URIUtil.parseParameters(location.getQuery(), "UTF-8");
    assertNotNull(queryParams.get("error"));
  }
  
  @Test
  public void wrongUserCredentialsTest() throws UnsupportedEncodingException {
    Response authorizeResponse = RestAssured.given()
        .redirects()
        .follow(false)
        .param("response_type", ResponseType.CODE)
        .param("client_id", properties.getProperty("client.id"))
        .param("redirect_uri", Base64.getUrlEncoder().withoutPadding().encodeToString(properties.getProperty("client.redirect_uri").getBytes()))
        .param("state", String.valueOf(new Date().getTime()))
        .get(baseUrl + authorizationEndpoint);
    authorizeResponse.then().assertThat().statusCode(302);
    URI location = URI.create(authorizeResponse.getHeader("Location"));
    String authenticateUrl = baseUrl + authenticationEndpoint + "?" + location.getQuery();
    Response authenticationResponse = RestAssured.given()
        .body("username=" + "wrongusername" + "&password=" + "wrongpassword")
        .contentType(ContentType.URLENC)
        .post(authenticateUrl);
    authenticationResponse.then().assertThat().statusCode(302);
    location = URI.create(authenticationResponse.getHeader("Location"));
    Map<String, String> queryParams = URIUtil.parseParameters(location.getQuery(), "UTF-8");
    assertTrue(queryParams.get("error").equals(ACCESS_DENIED));
  }
}
