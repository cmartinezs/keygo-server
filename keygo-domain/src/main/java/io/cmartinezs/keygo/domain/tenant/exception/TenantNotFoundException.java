package io.cmartinezs.keygo.domain.tenant.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when a tenant cannot be found by its slug.
 */
public class TenantNotFoundException extends DomainException {

  public TenantNotFoundException(String slug) {
    super("Tenant not found by slug: %s".formatted(slug));
  }
}
