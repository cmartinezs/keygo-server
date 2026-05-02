package io.cmartinezs.keygo.domain.membership.model;

import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import lombok.Builder;
import lombok.Getter;

/**
 * AppRole domain entity — represents a role within a single application.
 * <p>Entidad de dominio AppRole — representa un rol dentro de una única aplicación.
 * Roles are application-scoped, not global. Multiple roles can be assigned to a membership.
 * <p>Los roles son locales a la aplicación, no globales. Múltiples roles pueden asignarse a una membresía.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public class AppRole {

  private final AppRoleId id;
  private final ClientAppId clientAppId;
  private final RoleCode code;
  private String displayName;
  private String description;
  private boolean isDefault;

  @Builder
  private AppRole(AppRoleId id, ClientAppId clientAppId, RoleCode code, String displayName, String description, boolean isDefault) {
    if (clientAppId == null) throw new IllegalArgumentException("AppRole clientAppId cannot be null");
    if (code == null) throw new IllegalArgumentException("AppRole code cannot be null");

    this.id = id;
    this.clientAppId = clientAppId;
    this.code = code;
    this.displayName = displayName;
    this.description = description;
    this.isDefault = isDefault;
  }

  /* Domain behaviour */

  /**
   * Update role display name and description.
   * <p>Actualiza el nombre y descripción del rol.
   */
  public void updateMetadata(String newDisplayName, String newDescription) {
    this.displayName = newDisplayName;
    this.description = newDescription;
  }

  @Override
  public String toString() {
    return "AppRole[" + code.value() + " in app " + clientAppId + "]";
  }
}

