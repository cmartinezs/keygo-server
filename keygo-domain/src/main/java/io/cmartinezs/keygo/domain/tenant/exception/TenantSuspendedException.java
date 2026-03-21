package io.cmartinezs.keygo.domain.tenant.exception;

/**
 * Thrown when an operation is attempted on a suspended tenant.
 * <p>Se lanza cuando se intenta una operación sobre un tenant suspendido.
 * @author cmartinezs
 * @version 1.0
 */
public class TenantSuspendedException extends RuntimeException {

  public TenantSuspendedException(String slug) {
    super("Tenant is suspended and cannot process requests: " + slug);
  }
}

