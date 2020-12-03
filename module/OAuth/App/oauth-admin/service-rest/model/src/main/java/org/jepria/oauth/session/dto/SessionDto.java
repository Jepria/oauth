package org.jepria.oauth.session.dto;

import org.jepria.server.data.OptionDto;
import org.jepria.server.data.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

public class SessionDto implements Serializable {

  @PrimaryKey
  String sessionId;
  String authorizationCode;
  Date dateIns;
  OptionDto<Integer> operator;
  String operatorLogin;
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
  String redirectUri;
  OptionDto<String> client;

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getAuthorizationCode() {
    return authorizationCode;
  }

  public void setAuthorizationCode(String authorizationCode) {
    this.authorizationCode = authorizationCode;
  }

  public Date getDateIns() {
    return dateIns;
  }

  public void setDateIns(Date dateIns) {
    this.dateIns = dateIns;
  }

  public OptionDto<Integer> getOperator() {
    return operator;
  }

  public void setOperator(OptionDto<Integer> operator) {
    this.operator = operator;
  }

  public String getOperatorLogin() {
    return operatorLogin;
  }

  public void setOperatorLogin(String operatorLogin) {
    this.operatorLogin = operatorLogin;
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

  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public OptionDto<String> getClient() {
    return client;
  }

  public void setClient(OptionDto<String> client) {
    this.client = client;
  }
}