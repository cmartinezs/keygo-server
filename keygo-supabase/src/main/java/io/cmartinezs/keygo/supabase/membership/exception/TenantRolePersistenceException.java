package io.cmartinezs.keygo.supabase.membership.exception;

import io.cmartinezs.keygo.app.shared.exception.PortException;

/**
 * Thrown by the tenant role persistence adapter when a required record
 * cannot be found or an invariant is violated during a write operation.
 */
public class TenantRolePersistenceException extends PortException {

  public TenantRolePersistenceException(String message) {
    super(message);
  }
}
