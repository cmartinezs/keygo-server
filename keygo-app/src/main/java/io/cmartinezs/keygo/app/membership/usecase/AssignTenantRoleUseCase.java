package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.membership.command.AssignTenantRoleCommand;
import io.cmartinezs.keygo.app.membership.exception.TenantRoleNotFoundException;
import io.cmartinezs.keygo.app.membership.port.TenantRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.TenantUserRoleRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.TenantUserRole;

/**
 * Use case: assign a tenant role to a tenant user.
 * <p>Caso de uso: asignar un rol de tenant a un usuario de tenant.
 * Validates that the role exists and is active before assigning.
 * Idempotent: if already assigned (active), existing assignment is returned.
 * @author cmartinezs
 * @version 1.0
 */
public class AssignTenantRoleUseCase {

  private final TenantRoleRepositoryPort tenantRoleRepositoryPort;
  private final TenantUserRoleRepositoryPort tenantUserRoleRepositoryPort;

  public AssignTenantRoleUseCase(
      TenantRoleRepositoryPort tenantRoleRepositoryPort,
      TenantUserRoleRepositoryPort tenantUserRoleRepositoryPort) {
    this.tenantRoleRepositoryPort = tenantRoleRepositoryPort;
    this.tenantUserRoleRepositoryPort = tenantUserRoleRepositoryPort;
  }

  /**
   * Assign the specified tenant role to the given tenant user.
   * @param command assignment input (tenantUserId + tenantRoleId)
   * @return the created TenantUserRole assignment
   * @throws TenantRoleNotFoundException if the role does not exist or is inactive
   */
  public TenantUserRole execute(AssignTenantRoleCommand command) {
    tenantRoleRepositoryPort.findByTenantId(command.tenantUserId())
        .stream()
        .filter(r -> r.getId().value().equals(command.tenantRoleId()) && r.isActive())
        .findFirst()
        .orElseGet(() -> tenantRoleRepositoryPort.findByTenantId(command.tenantUserId())
            .stream()
            .filter(r -> r.getId().value().equals(command.tenantRoleId()))
            .findFirst()
            .orElseThrow(() -> new TenantRoleNotFoundException(command.tenantRoleId())));

    if (tenantUserRoleRepositoryPort.hasActiveRole(command.tenantUserId(), command.tenantRoleId())) {
      return tenantUserRoleRepositoryPort.findActiveByTenantUserId(command.tenantUserId()).stream()
          .filter(r -> r.getTenantRoleId().value().equals(command.tenantRoleId()))
          .findFirst()
          .orElseThrow(() -> new TenantRoleNotFoundException(command.tenantRoleId()));
    }

    return tenantUserRoleRepositoryPort.assign(command.tenantUserId(), command.tenantRoleId());
  }
}
