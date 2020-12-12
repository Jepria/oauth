package org.jepria.oauth.client.dto;


import org.hibernate.validator.constraints.NotBlank;
import org.jepria.oauth.client.constraint.ApplicationType;
import org.jepria.oauth.client.constraint.GrantType;
import org.jepria.oauth.client.constraint.ApplicationGrantType;

import java.util.List;

@ApplicationGrantType
public class ClientCreateDto {

  @NotBlank
  String clientId;
  @NotBlank
  String clientName;
  String clientNameEn;
  @NotBlank
  @ApplicationType
  String applicationType;
  @GrantType
  List<String> grantTypes;
  List<String> scope;

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
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