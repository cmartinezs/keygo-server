package io.cmartinezs.keygo.app.membership.command;

import io.cmartinezs.keygo.app.membership.exception.InvalidCommandFieldException;
import java.util.List;
import java.util.UUID;

/**
 * Command to assign one or more platform roles to a user.
 * <p>Comando para asignar uno o más roles de plataforma a un usuario.
 * @author cmartinezs
 * @version 1.0
 */
public record AssignPlatformRoleCommand(
    UUID platformUserId,
    List<String> roleCodes
) {

  public AssignPlatformRoleCommand {
    if (platformUserId == null) throw new InvalidCommandFieldException("platformUserId");
    if (roleCodes == null || roleCodes.isEmpty()) throw new InvalidCommandFieldException("roleCodes");
  }
}
