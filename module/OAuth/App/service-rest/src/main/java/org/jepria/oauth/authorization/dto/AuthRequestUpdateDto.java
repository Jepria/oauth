package org.jepria.oauth.authorization.dto;

import java.util.Date;

public class AuthRequestUpdateDto {
  Integer authRequestId;
  Integer operatorId;
  String tokenId;
  Date tokenDateIns;
  String sessionId;
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

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public Boolean getBlocked() {
    return isBlocked;
  }

  public void setBlocked(Boolean blocked) {
    isBlocked = blocked;
  }
}
