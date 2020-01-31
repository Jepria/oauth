package org.jepria.oauth.authorization.dto;

public class AuthRequestSearchDtoLocal {

  Integer authRequestId;
  String authorizationCode;
  Integer operatorId;
  String clientId;
  String sessionId;
  String tokenId;
  Boolean isBlocked;
  String redirectUri;
  Boolean hasToken;

  public Integer getAuthRequestId() {
    return authRequestId;
  }

  public void setAuthRequestId(Integer authRequestId) {
    this.authRequestId = authRequestId;
  }

  public String getAuthorizationCode() {
    return authorizationCode;
  }

  public void setAuthorizationCode(String authorizationCode) {
    this.authorizationCode = authorizationCode;
  }

  public Integer getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(Integer operatorId) {
    this.operatorId = operatorId;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getTokenId() {
    return tokenId;
  }

  public void setTokenId(String tokenId) {
    this.tokenId = tokenId;
  }

  public Boolean getBlocked() {
    return isBlocked;
  }

  public void setBlocked(Boolean blocked) {
    isBlocked = blocked;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public Boolean getHasToken() {
    return hasToken;
  }

  public void setHasToken(Boolean hasToken) {
    this.hasToken = hasToken;
  }
}