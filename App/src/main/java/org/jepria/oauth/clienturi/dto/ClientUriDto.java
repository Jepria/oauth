package org.jepria.oauth.clienturi.dto;

public class ClientUriDto {
  Integer clientUriId;
  Integer clientId;
  String clientUri;

  public Integer getClientUriId() {
    return clientUriId;
  }

  public void setClientUriId(Integer clientUriId) {
    this.clientUriId = clientUriId;
  }

  public Integer getClientId() {
    return clientId;
  }

  public void setClientId(Integer clientId) {
    this.clientId = clientId;
  }

  public String getClientUri() {
    return clientUri;
  }

  public void setClientUri(String clientUri) {
    this.clientUri = clientUri;
  }
}
