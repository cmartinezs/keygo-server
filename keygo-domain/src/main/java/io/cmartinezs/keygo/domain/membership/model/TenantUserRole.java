package io.cmartinezs.keygo.domain.membership.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * TenantUserRole domain entity — represents an organizational role assignment for a tenant user.
 * <p>Entidad de dominio TenantUserRole — representa la asignación de un rol organizacional a un usuario en un tenant.
 * This is the N:N link between {@code tenant_users} and {@link TenantRole}.
 * Supports soft-delete via {@code removedAt} to maintain assignment history for auditing.
 * <p>Este es el vínculo N:N entre {@code tenant_users} y {@link TenantRole}.
 * Soporta eliminación lógica (soft delete) vía {@code removedAt} para mantener historial de asignaciones.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public class TenantUserRole {

  private final TenantUserRoleId id;
  private final UUID tenantUserId;
  private final TenantRoleId tenantRoleId;
  private final Instant assignedAt;
  private Instant removedAt;

  @Builder
  private TenantUserRole(TenantUserRoleId id, UUID tenantUserId, TenantRoleId tenantRoleId,
      Instant assignedAt, Instant removedAt) {
    if (id == null) throw new IllegalArgumentException("TenantUserRole id cannot be null");
    if (tenantUserId == null) throw new IllegalArgumentException("TenantUserRole tenantUserId cannot be null");
    if (tenantRoleId == null) throw new IllegalArgumentException("TenantUserRole tenantRoleId cannot be null");

    this.id = id;
    this.tenantUserId = tenantUserId;
    this.tenantRoleId = tenantRoleId;
    this.assignedAt = assignedAt != null ? assignedAt : Instant.now();
    this.removedAt = removedAt;
  }

  /**
   * Returns whether this role assignment is currently active.
   * <p>Indica si esta asignación de rol está actualmente vigente.
   */
  public boolean isActive() {
    return removedAt == null;
  }

  /**
   * Revoke this role assignment (soft delete).
   * <p>Revoca esta asignación de rol (eliminación lógica).
   * @throws IllegalStateException if already revoked
   */
  public void revoke() {
    if (!isActive()) {
      throw new IllegalStateException("TenantUserRole is already revoked: " + id);
    }
    this.removedAt = Instant.now();
  }

  @Override
  public String toString() {
    return "TenantUserRole[tenantUserId=" + tenantUserId + ", tenantRoleId=" + tenantRoleId
        + (isActive() ? ", ACTIVE" : ", REVOKED at " + removedAt) + "]";
  }
}
