package io.cmartinezs.keygo.app.membership.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;
import java.util.UUID;

/**
 * Thrown when a TenantRole already exists with the same code in the same tenant.
 */
public class DuplicateTenantRoleException extends UseCaseException {

  public DuplicateTenantRoleException(String code, UUID tenantId) {
    super("TenantRole '%s' already exists in tenant: %s".formatted(code, tenantId));
  }
}
