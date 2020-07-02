package org.jepria.oauth.client.dto;

import org.jepria.server.data.OptionDto;
import org.jepria.server.data.PrimaryKey;

import java.util.List;

public class ClientDto {

  @PrimaryKey
  String clientId;
  String clientSecret;
  String clientName;
  String clientNameEn;
  String applicationType;
  String tokenAuthMethod;
  List<String> grantTypes;
  List<String> responseTypes;
  List<OptionDto<String>> scopes;

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

  public String getTokenAuthMethod() {
    return tokenAuthMethod;
  }

  public void setTokenAuthMethod(String tokenAuthMethod) {
    this.tokenAuthMethod = tokenAuthMethod;
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

  public List<OptionDto<String>> getScopes() {
    return scopes;
  }

  public void setScopes(List<OptionDto<String>> scopes) {
    this.scopes = scopes;
  }
}