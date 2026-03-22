package io.cmartinezs.keygo.domain.user.exception;

/**
 * Thrown when a user with the same email or username already exists within a tenant.
 * <p>Se lanza cuando ya existe un usuario con el mismo email o username dentro de un tenant.
 * @author cmartinezs
 * @version 1.0
 */
public class DuplicateUserException extends RuntimeException {

  public DuplicateUserException(String field, String value) {
    super("User already exists with " + field + ": " + value);
  }
}

