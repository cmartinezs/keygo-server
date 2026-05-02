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
 * Suspends a user account within a tenant.
 * Requires tenant to exist and user to be in ACTIVE status.
 * If user is already SUSPENDED, throws UserSuspendedException.
 */
public class SuspendUserUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final UserRepositoryPort userRepositoryPort;

  public SuspendUserUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.userRepositoryPort = userRepositoryPort;
  }

  public User execute(String tenantSlug, String userId) {
    // Resolve tenant
    Tenant tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));

    // Resolve user
    User user = userRepositoryPort.findByIdAndTenantId(UserId.of(userId), tenant.getId())
        .orElseThrow(() -> new UserNotFoundException("id", userId));

    // Suspend (throws UserSuspendedException if already SUSPENDED)
    user.suspend();

    // Persist
    return userRepositoryPort.save(user);
  }
}
