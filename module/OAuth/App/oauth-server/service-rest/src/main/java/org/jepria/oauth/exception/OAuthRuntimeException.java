package org.jepria.oauth.exception;

public class OAuthRuntimeException extends RuntimeException {

  static final long serialVersionUID = -7034823190735666639L;
  final String code;

  public OAuthRuntimeException(String code) {
    super();
    this.code = code;
  }

  public OAuthRuntimeException(String code, String message) {
    super(message);
    this.code = code;
  }


  public OAuthRuntimeException(String code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

  public OAuthRuntimeException(String code, Throwable cause) {
    super(cause);
    this.code = code;
  }

  public String getExceptionCode() {
    return this.code;
  }
}
