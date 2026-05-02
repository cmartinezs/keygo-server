package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.membership.exception.PlatformRoleNotFoundException;
import io.cmartinezs.keygo.app.membership.port.PlatformRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.PlatformUserRoleRepositoryPort;
import java.util.UUID;

/**
 * Use case: revoke a platform role from a user.
 * <p>Caso de uso: revocar un rol de plataforma de un usuario.
 * Validates that the role exists before attempting revocation.
 * Idempotent: if already not assigned, no action is taken.
 * @author cmartinezs
 * @version 1.0
 */
public class RevokePlatformRoleUseCase {

  private final PlatformRoleRepositoryPort platformRoleRepositoryPort;
  private final PlatformUserRoleRepositoryPort platformUserRoleRepositoryPort;

  public RevokePlatformRoleUseCase(
      PlatformRoleRepositoryPort platformRoleRepositoryPort,
      PlatformUserRoleRepositoryPort platformUserRoleRepositoryPort) {
    this.platformRoleRepositoryPort = platformRoleRepositoryPort;
    this.platformUserRoleRepositoryPort = platformUserRoleRepositoryPort;
  }

  /**
   * Revoke the specified platform role from the given user.
   * @param tenantUserId the user whose role is being revoked
   * @param roleCode     the code of the platform role to revoke
   */
  public void execute(UUID tenantUserId, String roleCode) {
    if (!platformRoleRepositoryPort.existsByCode(roleCode)) {
      throw new PlatformRoleNotFoundException(roleCode);
    }
    platformUserRoleRepositoryPort.revoke(tenantUserId, roleCode);
  }
}
