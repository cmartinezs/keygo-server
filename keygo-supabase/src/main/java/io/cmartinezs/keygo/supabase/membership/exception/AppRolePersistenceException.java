package io.cmartinezs.keygo.supabase.membership.exception;

import io.cmartinezs.keygo.app.shared.exception.PortException;
import java.util.UUID;

/**
 * Thrown by the membership persistence adapter when an AppRole record
 * expected to exist in the database cannot be found during a write operation.
 */
public class AppRolePersistenceException extends PortException {

  public AppRolePersistenceException(UUID roleId) {
    super("AppRole not found in persistence for update: %s".formatted(roleId));
  }
}
