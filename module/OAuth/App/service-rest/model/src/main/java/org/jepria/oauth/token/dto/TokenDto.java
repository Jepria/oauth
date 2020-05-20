package org.jepria.oauth.token.dto;

public class TokenDto {
  String token_type;
  Integer expires_in;
  String access_token;
  String refresh_token;

  public String getTokenType() {
    return token_type;
  }

  public void setTokenType(String token_type) {
    this.token_type = token_type;
  }

  public Integer getExpiresIn() {
    return expires_in;
  }

  public void setExpiresIn(Integer expires_in) {
    this.expires_in = expires_in;
  }

  public String getAccessToken() {
    return access_token;
  }

  public void setAccessToken(String access_token) {
    this.access_token = access_token;
  }

  public String getRefreshToken() {
    return refresh_token;
  }

  public void setRefreshToken(String refresh_token) {
    this.refresh_token = refresh_token;
  }
}
