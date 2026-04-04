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
 * Entidad JPA para persistencia de códigos de verificación del flujo RESET_PASSWORD.
 * <p>Un solo código activo por usuario ({@code UNIQUE tenant_user_id}).
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
    name = "password_reset_codes",
    uniqueConstraints = @UniqueConstraint(name = "uq_password_reset_codes_user", columnNames = "tenant_user_id"),
    indexes = @Index(name = "idx_password_reset_codes_user", columnList = "tenant_user_id"))
public class PasswordResetCodeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tenant_user_id", nullable = false)
  private TenantUserEntity tenantUser;

  @Column(nullable = false, length = 6)
  private String code;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "used_at")
  private Instant usedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}

