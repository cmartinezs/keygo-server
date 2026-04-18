package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.membership.command.AssignPlatformRoleCommand;
import io.cmartinezs.keygo.app.membership.exception.PlatformRoleNotFoundException;
import io.cmartinezs.keygo.app.membership.port.PlatformRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.PlatformUserRoleRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.PlatformUserRole;
import java.util.ArrayList;
import java.util.List;

/**
 * Use case: assign one or more platform roles to a user.
 * <p>Caso de uso: asignar uno o más roles de plataforma a un usuario.
 * Validates that every role exists before assigning any. Idempotent: already-assigned roles are skipped.
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
   * Assign all specified platform roles to the given user.
   * @param command assignment input (platformUserId + roleCodes)
   * @return list of newly created PlatformUserRole assignments (already-assigned roles are skipped)
   */
  public List<PlatformUserRole> execute(AssignPlatformRoleCommand command) {
    for (String roleCode : command.roleCodes()) {
      if (!platformRoleRepositoryPort.existsByCode(roleCode)) {
        throw new PlatformRoleNotFoundException(roleCode);
      }
    }

    List<PlatformUserRole> assigned = new ArrayList<>();
    for (String roleCode : command.roleCodes()) {
      if (!platformUserRoleRepositoryPort.hasRole(command.platformUserId(), roleCode)) {
        assigned.add(platformUserRoleRepositoryPort.assign(command.platformUserId(), roleCode));
      }
    }
    return assigned;
  }
}
