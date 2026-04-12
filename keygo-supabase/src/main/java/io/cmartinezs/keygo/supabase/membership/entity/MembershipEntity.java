package io.cmartinezs.keygo.supabase.membership.entity;

import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
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
 * JPA entity for membership persistence (user access to app).
 *
 * <p>Entidad JPA para persistencia de membresía (acceso de usuario a app).
 *
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
    name = "app_memberships",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_app_memberships_tenant_user_app",
          columnNames = {"tenant_user_id", "client_app_id"}),
      @UniqueConstraint(
          name = "uq_app_memberships_id_client_app",
          columnNames = {"id", "client_app_id"})
    },
    indexes = {
      @Index(name = "idx_app_memberships_tenant_user", columnList = "tenant_user_id"),
      @Index(name = "idx_app_memberships_client_app_status", columnList = "client_app_id, status")
    })
public class MembershipEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "tenant_user_id", nullable = false)
  private UUID tenantUserId;

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
  private TenantUserEntity user;

  @Column(name = "client_app_id", nullable = false)
  private UUID clientAppId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumns({
      @JoinColumn(
          name = "client_app_id",
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
  private ClientAppEntity clientApp;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private MembershipStatus status = MembershipStatus.ACTIVE;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
