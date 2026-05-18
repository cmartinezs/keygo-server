package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.UserStatusActionResult;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;

/**
 * Suspends a user account within a tenant.
 * Idempotent: if already SUSPENDED, returns result with idempotent=true without persisting.
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

  public UserStatusActionResult execute(String tenantSlug, String userId) {
    Tenant tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));

    User user = userRepositoryPort.findByIdAndTenantId(UserId.of(userId), tenant.getId())
        .orElseThrow(() -> new UserNotFoundException("id", userId));

    UserStatus previousStatus = user.getStatus();

    if (user.isSuspended()) {
      return new UserStatusActionResult(user.getId().value(), previousStatus, previousStatus, true);
    }

    user.suspend();
    User saved = userRepositoryPort.save(user);
    return new UserStatusActionResult(saved.getId().value(), previousStatus, saved.getStatus(), false);
  }
}
