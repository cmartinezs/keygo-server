package io.cmartinezs.keygo.domain.auth.exception;

/**
 * Excepción lanzada cuando un refresh token existe pero su tiempo de expiración ya pasó.
 */
public class RefreshTokenExpiredException extends RuntimeException {
  public RefreshTokenExpiredException(String message) {
    super(message);
  }

  public RefreshTokenExpiredException(String message, Throwable cause) {
    super(message, cause);
  }
}

