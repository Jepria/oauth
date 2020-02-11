package org.jepria.oauth.main.exception;

public class HandledRuntimeException extends RuntimeException {

  static final long serialVersionUID = -7034823190735666639L;
  final String code;

  public HandledRuntimeException(String code) {
    super();
    this.code = code;
  }

  public HandledRuntimeException(String code, String message) {
    super(message);
    this.code = code;
  }


  public HandledRuntimeException(String code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

  public HandledRuntimeException(String code, Throwable cause) {
    super(cause);
    this.code = code;
  }

  public String getExceptionCode() {
    return this.code;
  }
}
