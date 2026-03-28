package io.cmartinezs.keygo.app.tenant.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;

/**
 * Use case: reactivate a previously suspended tenant.
 * <p>Caso de uso: reactivar un tenant previamente suspendido.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class ActivateTenantUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;

  public ActivateTenantUseCase(TenantRepositoryPort tenantRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
  }

  /**
   * Executes the use case.
   *
   * @param slug the tenant slug to activate
   * @return the activated Tenant
   * @throws TenantNotFoundException if no tenant exists with the given slug
   */
  public Tenant execute(String slug) {
    Tenant tenant = tenantRepositoryPort
        .findBySlug(TenantSlug.of(slug))
        .orElseThrow(() -> new TenantNotFoundException(slug));

    tenant.activate();

    return tenantRepositoryPort.save(tenant);
  }
}

