package org.jepria.oauth.session.dto;

import javax.ws.rs.QueryParam;
import java.io.Serializable;

public class SessionSearchDto implements Serializable {
  
  @QueryParam("sessionId")
  String sessionId;
  @QueryParam("authorizationCode")
  String authorizationCode;
  @QueryParam("operatorId")
  Integer operatorId;
  @QueryParam("clientId")
  String clientId;
  @QueryParam("sessionTokenId")
  String sessionTokenId;
  @QueryParam("accessTokenId")
  String accessTokenId;
  @QueryParam("refreshTokenId")
  String refreshTokenId;
  @QueryParam("redirectUri")
  String redirectUri;
  @QueryParam("maxRowCount")
  Integer maxRowCount;

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getAuthorizationCode() {
    return authorizationCode;
  }

  public void setAuthorizationCode(String authorizationCode) {
    this.authorizationCode = authorizationCode;
  }

  public Integer getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(Integer operatorId) {
    this.operatorId = operatorId;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getSessionTokenId() {
    return sessionTokenId;
  }

  public void setSessionTokenId(String sessionTokenId) {
    this.sessionTokenId = sessionTokenId;
  }

  public String getAccessTokenId() {
    return accessTokenId;
  }

  public void setAccessTokenId(String accessTokenId) {
    this.accessTokenId = accessTokenId;
  }

  public String getRefreshTokenId() {
    return refreshTokenId;
  }

  public void setRefreshTokenId(String refreshTokenId) {
    this.refreshTokenId = refreshTokenId;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public Integer getMaxRowCount() {
    return maxRowCount;
  }

  public void setMaxRowCount(Integer maxRowCount) {
    this.maxRowCount = maxRowCount;
  }
}