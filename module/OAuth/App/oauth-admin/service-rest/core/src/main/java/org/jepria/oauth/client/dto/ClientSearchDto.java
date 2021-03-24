package org.jepria.oauth.client.dto;

import javax.ws.rs.QueryParam;
import java.io.Serializable;

public class ClientSearchDto implements Serializable {

  @QueryParam("clientId")
  String clientId;
  @QueryParam("clientName")
  String clientName;
  @QueryParam("clientNameEn")
  String clientNameEn;
  @QueryParam("maxRowCount")
  Integer maxRowCount;

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

  public Integer getMaxRowCount() {
    return maxRowCount;
  }

  public void setMaxRowCount(Integer maxRowCount) {
    this.maxRowCount = maxRowCount;
  }
}
