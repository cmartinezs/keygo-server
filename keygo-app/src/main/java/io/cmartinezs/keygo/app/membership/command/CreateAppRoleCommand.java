package io.cmartinezs.keygo.app.membership.command;

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
      throw new IllegalArgumentException("tenantSlug cannot be null or blank");
    }
    if (clientAppId == null) {
      throw new IllegalArgumentException("clientAppId cannot be null");
    }
    if (code == null || code.isBlank()) {
      throw new IllegalArgumentException("code cannot be null or blank");
    }
  }
}

