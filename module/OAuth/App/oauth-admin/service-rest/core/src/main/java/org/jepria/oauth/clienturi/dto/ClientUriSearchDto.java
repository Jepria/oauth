package org.jepria.oauth.clienturi.dto;

import java.io.Serializable;

public class ClientUriSearchDto implements Serializable {
  String clientUriId;
  String clientId;

  public String getClientUriId() {
    return clientUriId;
  }

  public void setClientUriId(String clientUriId) {
    this.clientUriId = clientUriId;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }
}
