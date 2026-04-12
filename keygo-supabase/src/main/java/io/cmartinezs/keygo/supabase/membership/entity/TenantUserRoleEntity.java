package io.cmartinezs.keygo.supabase.membership.entity;

import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import jakarta.persistence.*;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * JPA entity for tenant-level user role assignment persistence.
 * <p>Entidad JPA para persistencia de asignación de roles de tenant a usuarios de tenant.
 * The table is a pure join: revocation deletes the row instead of marking it as removed.
 * <p>La tabla es un vínculo puro: la revocación elimina la fila en lugar de marcarla como removida.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(TenantUserRoleKey.class)
@Table(
    name = "tenant_user_roles",
    indexes = {
      @Index(name = "idx_tenant_user_roles_tenant_role", columnList = "tenant_id, role_id")
    })
public class TenantUserRoleEntity {

  @Id
  @Column(name = "tenant_user_id", nullable = false)
  private UUID tenantUserId;

  @Id
  @Column(name = "role_id", nullable = false)
  private UUID tenantRoleId;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumns({
      @JoinColumn(
          name = "tenant_user_id",
          referencedColumnName = "id",
          nullable = false,
          insertable = false,
          updatable = false),
      @JoinColumn(
          name = "tenant_id",
          referencedColumnName = "tenant_id",
          nullable = false,
          insertable = false,
          updatable = false)
  })
  private TenantUserEntity tenantUser;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumns({
      @JoinColumn(
          name = "role_id",
          referencedColumnName = "id",
          nullable = false,
          insertable = false,
          updatable = false),
      @JoinColumn(
          name = "tenant_id",
          referencedColumnName = "tenant_id",
          nullable = false,
          insertable = false,
          updatable = false)
  })
  private TenantRoleEntity tenantRole;

  @Transient
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private UUID id;

  @Column(name = "assigned_at", nullable = false)
  @Builder.Default
  private OffsetDateTime assignedAt = OffsetDateTime.now();

  /** Returns true because persisted rows represent active assignments only. */
  public boolean isActive() {
    return true;
  }

  public UUID getId() {
    if (id == null && tenantUserId != null && tenantRoleId != null) {
      id = UUID.nameUUIDFromBytes(
          (tenantUserId + ":" + tenantRoleId).getBytes(StandardCharsets.UTF_8));
    }
    return id;
  }
}
