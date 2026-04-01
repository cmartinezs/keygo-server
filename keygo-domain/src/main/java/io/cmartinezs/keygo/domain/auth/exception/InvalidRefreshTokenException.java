package io.cmartinezs.keygo.domain.auth.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when a refresh token is invalid, already used, revoked, or does not belong to the client.
 */
public class InvalidRefreshTokenException extends DomainException {

  public InvalidRefreshTokenException(String reason) {
    super("Invalid refresh token: %s".formatted(reason));
  }

  public InvalidRefreshTokenException(String reason, Throwable cause) {
    super("Invalid refresh token: %s".formatted(reason), cause);
  }
}
