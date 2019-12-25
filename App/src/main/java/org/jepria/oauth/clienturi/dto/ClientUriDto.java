package org.jepria.oauth.clienturi.dto;

import org.jepria.server.data.PrimaryKey;

public class ClientUriDto {

  @PrimaryKey
  String clientId;
  @PrimaryKey
  Integer clientUriId;
  String clientUri;

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public Integer getClientUriId() {
    return clientUriId;
  }

  public void setClientUriId(Integer clientUriId) {
    this.clientUriId = clientUriId;
  }

  public String getClientUri() {
    return clientUri;
  }

  public void setClientUri(String clientUri) {
    this.clientUri = clientUri;
  }
}
