package io.cmartinezs.keygo.domain.auth.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when a requested scope has not been granted.
 */
public class ScopeNotGrantedException extends DomainException {

  public ScopeNotGrantedException(String scope) {
    super("Scope not granted: %s".formatted(scope));
  }
}
