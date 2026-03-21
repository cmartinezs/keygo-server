package io.cmartinezs.keygo.domain.tenant.model;

/**
 * Status of a tenant in the system.
 * <p>Estado de un tenant en el sistema.
 * @author cmartinezs
 * @version 1.0
 */
public enum TenantStatus {

  /* Tenant is active and operational.
   * El tenant está activo y operacional. */
  ACTIVE,

  /* Tenant is suspended and cannot perform operations.
   * El tenant está suspendido y no puede realizar operaciones. */
  SUSPENDED,

  /* Tenant is pending activation (e.g. awaiting email verification).
   * El tenant está pendiente de activación (ej. esperando verificación de email). */
  PENDING
}

