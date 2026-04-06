package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.membership.command.AssignPlatformRoleCommand;
import io.cmartinezs.keygo.app.membership.exception.PlatformRoleNotFoundException;
import io.cmartinezs.keygo.app.membership.port.PlatformRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.PlatformUserRoleRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.PlatformUserRole;

/**
 * Use case: assign a platform role to a user.
 * <p>Caso de uso: asignar un rol de plataforma a un usuario.
 * Validates that the role exists before assigning it.
 * Idempotent: if already assigned, existing assignment is returned.
 * @author cmartinezs
 * @version 1.0
 */
public class AssignPlatformRoleUseCase {

  private final PlatformRoleRepositoryPort platformRoleRepositoryPort;
  private final PlatformUserRoleRepositoryPort platformUserRoleRepositoryPort;

  public AssignPlatformRoleUseCase(
      PlatformRoleRepositoryPort platformRoleRepositoryPort,
      PlatformUserRoleRepositoryPort platformUserRoleRepositoryPort) {
    this.platformRoleRepositoryPort = platformRoleRepositoryPort;
    this.platformUserRoleRepositoryPort = platformUserRoleRepositoryPort;
  }

  /**
   * Assign the specified platform role to the given user.
   * @param command assignment input (tenantUserId + roleCode)
   * @return the created or existing PlatformUserRole assignment
   */
  public PlatformUserRole execute(AssignPlatformRoleCommand command) {
    if (!platformRoleRepositoryPort.existsByCode(command.roleCode())) {
      throw new PlatformRoleNotFoundException(command.roleCode());
    }

    if (platformUserRoleRepositoryPort.hasRole(command.tenantUserId(), command.roleCode())) {
      return platformUserRoleRepositoryPort.findByPlatformUserId(command.tenantUserId()).stream()
          .filter(r -> r.getPlatformRoleId() != null)
          .findFirst()
          .orElseThrow(() -> new PlatformRoleNotFoundException(command.roleCode()));
    }

    return platformUserRoleRepositoryPort.assign(command.tenantUserId(), command.roleCode());
  }
}
