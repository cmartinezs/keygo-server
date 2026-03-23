package io.cmartinezs.keygo.domain.auth.exception;

/**
 * Excepción lanzada cuando un refresh token es inválido, ya fue usado, está revocado,
 * o no pertenece al cliente que lo presenta.
 */
public class InvalidRefreshTokenException extends RuntimeException {
  public InvalidRefreshTokenException(String message) {
    super(message);
  }

  public InvalidRefreshTokenException(String message, Throwable cause) {
    super(message, cause);
  }
}

