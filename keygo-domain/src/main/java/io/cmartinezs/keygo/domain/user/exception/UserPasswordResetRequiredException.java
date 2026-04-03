package io.cmartinezs.keygo.domain.user.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when a login attempt is made by a user whose account requires a password reset.
 * <p>Se lanza cuando un usuario con {@code status=RESET_PASSWORD} intenta autenticarse.
 * The user must complete the password reset flow before they can log in.
 * @author cmartinezs
 * @version 1.0
 */
public class UserPasswordResetRequiredException extends DomainException {

  public UserPasswordResetRequiredException(String username) {
    super("User account requires password reset: %s".formatted(username));
  }
}
