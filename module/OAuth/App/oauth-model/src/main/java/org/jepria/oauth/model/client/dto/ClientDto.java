package org.jepria.oauth.model.client.dto;

import org.jepria.server.data.OptionDto;
import org.jepria.server.data.PrimaryKey;

import java.util.List;

public class ClientDto {

  @PrimaryKey
  String clientId;
  String clientSecret;
  String clientName;
  String clientNameEn;
  OptionDto<String> applicationType;
  OptionDto<String> tokenAuthMethod;
  List<OptionDto<String>> grantTypes;
  List<OptionDto<String>> responseTypes;

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

  public OptionDto<String> getApplicationType() {
    return applicationType;
  }

  public void setApplicationType(OptionDto<String> applicationType) {
    this.applicationType = applicationType;
  }

  public OptionDto<String> getTokenAuthMethod() {
    return tokenAuthMethod;
  }

  public void setTokenAuthMethod(OptionDto<String> tokenAuthMethod) {
    this.tokenAuthMethod = tokenAuthMethod;
  }

  public List<OptionDto<String>> getGrantTypes() {
    return grantTypes;
  }

  public void setGrantTypes(List<OptionDto<String>> grantTypes) {
    this.grantTypes = grantTypes;
  }

  public List<OptionDto<String>> getResponseTypes() {
    return responseTypes;
  }

  public void setResponseTypes(List<OptionDto<String>> responseTypes) {
    this.responseTypes = responseTypes;
  }
}