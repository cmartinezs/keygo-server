package io.cmartinezs.keygo.domain.user.exception;

/**
 * Thrown when a user cannot be found within a tenant.
 * <p>Se lanza cuando no se puede encontrar un usuario dentro de un tenant.
 * @author cmartinezs
 * @version 1.0
 */
public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(String identifier) {
    super("User not found: " + identifier);
  }
}

