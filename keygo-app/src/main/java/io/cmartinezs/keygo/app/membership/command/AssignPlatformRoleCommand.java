package io.cmartinezs.keygo.app.membership.command;

import io.cmartinezs.keygo.app.membership.exception.InvalidCommandFieldException;
import java.util.UUID;

/**
 * Command to assign a platform role to a user.
 * <p>Comando para asignar un rol de plataforma a un usuario.
 * @author cmartinezs
 * @version 1.0
 */
public record AssignPlatformRoleCommand(
    UUID tenantUserId,
    String roleCode
) {

  public AssignPlatformRoleCommand {
    if (tenantUserId == null) throw new InvalidCommandFieldException("tenantUserId");
    if (roleCode == null || roleCode.isBlank()) throw new InvalidCommandFieldException("roleCode");
  }
}
