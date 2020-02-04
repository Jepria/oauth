package org.jepria.oauth.authorization.dto;

import java.util.Date;

public class AuthRequestUpdateDto {
  Integer authRequestId;
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
  Boolean isBlocked;

  public Integer getAuthRequestId() {
    return authRequestId;
  }

  public void setAuthRequestId(Integer authRequestId) {
    this.authRequestId = authRequestId;
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

  public Boolean getBlocked() {
    return isBlocked;
  }

  public void setBlocked(Boolean blocked) {
    isBlocked = blocked;
  }
}