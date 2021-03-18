package org.jepria.oauth.client.dto;

import org.jepria.oauth.client.constraint.ApplicationGrantType;
import org.jepria.oauth.client.constraint.ApplicationType;
import org.jepria.oauth.client.constraint.GrantType;
import org.jepria.oauth.clienturi.constraint.ValidURI;

import javax.validation.constraints.NotBlank;
import java.util.List;

@ApplicationGrantType
public class ClientUpdateDto {

  @NotBlank
  private String clientName;
  private String clientNameEn;
  @NotBlank
  @ApplicationType
  private String applicationType;
  @ValidURI
  private String loginModuleUri;
  @GrantType
  private List<String> grantTypes;
  private List<String> scope;
  
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
  
  public List<String> getScope() {
    return scope;
  }
  
  public void setScope(List<String> scope) {
    this.scope = scope;
  }
}
