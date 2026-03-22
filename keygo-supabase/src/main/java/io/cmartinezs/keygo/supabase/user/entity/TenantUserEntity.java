package io.cmartinezs.keygo.supabase.user.entity;

import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for tenant-scoped user persistence.
 * <p>Entidad JPA para persistencia de usuarios con alcance de tenant.
 * Username and email are unique per tenant (composite unique constraints).
 * <p>El username y email son únicos por tenant (restricciones únicas compuestas).
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
      @UniqueConstraint(name = "uq_tenant_users_tenant_email", columnNames = {"tenant_id", "email"}),
      @UniqueConstraint(name = "uq_tenant_users_tenant_username", columnNames = {"tenant_id", "username"})
    },
    indexes = {
      @Index(name = "idx_tenant_users_tenant_id", columnList = "tenant_id"),
      @Index(name = "idx_tenant_users_email", columnList = "email"),
      @Index(name = "idx_tenant_users_username", columnList = "username"),
      @Index(name = "idx_tenant_users_status", columnList = "status")
    })
public class TenantUserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tenant_id", nullable = false)
  private TenantEntity tenant;

  @Column(nullable = false, length = 100)
  private String username;

  @Column(nullable = false, length = 255)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "first_name", length = 100)
  private String firstName;

  @Column(name = "last_name", length = 100)
  private String lastName;

  @Enumerated(EnumType.STRING)
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

