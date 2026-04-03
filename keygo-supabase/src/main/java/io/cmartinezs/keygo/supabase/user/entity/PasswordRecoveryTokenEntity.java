package io.cmartinezs.keygo.supabase.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for password recovery token persistence.
 * <p>Entidad JPA para persistencia de tokens de recuperación de contraseña.
 * One active token per user ({@code UNIQUE(tenant_user_id)}) — upsert invalidates the previous one.
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
    name = "password_recovery_tokens",
    uniqueConstraints = @UniqueConstraint(name = "uq_pwd_recovery_tenant_user", columnNames = "tenant_user_id"),
    indexes = @Index(name = "idx_pwd_recovery_token", columnList = "token"))
public class PasswordRecoveryTokenEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tenant_user_id", nullable = false)
  private TenantUserEntity tenantUser;

  @Column(nullable = false, length = 64)
  private String token;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "used_at")
  private Instant usedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
