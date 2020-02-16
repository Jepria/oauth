package org.jepria.oauth.model.client.dto;

import org.hibernate.validator.constraints.NotBlank;

import java.util.List;

public class ClientUpdateDto {

  @NotBlank
  String clientName;
  String clientNameEn;
  @NotBlank
  String applicationType;
  List<String> grantTypes;
  @NotBlank
  String tokenAuthMethod;

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

  public void setApplicationTypeC(String applicationType) {
    this.applicationType = applicationType;
  }

  public List<String> getGrantTypes() {
    return grantTypes;
  }

  public void setGrantTypes(List<String> grantTypes) {
    this.grantTypes = grantTypes;
  }

  public String getTokenAuthMethod() {
    return tokenAuthMethod;
  }

  public void setTokenAuthMethod(String tokenEndpointAuthMethod) {
    this.tokenAuthMethod = tokenAuthMethod;
  }
}
