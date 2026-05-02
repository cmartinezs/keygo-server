package io.cmartinezs.keygo.app.tenant.usecase;

import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.tenant.filter.TenantFilter;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;

/**
 * Use case: list all tenants with optional filtering and pagination.
 * <p>Caso de uso: listar todos los tenants con filtrado opcional y paginación.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class ListTenantsUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;

  public ListTenantsUseCase(TenantRepositoryPort tenantRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
  }

  /**
   * Execute the use case.
   *
   * @param filter filter and pagination criteria / criterios de filtro y paginación
   * @return paginated list of tenants matching the criteria / lista paginada de tenants que coinciden
   */
  public PagedResult<Tenant> execute(TenantFilter filter) {
    return tenantRepositoryPort.findAll(filter);
  }
}

