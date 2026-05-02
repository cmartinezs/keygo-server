package io.cmartinezs.keygo.supabase.membership.entity;

import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA entity for tenant-level role persistence.
 * <p>Entidad JPA para persistencia de roles a nivel tenant.
 * Each tenant can define its own roles (e.g. TENANT_ADMIN, TENANT_EDITOR).
 * Role codes are unique per tenant.
 * <p>Cada tenant puede definir sus propios roles. Los códigos son únicos por tenant.
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
    name = "tenant_roles",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_tenant_roles_tenant_code",
          columnNames = {"tenant_id", "code"})
    },
    indexes = {
      @Index(name = "idx_tenant_roles_tenant_id", columnList = "tenant_id"),
      @Index(name = "idx_tenant_roles_code", columnList = "code")
    })
public class TenantRoleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "tenant_id", nullable = false, insertable = false, updatable = false)
  private UUID tenantId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tenant_id", nullable = false)
  private TenantEntity tenant;

  @Column(nullable = false, length = 50)
  private String code;

  @Column(name = "display_name", length = 255)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Transient
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  @Builder.Default
  private boolean active = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
