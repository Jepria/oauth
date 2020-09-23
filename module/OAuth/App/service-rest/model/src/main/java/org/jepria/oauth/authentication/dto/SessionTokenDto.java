package org.jepria.oauth.authentication.dto;

import java.util.Date;

public class SessionTokenDto {
  String token;
  Date expirationDate;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Date getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }
}
