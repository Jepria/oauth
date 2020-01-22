package org.jepria.oauth.authorization.dto;

public class AuthRequestSearchDto {

  Integer authRequestId;
  Integer operatorId;
  String clientId;
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
