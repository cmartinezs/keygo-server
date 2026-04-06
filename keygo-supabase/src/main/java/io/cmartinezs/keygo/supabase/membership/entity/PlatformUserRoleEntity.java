package io.cmartinezs.keygo.supabase.membership.entity;

import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
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
 * JPA entity for platform-level user role assignment persistence.
 * <p>Entidad JPA para persistencia de asignación de roles de plataforma a usuarios.
 * Links a platform user (global identity) to a platform role.
 * <p>Modelo restructurado (RFC restructure-multitenant):
 * FK now references platform_users instead of tenant_users.
 * @author cmartinezs
 * @version 2.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "platform_user_roles",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_platform_user_roles_user_role",
          columnNames = {"platform_user_id", "platform_role_id"})
    },
    indexes = {
      @Index(name = "idx_platform_user_roles_platform_user", columnList = "platform_user_id"),
      @Index(name = "idx_platform_user_roles_platform_role_id", columnList = "platform_role_id")
    })
public class PlatformUserRoleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "platform_user_id", nullable = false)
  private PlatformUserEntity platformUser;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "platform_role_id", nullable = false)
  private PlatformRoleEntity platformRole;

  @Column(name = "assigned_at", nullable = false)
  @Builder.Default
  private OffsetDateTime assignedAt = OffsetDateTime.now();

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
