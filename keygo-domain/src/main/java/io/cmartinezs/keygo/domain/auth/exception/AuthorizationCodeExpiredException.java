package io.cmartinezs.keygo.domain.auth.exception;

/**
 * Excepción lanzada cuando un código de autorización ha expirado.
 */
public class AuthorizationCodeExpiredException extends RuntimeException {
  public AuthorizationCodeExpiredException(String message) {
    super(message);
  }

  public AuthorizationCodeExpiredException(String message, Throwable cause) {
    super(message, cause);
  }
}

