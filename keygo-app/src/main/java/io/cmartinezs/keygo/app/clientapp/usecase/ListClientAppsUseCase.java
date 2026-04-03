package io.cmartinezs.keygo.app.clientapp.usecase;

import io.cmartinezs.keygo.app.clientapp.filter.ClientAppFilter;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;

import java.util.List;

/**
 * Use case: list client applications with pagination, filtering, and sorting.
 * <p>Caso de uso: listar aplicaciones cliente con paginación, filtrado y ordenamiento.
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
   * Execute the use case with filtering, sorting, and pagination.
   * @param tenantSlug the tenant slug
   * @param filter filter criteria with pagination and sorting
   * @return paginated result of client apps
   * @throws TenantNotFoundException if the tenant does not exist
   */
  public PagedResult<ClientApp> execute(String tenantSlug, ClientAppFilter filter) {
    Tenant tenant = tenantRepositoryPort
        .findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));

    return clientAppRepositoryPort.findAllPaged(tenant.getId(), filter);
  }
}

