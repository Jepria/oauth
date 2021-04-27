package org.jepria.oauth.client.dto;

import org.jepria.server.data.OptionDto;
import org.jepria.server.data.PrimaryKey;

import java.io.Serializable;
import java.util.List;

public class ClientDto implements Serializable {

  @PrimaryKey
  private String clientId;
  private String clientSecret;
  private String clientName;
  private String clientNameEn;
  private String applicationType;
  private String loginModuleUri;
  private List<String> grantTypes;
  private List<String> responseTypes;
  private List<OptionDto<String>> scope;
  
  public String getClientId() {
    return clientId;
  }
  
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }
  
  public String getClientSecret() {
    return clientSecret;
  }
  
  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }
  
  public String getClientName() {
    return clientName;
  }
  
  public void setClientName(String clientName) {
    this.clientName = clientName;
  }
  
  public String getClientNameEn() {
    return clientNameEn;
  }
  
  public void setClientNameEn(String clientNameEn) {
    this.clientNameEn = clientNameEn;
  }
  
  public String getApplicationType() {
    return applicationType;
  }
  
  public void setApplicationType(String applicationType) {
    this.applicationType = applicationType;
  }
  
  public String getLoginModuleUri() {
    return loginModuleUri;
  }
  
  public void setLoginModuleUri(String loginModuleUri) {
    this.loginModuleUri = loginModuleUri;
  }
  
  public List<String> getGrantTypes() {
    return grantTypes;
  }
  
  public void setGrantTypes(List<String> grantTypes) {
    this.grantTypes = grantTypes;
  }
  
  public List<String> getResponseTypes() {
    return responseTypes;
  }
  
  public void setResponseTypes(List<String> responseTypes) {
    this.responseTypes = responseTypes;
  }
  
  public List<OptionDto<String>> getScope() {
    return scope;
  }
  
  public void setScope(List<OptionDto<String>> scope) {
    this.scope = scope;
  }
}