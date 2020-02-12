package org.jepria.oauth.session.dto;

public class SessionSearchDto {

  Integer sessionId;
  Integer operatorId;
  String clientId;
  Boolean isBlocked;

  public Integer getSessionId() {
    return sessionId;
  }

  public void setSessionId(Integer sessionId) {
    this.sessionId = sessionId;
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

  public Boolean getBlocked() {
    return isBlocked;
  }

  public void setBlocked(Boolean blocked) {
    isBlocked = blocked;
  }
}