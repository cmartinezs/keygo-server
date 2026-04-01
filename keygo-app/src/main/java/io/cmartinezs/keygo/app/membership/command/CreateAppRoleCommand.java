package io.cmartinezs.keygo.app.membership.command;

import io.cmartinezs.keygo.app.membership.exception.InvalidCommandFieldException;
import java.util.UUID;

/**
 * Command to create an app-scoped role.
 * <p>Comando para crear un rol a nivel de app.
 * @author cmartinezs
 * @version 1.0
 */
public record CreateAppRoleCommand(
    String tenantSlug,
    UUID clientAppId,
    String code,
    String displayName,
    String description
) {

  public CreateAppRoleCommand {
    if (tenantSlug == null || tenantSlug.isBlank()) {
      throw new InvalidCommandFieldException("tenantSlug");
    }
    if (clientAppId == null) {
      throw new InvalidCommandFieldException("clientAppId");
    }
    if (code == null || code.isBlank()) {
      throw new InvalidCommandFieldException("code");
    }
  }
}

