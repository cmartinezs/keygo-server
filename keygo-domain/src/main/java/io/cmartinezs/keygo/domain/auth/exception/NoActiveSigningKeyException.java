package io.cmartinezs.keygo.domain.auth.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when no active signing key is available to issue JWT tokens.
 */
public class NoActiveSigningKeyException extends DomainException {

  public NoActiveSigningKeyException() {
    super("No active signing key available");
  }
}
