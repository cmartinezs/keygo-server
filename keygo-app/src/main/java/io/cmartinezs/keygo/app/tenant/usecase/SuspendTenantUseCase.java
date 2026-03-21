package io.cmartinezs.keygo.app.tenant.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;

/**
 * Use case: suspend a tenant.
 * A suspended tenant cannot process authentication requests.
 * <p>Caso de uso: suspender un tenant.
 * Un tenant suspendido no puede procesar solicitudes de autenticación.
 * @author cmartinezs
 * @version 1.0
 */
public class SuspendTenantUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;

  public SuspendTenantUseCase(TenantRepositoryPort tenantRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
  }

  /**
   * Execute the use case.
   * @param slug the tenant slug to suspend
   * @return the suspended Tenant
   * @throws TenantNotFoundException if no tenant exists with the given slug
   * @throws IllegalStateException if the tenant is already suspended
   */
  public Tenant execute(String slug) {
    Tenant tenant = tenantRepositoryPort
        .findBySlug(TenantSlug.of(slug))
        .orElseThrow(() -> new TenantNotFoundException(slug));

    tenant.suspend();

    return tenantRepositoryPort.save(tenant);
  }
}

