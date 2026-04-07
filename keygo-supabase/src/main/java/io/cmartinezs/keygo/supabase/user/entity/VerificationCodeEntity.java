package io.cmartinezs.keygo.supabase.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA unificada para códigos de verificación.
 *
 * <p>Consolida {@code EmailVerificationEntity}, {@code PasswordResetCodeEntity} y
 * {@code PasswordRecoveryTokenEntity} en una sola entidad con discriminador {@code purpose}.
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
    name = "verification_codes",
    indexes = {
        @Index(name = "idx_vc_tenant_user", columnList = "tenant_user_id"),
        @Index(name = "idx_vc_platform_user", columnList = "platform_user_id"),
        @Index(name = "idx_vc_code", columnList = "code"),
        @Index(name = "idx_vc_purpose", columnList = "purpose")
    })
public class VerificationCodeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_user_id")
  private TenantUserEntity tenantUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "platform_user_id")
  private PlatformUserEntity platformUser;

  @Column(nullable = false, length = 30)
  private String purpose;

  @Column(nullable = false, length = 64)
  private String code;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "used_at")
  private Instant usedAt;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private String metadata;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  /**
   * Retorna el UUID del usuario propietario (tenant o plataforma).
   */
  public UUID getOwnerUserId() {
    if (platformUser != null) {
      return platformUser.getId();
    }
    return tenantUser != null ? tenantUser.getId() : null;
  }
}
