package io.cmartinezs.keygo.domain.auth.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when an authorization code has expired.
 */
public class AuthorizationCodeExpiredException extends DomainException {

  public AuthorizationCodeExpiredException() {
    super("Authorization code has expired");
  }
}
