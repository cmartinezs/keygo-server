package io.cmartinezs.keygo.app.clientapp.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;

import java.util.List;

/**
 * Use case: list all client applications belonging to a tenant.
 * <p>Caso de uso: listar todas las aplicaciones cliente de un tenant.
 * @author cmartinezs
 * @version 1.0
 */
public class ListClientAppsUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final ClientAppRepositoryPort clientAppRepositoryPort;

  public ListClientAppsUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.clientAppRepositoryPort = clientAppRepositoryPort;
  }

  /**
   * Execute the use case.
   * @param tenantSlug the tenant slug
   * @return list of client apps for the tenant
   * @throws TenantNotFoundException if the tenant does not exist
   */
  public List<ClientApp> execute(String tenantSlug) {
    Tenant tenant = tenantRepositoryPort
        .findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));

    return clientAppRepositoryPort.findAllByTenantId(tenant.getId());
  }
}

