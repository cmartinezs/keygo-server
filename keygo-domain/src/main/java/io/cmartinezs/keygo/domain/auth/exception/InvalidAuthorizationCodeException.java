package io.cmartinezs.keygo.domain.auth.exception;

/**
 * Excepción lanzada cuando un código de autorización no existe.
 */
public class InvalidAuthorizationCodeException extends RuntimeException {
  public InvalidAuthorizationCodeException(String message) {
    super(message);
  }

  public InvalidAuthorizationCodeException(String message, Throwable cause) {
    super(message, cause);
  }
}

