package io.cmartinezs.keygo.domain.auth.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when a refresh token exists but its expiration time has passed.
 */
public class RefreshTokenExpiredException extends DomainException {

  public RefreshTokenExpiredException() {
    super("Refresh token has expired");
  }
}
