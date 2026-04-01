package io.cmartinezs.keygo.domain.user.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when a user attempts to login but their email has not been verified yet.
 * <p>Se lanza cuando un usuario intenta hacer login pero su email aún no ha sido verificado.
 * @author cmartinezs
 * @version 1.0
 */
public class UserPendingVerificationException extends DomainException {

  public UserPendingVerificationException(String email) {
    super("User pending email verification: %s".formatted(email));
  }
}

