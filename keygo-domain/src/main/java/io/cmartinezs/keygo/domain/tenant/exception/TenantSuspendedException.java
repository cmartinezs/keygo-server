package io.cmartinezs.keygo.domain.tenant.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when an operation is attempted on a suspended tenant.
 */
public class TenantSuspendedException extends DomainException {

  public TenantSuspendedException(String slug) {
    super("Tenant is suspended: %s".formatted(slug));
  }
}
