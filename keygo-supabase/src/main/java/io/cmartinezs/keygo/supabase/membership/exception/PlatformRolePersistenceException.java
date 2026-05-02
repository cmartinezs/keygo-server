package io.cmartinezs.keygo.supabase.membership.exception;

import io.cmartinezs.keygo.app.shared.exception.PortException;

/**
 * Thrown by the platform role persistence adapter when a required record
 * cannot be found or an invariant is violated during a write operation.
 */
public class PlatformRolePersistenceException extends PortException {

  public PlatformRolePersistenceException(String message) {
    super(message);
  }
}
