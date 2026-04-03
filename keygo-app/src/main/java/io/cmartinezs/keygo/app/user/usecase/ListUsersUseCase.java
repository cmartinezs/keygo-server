package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.filter.UserFilter;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.model.User;

import java.util.List;

/**
 * Use case: list users belonging to a tenant with pagination, filtering, and sorting.
 * <p>Caso de uso: listar usuarios de un tenant con paginación, filtrado y ordenamiento.
 * @author cmartinezs
 * @version 1.0
 */
public class ListUsersUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final UserRepositoryPort userRepositoryPort;

  public ListUsersUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.userRepositoryPort = userRepositoryPort;
  }

  /**
   * Execute the use case with filtering, sorting, and pagination.
   * @param tenantSlug the tenant slug
   * @param filter filter criteria with pagination and sorting
   * @return paginated result of users (may be empty)
   * @throws TenantNotFoundException if the tenant does not exist
   */
  public PagedResult<User> execute(String tenantSlug, UserFilter filter) {
    Tenant tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));

    return userRepositoryPort.findAllPaged(tenant.getId(), filter);
  }
}

