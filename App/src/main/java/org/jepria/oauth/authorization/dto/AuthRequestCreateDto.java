package org.jepria.oauth.authorization.dto;

import java.util.Date;

public class AuthRequestCreateDto {

  String authorizationCode;
  String redirectUri;
  String clientId;
  Integer operatorId;
  String tokenId;
  Date tokenDateIns;

  public String getAuthorizationCode() {
    return authorizationCode;
  }

  public void setAuthorizationCode(String authorizationCode) {
    this.authorizationCode = authorizationCode;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public Integer getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(Integer operatorId) {
    this.operatorId = operatorId;
  }

  public String getTokenId() {
    return tokenId;
  }

  public void setTokenId(String tokenId) {
    this.tokenId = tokenId;
  }

  public Date getTokenDateIns() {
    return tokenDateIns;
  }

  public void setTokenDateIns(Date tokenDateIns) {
    this.tokenDateIns = tokenDateIns;
  }
}
