package io.cmartinezs.keygo.supabase.membership.entity;

import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
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
 * <p>Entidad JPA para persistencia de membresía (acceso de usuario a app).
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
    name = "membership",
    uniqueConstraints = {
      @UniqueConstraint(name = "uq_membership_user_app", columnNames = {"user_id", "client_app_id"})
    },
    indexes = {
      @Index(name = "idx_membership_user_id", columnList = "user_id"),
      @Index(name = "idx_membership_client_app_id", columnList = "client_app_id"),
      @Index(name = "idx_membership_status", columnList = "status")
    })
public class MembershipEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private TenantUserEntity user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "client_app_id", nullable = false)
  private ClientAppEntity clientApp;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private MembershipStatus status = MembershipStatus.ACTIVE;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "membership_role",
      joinColumns = @JoinColumn(name = "membership_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
  private Set<AppRoleEntity> roles = new HashSet<>();

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}

