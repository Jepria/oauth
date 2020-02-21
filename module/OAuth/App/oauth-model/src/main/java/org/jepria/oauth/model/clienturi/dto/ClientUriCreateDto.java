package org.jepria.oauth.model.clienturi.dto;

import org.jepria.oauth.model.clienturi.constraint.ValidURI;

public class ClientUriCreateDto {

  String clientId;
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
