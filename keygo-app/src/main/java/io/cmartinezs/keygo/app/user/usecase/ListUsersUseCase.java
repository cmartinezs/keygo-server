package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.model.User;

import java.util.List;

/**
 * Use case: list all users belonging to a tenant.
 * <p>Caso de uso: listar todos los usuarios de un tenant.
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
   * Execute the use case.
   * @param tenantSlug the tenant slug
   * @return list of users (may be empty)
   * @throws TenantNotFoundException if the tenant does not exist
   */
  public List<User> execute(String tenantSlug) {
    Tenant tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));

    return userRepositoryPort.findAllByTenantId(tenant.getId());
  }
}

