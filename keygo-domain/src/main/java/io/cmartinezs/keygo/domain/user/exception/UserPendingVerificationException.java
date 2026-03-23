package io.cmartinezs.keygo.domain.user.exception;

/**
 * Thrown when a user attempts to login but their email has not been verified yet.
 * <p>Se lanza cuando un usuario intenta hacer login pero su email aún no ha sido verificado.
 * @author cmartinezs
 * @version 1.0
 */
public class UserPendingVerificationException extends RuntimeException {

  public UserPendingVerificationException(String email) {
    super("User account is pending email verification: " + email);
  }
}

