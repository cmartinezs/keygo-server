package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;

/**
 * Use case: retrieve a specific user by its UUID within a tenant.
 * <p>Caso de uso: obtener un usuario específico por su UUID dentro de un tenant.
 * @author cmartinezs
 * @version 1.0
 */
public class GetUserUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final UserRepositoryPort userRepositoryPort;

  public GetUserUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.userRepositoryPort = userRepositoryPort;
  }

  /**
   * Execute the use case.
   * @param tenantSlug the tenant slug
   * @param userId     the user UUID string
   * @return the found User
   * @throws TenantNotFoundException if the tenant does not exist
   * @throws UserNotFoundException   if the user does not exist within the tenant
   */
  public User execute(String tenantSlug, String userId) {
    Tenant tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));

    return userRepositoryPort.findByIdAndTenantId(UserId.of(userId), tenant.getId())
        .orElseThrow(() -> new UserNotFoundException(userId));
  }
}

