package org.jepria.oauth.main.dto;

public class ErrorDto {
  String errorId;
  String error;
  String errorDescription;
  String errorUri;

  public String getErrorId() {
    return errorId;
  }

  public void setErrorId(String errorId) {
    this.errorId = errorId;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getErrorDescription() {
    return errorDescription;
  }

  public void setErrorDescription(String errorDescription) {
    this.errorDescription = errorDescription;
  }

  public String getErrorUri() {
    return errorUri;
  }

  public void setErrorUri(String errorUri) {
    this.errorUri = errorUri;
  }
}