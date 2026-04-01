package io.cmartinezs.keygo.domain.auth.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when PKCE code verifier does not match the challenge or is missing/unsupported.
 */
public class InvalidPkceVerificationException extends DomainException {

  public InvalidPkceVerificationException(String reason) {
    super("PKCE verification failed: %s".formatted(reason));
  }

  public InvalidPkceVerificationException(String reason, Throwable cause) {
    super("PKCE verification failed: %s".formatted(reason), cause);
  }
}
