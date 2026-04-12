package io.cmartinezs.keygo.domain.membership.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * TenantUserRole domain entity — represents an organizational role assignment for a tenant user.
 * <p>Entidad de dominio TenantUserRole — representa la asignación de un rol organizacional a un usuario en un tenant.
 * This is the N:N link between {@code tenant_users} and {@link TenantRole}.
 * If the assignment row exists, the role is active; revocation is handled by deleting the link.
 * <p>Este es el vínculo N:N entre {@code tenant_users} y {@link TenantRole}.
 * Si la fila existe, la asignación está activa; la revocación se resuelve eliminando el vínculo.
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

  @Builder
  private TenantUserRole(TenantUserRoleId id, UUID tenantUserId, TenantRoleId tenantRoleId,
      Instant assignedAt) {
    if (id == null) throw new IllegalArgumentException("TenantUserRole id cannot be null");
    if (tenantUserId == null) throw new IllegalArgumentException("TenantUserRole tenantUserId cannot be null");
    if (tenantRoleId == null) throw new IllegalArgumentException("TenantUserRole tenantRoleId cannot be null");

    this.id = id;
    this.tenantUserId = tenantUserId;
    this.tenantRoleId = tenantRoleId;
    this.assignedAt = assignedAt != null ? assignedAt : Instant.now();
  }

  /**
   * Returns whether this persisted assignment is active.
   * <p>Indica si esta asignación persistida está actualmente vigente.
   */
  public boolean isActive() {
    return true;
  }

  @Override
  public String toString() {
    return "TenantUserRole[tenantUserId=" + tenantUserId + ", tenantRoleId=" + tenantRoleId + ", ACTIVE]";
  }
}
