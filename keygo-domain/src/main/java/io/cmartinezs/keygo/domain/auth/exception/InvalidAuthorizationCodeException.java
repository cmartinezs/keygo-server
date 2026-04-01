package io.cmartinezs.keygo.domain.auth.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when an authorization code is invalid, already used, or does not exist.
 */
public class InvalidAuthorizationCodeException extends DomainException {

  public InvalidAuthorizationCodeException(String reason) {
    super("Authorization code is invalid: %s".formatted(reason));
  }

  public InvalidAuthorizationCodeException(String reason, Throwable cause) {
    super("Authorization code is invalid: %s".formatted(reason), cause);
  }
}
