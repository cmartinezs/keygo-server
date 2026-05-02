package io.cmartinezs.keygo.app.membership.command;

import io.cmartinezs.keygo.app.membership.exception.InvalidCommandFieldException;
import java.util.UUID;

/**
 * Command to create a tenant-scoped role.
 * <p>Comando para crear un rol a nivel de tenant.
 * @author cmartinezs
 * @version 1.0
 */
public record CreateTenantRoleCommand(
    UUID tenantId,
    String code,
    String name,
    String description
) {

  public CreateTenantRoleCommand {
    if (tenantId == null) throw new InvalidCommandFieldException("tenantId");
    if (code == null || code.isBlank()) throw new InvalidCommandFieldException("code");
    if (name == null || name.isBlank()) throw new InvalidCommandFieldException("name");
  }
}
