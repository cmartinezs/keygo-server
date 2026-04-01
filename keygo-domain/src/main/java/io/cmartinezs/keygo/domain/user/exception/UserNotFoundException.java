package io.cmartinezs.keygo.domain.user.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when a user cannot be found within a tenant.
 */
public class UserNotFoundException extends DomainException {

  public UserNotFoundException(String field, String value) {
    super("User not found by %s: %s".formatted(field, value));
  }
}
