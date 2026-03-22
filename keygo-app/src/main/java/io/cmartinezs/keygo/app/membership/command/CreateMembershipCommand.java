package io.cmartinezs.keygo.app.membership.command;

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
      throw new IllegalArgumentException("tenantSlug cannot be null or blank");
    }
    if (userId == null) {
      throw new IllegalArgumentException("userId cannot be null");
    }
    if (clientAppId == null) {
      throw new IllegalArgumentException("clientAppId cannot be null");
    }
    if (roleCodes == null) {
      throw new IllegalArgumentException("roleCodes cannot be null");
    }
  }
}

