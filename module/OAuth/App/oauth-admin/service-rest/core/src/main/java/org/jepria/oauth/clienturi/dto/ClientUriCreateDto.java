package org.jepria.oauth.clienturi.dto;

import org.jepria.oauth.clienturi.constraint.ValidURI;

import javax.validation.constraints.NotBlank;

public class ClientUriCreateDto {

  String clientId;
  @NotBlank
  @ValidURI
  String clientUri;

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientUri() {
    return clientUri;
  }

  public void setClientUri(String clientUri) {
    this.clientUri = clientUri;
  }
}
