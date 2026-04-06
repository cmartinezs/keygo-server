package io.cmartinezs.keygo.supabase.membership.entity;

import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA entity for tenant-level user role assignment persistence.
 * <p>Entidad JPA para persistencia de asignación de roles de tenant a usuarios de tenant.
 * Supports soft-delete via removed_at: a null value means the assignment is active.
 * <p>Soporta eliminación lógica via removed_at: valor null significa que la asignación está activa.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "tenant_user_roles",
    indexes = {
      @Index(name = "idx_tenant_user_roles_tenant_user_id", columnList = "tenant_user_id"),
      @Index(name = "idx_tenant_user_roles_tenant_role_id", columnList = "tenant_role_id"),
      @Index(name = "idx_tenant_user_roles_removed_at", columnList = "removed_at")
    })
public class TenantUserRoleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tenant_user_id", nullable = false)
  private TenantUserEntity tenantUser;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tenant_role_id", nullable = false)
  private TenantRoleEntity tenantRole;

  @Column(name = "assigned_at", nullable = false)
  @Builder.Default
  private OffsetDateTime assignedAt = OffsetDateTime.now();

  @Column(name = "removed_at")
  private OffsetDateTime removedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  /** Returns true if the assignment is still active (not revoked). */
  public boolean isActive() {
    return removedAt == null;
  }
}
