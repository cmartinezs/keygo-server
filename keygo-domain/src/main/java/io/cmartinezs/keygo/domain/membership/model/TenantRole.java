package io.cmartinezs.keygo.domain.membership.model;

import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import lombok.Builder;
import lombok.Getter;

/**
 * TenantRole domain entity — represents an organizational role within a specific tenant.
 * <p>Entidad de dominio TenantRole — representa un rol organizacional dentro de un tenant específico.
 * Tenant roles are defined per-tenant (code is unique within a tenant) and govern
 * organizational access, separate from platform-level and app-level RBAC.
 * <p>Los roles de tenant se definen por tenant (código único dentro del tenant) y gobiernan
 * el acceso organizacional, separado del RBAC de plataforma y del RBAC de app.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public class TenantRole {

  private final TenantRoleId id;
  private final TenantId tenantId;
  private final String code;
  private String name;
  private String description;
  private boolean active;

  @Builder
  private TenantRole(TenantRoleId id, TenantId tenantId, String code, String name, String description, Boolean active) {
    if (tenantId == null) throw new IllegalArgumentException("TenantRole tenantId cannot be null");
    if (code == null || code.isBlank()) throw new IllegalArgumentException("TenantRole code cannot be null or blank");
    if (name == null || name.isBlank()) throw new IllegalArgumentException("TenantRole name cannot be null or blank");
    if (!code.matches("^[A-Z][A-Z0-9_]*$")) {
      throw new IllegalArgumentException(
          "TenantRole code must be uppercase, start with a letter, and contain only letters, digits or underscores");
    }

    this.id = id;
    this.tenantId = tenantId;
    this.code = code;
    this.name = name;
    this.description = description;
    this.active = active != null ? active : true;
  }

  /**
   * Update the display metadata of this tenant role.
   * <p>Actualiza el nombre y descripción del rol de tenant.
   */
  public void updateMetadata(String newName, String newDescription) {
    if (newName == null || newName.isBlank()) {
      throw new IllegalArgumentException("TenantRole name cannot be null or blank");
    }
    this.name = newName;
    this.description = newDescription;
  }

  /**
   * Deactivate this tenant role, preventing further assignments.
   * <p>Desactiva este rol de tenant, impidiendo nuevas asignaciones.
   */
  public void deactivate() {
    this.active = false;
  }

  /**
   * Reactivate this tenant role.
   * <p>Reactiva este rol de tenant.
   */
  public void reactivate() {
    this.active = true;
  }

  @Override
  public String toString() {
    return "TenantRole[" + code + " in tenant " + tenantId + "]";
  }
}
