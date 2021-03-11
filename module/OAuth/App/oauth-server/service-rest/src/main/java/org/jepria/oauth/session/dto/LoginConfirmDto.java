package org.jepria.oauth.session.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class LoginConfirmDto {
  @NotBlank
  private String username;
  @NotNull
  private Integer operatorId;
  @NotBlank
  private String responseType;
  @NotBlank
  private String redirectUri;
  @NotBlank
  private String clientId;
  private String state;
  
  public String getUsername() {
    return username;
  }
  
  public void setUsername(String username) {
    this.username = username;
  }
  
  public Integer getOperatorId() {
    return operatorId;
  }
  
  public void setOperatorId(Integer operatorId) {
    this.operatorId = operatorId;
  }
  
  public String getResponseType() {
    return responseType;
  }
  
  public void setResponseType(String responseType) {
    this.responseType = responseType;
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
  
  public String getState() {
    return state;
  }
  
  public void setState(String state) {
    this.state = state;
  }
}
