package io.cmartinezs.keygo.domain.user.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when an operation is attempted on a suspended user account.
 * <p>Se lanza cuando se intenta una operación sobre una cuenta de usuario suspendida.
 * @author cmartinezs
 * @version 1.0
 */
public class UserSuspendedException extends DomainException {

  public UserSuspendedException(String username) {
    super("User account is suspended: %s".formatted(username));
  }
}

