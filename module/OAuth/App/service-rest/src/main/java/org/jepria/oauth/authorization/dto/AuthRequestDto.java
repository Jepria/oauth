package org.jepria.oauth.authorization.dto;

import org.jepria.server.data.OptionDto;
import org.jepria.server.data.PrimaryKey;

import java.util.Date;

public class AuthRequestDto {

  @PrimaryKey
  Integer authRequestId;
  String authorizationCode;
  Date dateIns;
  OptionDto<Integer> operator;
  String operatorLogin;
  String tokenId;
  Date tokenDateIns;
  String redirectUri;
  OptionDto<String> client;
  Boolean isBlocked;

  public Integer getAuthRequestId() {
    return authRequestId;
  }

  public void setAuthRequestId(Integer authRequestId) {
    this.authRequestId = authRequestId;
  }

  public String getAuthorizationCode() {
    return authorizationCode;
  }

  public void setAuthorizationCode(String authorizationCode) {
    this.authorizationCode = authorizationCode;
  }

  public Date getDateIns() {
    return dateIns;
  }

  public void setDateIns(Date dateIns) {
    this.dateIns = dateIns;
  }

  public OptionDto<Integer> getOperator() {
    return operator;
  }

  public void setOperator(OptionDto<Integer> operator) {
    this.operator = operator;
  }

  public String getOperatorLogin() {
    return operatorLogin;
  }

  public void setOperatorLogin(String operatorLogin) {
    this.operatorLogin = operatorLogin;
  }

  public String getTokenId() {
    return tokenId;
  }

  public void setTokenId(String tokenId) {
    this.tokenId = tokenId;
  }

  public Date getTokenDateIns() {
    return tokenDateIns;
  }

  public void setTokenDateIns(Date tokenDateIns) {
    this.tokenDateIns = tokenDateIns;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public OptionDto<String> getClient() {
    return client;
  }

  public void setClient(OptionDto<String> client) {
    this.client = client;
  }

  public Boolean getBlocked() {
    return isBlocked;
  }

  public void setBlocked(Boolean blocked) {
    isBlocked = blocked;
  }
}
