package io.cmartinezs.keygo.domain.tenant.exception;

/**
 * Thrown when a tenant cannot be found by its slug.
 * <p>Se lanza cuando no se puede encontrar un tenant por su slug.
 * @author cmartinezs
 * @version 1.0
 */
public class TenantNotFoundException extends RuntimeException {

  public TenantNotFoundException(String slug) {
    super("Tenant not found with slug: " + slug);
  }
}

