package org.jepria.oauth.client.dto;

import org.hibernate.validator.constraints.NotBlank;
import org.jepria.oauth.client.constraint.ApplicationGrantType;
import org.jepria.oauth.client.constraint.ApplicationType;
import org.jepria.oauth.client.constraint.GrantType;

import java.util.List;

@ApplicationGrantType
public class ClientUpdateDto {

  @NotBlank
  String clientName;
  String clientNameEn;
  @NotBlank
  @ApplicationType
  String applicationType;
  @GrantType
  List<String> grantTypes;

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
}
