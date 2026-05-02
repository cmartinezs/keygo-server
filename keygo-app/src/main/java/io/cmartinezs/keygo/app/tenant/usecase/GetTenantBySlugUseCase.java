package io.cmartinezs.keygo.app.tenant.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;

/**
 * Use case: retrieve a tenant by its slug.
 * <p>Caso de uso: obtener un tenant por su slug.
 * @author cmartinezs
 * @version 1.0
 */
public class GetTenantBySlugUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;

  public GetTenantBySlugUseCase(TenantRepositoryPort tenantRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
  }

  /**
   * Execute the use case.
   * @param slug the tenant slug
   * @return the found Tenant
   * @throws TenantNotFoundException if no tenant exists with the given slug
   */
  public Tenant execute(String slug) {
    return tenantRepositoryPort
        .findBySlug(TenantSlug.of(slug))
        .orElseThrow(() -> new TenantNotFoundException(slug));
  }
}

