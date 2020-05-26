package org.jepria.oauth.session.dto;

public class SessionSearchDto {

  Integer sessionId;
  String authorizationCode;
  Integer operatorId;
  String clientId;
  String sessionTokenId;
  String accessTokenId;
  String refreshTokenId;
  Boolean isBlocked;
  String redirectUri;
  Boolean hasToken;

  public Integer getSessionId() {
    return sessionId;
  }

  public void setSessionId(Integer sessionId) {
    this.sessionId = sessionId;
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

  public String getSessionTokenId() {
    return sessionTokenId;
  }

  public void setSessionTokenId(String sessionTokenId) {
    this.sessionTokenId = sessionTokenId;
  }

  public String getAccessTokenId() {
    return accessTokenId;
  }

  public void setAccessTokenId(String accessTokenId) {
    this.accessTokenId = accessTokenId;
  }

  public String getRefreshTokenId() {
    return refreshTokenId;
  }

  public void setRefreshTokenId(String refreshTokenId) {
    this.refreshTokenId = refreshTokenId;
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