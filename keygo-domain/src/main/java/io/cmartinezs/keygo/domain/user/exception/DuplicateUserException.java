package io.cmartinezs.keygo.domain.user.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when a user with the same email or username already exists within a tenant.
 * <p>Se lanza cuando ya existe un usuario con el mismo email o username dentro de un tenant.
 * @author cmartinezs
 * @version 1.0
 */
public class DuplicateUserException extends DomainException {

  public DuplicateUserException(String field, String value) {
    super("User already exists with %s: %s".formatted(field, value));
  }
}

