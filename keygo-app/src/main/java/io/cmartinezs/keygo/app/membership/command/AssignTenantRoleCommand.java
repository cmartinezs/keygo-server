package io.cmartinezs.keygo.app.membership.command;

import io.cmartinezs.keygo.app.membership.exception.InvalidCommandFieldException;
import java.util.UUID;

/**
 * Command to assign a tenant role to a tenant user.
 * <p>Comando para asignar un rol de tenant a un usuario de tenant.
 * @author cmartinezs
 * @version 1.0
 */
public record AssignTenantRoleCommand(
    UUID tenantUserId,
    UUID tenantRoleId
) {

  public AssignTenantRoleCommand {
    if (tenantUserId == null) throw new InvalidCommandFieldException("tenantUserId");
    if (tenantRoleId == null) throw new InvalidCommandFieldException("tenantRoleId");
  }
}
