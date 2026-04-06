package io.cmartinezs.keygo.app.membership.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;
import java.util.UUID;

/**
 * Thrown when a TenantRole is not found.
 */
public class TenantRoleNotFoundException extends UseCaseException {

  public TenantRoleNotFoundException(UUID tenantRoleId) {
    super("TenantRole not found: " + tenantRoleId);
  }
}
