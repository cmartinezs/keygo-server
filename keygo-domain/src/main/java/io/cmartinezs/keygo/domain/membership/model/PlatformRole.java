package io.cmartinezs.keygo.domain.membership.model;

import lombok.Builder;
import lombok.Getter;

/**
 * PlatformRole domain entity — represents a role within the Keygo platform.
 * <p>Entidad de dominio PlatformRole — representa un rol en la plataforma Keygo.
 * Platform roles are global (not tenant- or app-scoped) and govern administrative
 * access to the platform itself.
 * <p>Los roles de plataforma son globales (no pertenecen a ningún tenant ni app)
 * y gobiernan el acceso administrativo a la propia plataforma.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public class PlatformRole {

  /** Well-known platform role codes. */
  public static final String KEYGO_ADMIN = "keygo_admin";
  public static final String KEYGO_ACCOUNT_ADMIN = "keygo_account_admin";
  public static final String KEYGO_USER = "keygo_user";

  private final PlatformRoleId id;
  private final String code;
  private String name;
  private String description;

  @Builder
  private PlatformRole(PlatformRoleId id, String code, String name, String description) {
    if (id == null) throw new IllegalArgumentException("PlatformRole id cannot be null");
    if (code == null || code.isBlank()) throw new IllegalArgumentException("PlatformRole code cannot be null or blank");
    if (name == null || name.isBlank()) throw new IllegalArgumentException("PlatformRole name cannot be null or blank");

    this.id = id;
    this.code = code;
    this.name = name;
    this.description = description;
  }

  /**
   * Update the display metadata of this platform role.
   * <p>Actualiza el nombre y descripción del rol de plataforma.
   */
  public void updateMetadata(String newName, String newDescription) {
    if (newName == null || newName.isBlank()) {
      throw new IllegalArgumentException("PlatformRole name cannot be null or blank");
    }
    this.name = newName;
    this.description = newDescription;
  }

  @Override
  public String toString() {
    return "PlatformRole[" + code + "]";
  }
}
