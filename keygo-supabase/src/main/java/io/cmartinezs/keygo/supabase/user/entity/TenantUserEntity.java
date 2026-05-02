package io.cmartinezs.keygo.supabase.user.entity;

import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
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
 * JPA entity for tenant participation.
 * <p>Entidad JPA para pertenencia de un platform user a un tenant.
 * Local usernames remain tenant-scoped aliases, while credentials and profile live in
 * {@link PlatformUserEntity}.
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
    name = "tenant_users",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_tenant_users_tenant_platform_user",
          columnNames = {"tenant_id", "platform_user_id"}),
      @UniqueConstraint(name = "uq_tenant_users_id_tenant", columnNames = {"id", "tenant_id"})
    },
    indexes = {
      @Index(name = "idx_tenant_users_tenant_id", columnList = "tenant_id"),
      @Index(name = "idx_tenant_users_platform_user", columnList = "platform_user_id"),
      @Index(name = "idx_tenant_users_status", columnList = "status")
    })
public class TenantUserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "tenant_id", nullable = false, insertable = false, updatable = false)
  private UUID tenantId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tenant_id", nullable = false)
  private TenantEntity tenant;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "platform_user_id", nullable = false)
  private PlatformUserEntity platformUser;

  @Column(name = "local_username", length = 100)
  private String localUsername;

  @Column(name = "display_name_override", length = 255)
  private String displayNameOverride;

  @Enumerated(jakarta.persistence.EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private UserStatus status = UserStatus.ACTIVE;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

}
