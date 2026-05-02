package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.membership.exception.TenantRoleNotFoundException;
import io.cmartinezs.keygo.app.membership.port.TenantRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.TenantUserRoleRepositoryPort;
import java.util.UUID;

/**
 * Use case: revoke a tenant role from a tenant user.
 * <p>Caso de uso: revocar un rol de tenant de un usuario de tenant.
 * Revocation deletes the join row because {@code tenant_user_roles} is a pure link table.
 * Idempotent: if not currently assigned, no action is taken.
 * @author cmartinezs
 * @version 1.0
 */
public class RevokeTenantRoleUseCase {

  private final TenantRoleRepositoryPort tenantRoleRepositoryPort;
  private final TenantUserRoleRepositoryPort tenantUserRoleRepositoryPort;

  public RevokeTenantRoleUseCase(
      TenantRoleRepositoryPort tenantRoleRepositoryPort,
      TenantUserRoleRepositoryPort tenantUserRoleRepositoryPort) {
    this.tenantRoleRepositoryPort = tenantRoleRepositoryPort;
    this.tenantUserRoleRepositoryPort = tenantUserRoleRepositoryPort;
  }

  /**
   * Revoke the specified tenant role from the given tenant user.
   * @param tenantUserId the user whose role is being revoked
   * @param tenantRoleId the ID of the tenant role to revoke
   * @throws TenantRoleNotFoundException if the role does not exist
   */
  public void execute(UUID tenantUserId, UUID tenantRoleId) {
    boolean roleExists = tenantRoleRepositoryPort.findByTenantId(tenantUserId).stream()
        .anyMatch(r -> r.getId().value().equals(tenantRoleId));
    if (!roleExists && tenantRoleRepositoryPort.findByTenantId(tenantUserId).isEmpty()) {
      throw new TenantRoleNotFoundException(tenantRoleId);
    }
    tenantUserRoleRepositoryPort.revoke(tenantUserId, tenantRoleId);
  }
}
