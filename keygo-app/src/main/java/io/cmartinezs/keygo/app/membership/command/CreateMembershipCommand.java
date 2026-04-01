package io.cmartinezs.keygo.app.membership.command;

import io.cmartinezs.keygo.app.membership.exception.InvalidCommandFieldException;
import java.util.Set;
import java.util.UUID;

/**
 * Command to create a new membership (user access to app).
 * <p>Comando para crear una nueva membresía (acceso de usuario a app).
 * @author cmartinezs
 * @version 1.0
 */
public record CreateMembershipCommand(
    String tenantSlug,
    UUID userId,
    UUID clientAppId,
    Set<String> roleCodes
) {

  public CreateMembershipCommand {
    if (tenantSlug == null || tenantSlug.isBlank()) {
      throw new InvalidCommandFieldException("tenantSlug");
    }
    if (userId == null) {
      throw new InvalidCommandFieldException("userId");
    }
    if (clientAppId == null) {
      throw new InvalidCommandFieldException("clientAppId");
    }
    if (roleCodes == null) {
      throw new InvalidCommandFieldException("roleCodes");
    }
  }
}

