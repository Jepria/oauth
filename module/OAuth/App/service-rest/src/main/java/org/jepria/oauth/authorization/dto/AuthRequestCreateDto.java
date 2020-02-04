package org.jepria.oauth.authorization.dto;

import java.util.Date;

public class AuthRequestCreateDto {

  String authorizationCode;
  String redirectUri;
  String clientId;
  Integer operatorId;
  String accessTokenId;
  Date accessTokenDateIns;
  Date accessTokenDateFinish;
  String sessionTokenId;
  Date sessionTokenDateIns;
  Date sessionTokenDateFinish;
  String refreshTokenId;
  Date refreshTokenDateIns;
  Date refreshTokenDateFinish;
  String codeChallenge;

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

  public String getAccessTokenId() {
    return accessTokenId;
  }

  public void setAccessTokenId(String accessTokenId) {
    this.accessTokenId = accessTokenId;
  }

  public Date getAccessTokenDateIns() {
    return accessTokenDateIns;
  }

  public void setAccessTokenDateIns(Date accessTokenDateIns) {
    this.accessTokenDateIns = accessTokenDateIns;
  }

  public Date getAccessTokenDateFinish() {
    return accessTokenDateFinish;
  }

  public void setAccessTokenDateFinish(Date accessTokenDateFinish) {
    this.accessTokenDateFinish = accessTokenDateFinish;
  }

  public String getSessionTokenId() {
    return sessionTokenId;
  }

  public void setSessionTokenId(String sessionTokenId) {
    this.sessionTokenId = sessionTokenId;
  }

  public Date getSessionTokenDateIns() {
    return sessionTokenDateIns;
  }

  public void setSessionTokenDateIns(Date sessionTokenDateIns) {
    this.sessionTokenDateIns = sessionTokenDateIns;
  }

  public Date getSessionTokenDateFinish() {
    return sessionTokenDateFinish;
  }

  public void setSessionTokenDateFinish(Date sessionTokenDateFinish) {
    this.sessionTokenDateFinish = sessionTokenDateFinish;
  }

  public String getRefreshTokenId() {
    return refreshTokenId;
  }

  public void setRefreshTokenId(String refreshTokenId) {
    this.refreshTokenId = refreshTokenId;
  }

  public Date getRefreshTokenDateIns() {
    return refreshTokenDateIns;
  }

  public void setRefreshTokenDateIns(Date refreshTokenDateIns) {
    this.refreshTokenDateIns = refreshTokenDateIns;
  }

  public Date getRefreshTokenDateFinish() {
    return refreshTokenDateFinish;
  }

  public void setRefreshTokenDateFinish(Date refreshTokenDateFinish) {
    this.refreshTokenDateFinish = refreshTokenDateFinish;
  }

  public String getCodeChallenge() {
    return codeChallenge;
  }

  public void setCodeChallenge(String codeChallenge) {
    this.codeChallenge = codeChallenge;
  }
}
