package io.cmartinezs.keygo.supabase.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Entidad JPA para códigos de verificación basados en código.
 *
 * <p>Mapea la tabla {@code email_verifications}. Se reutiliza para:
 * <ul>
 *   <li>{@code EMAIL_VERIFICATION}</li>
 *   <li>{@code PASSWORD_RESET}</li>
 * </ul>
 * Los tokens de recuperación hashados viven en {@code password_reset_tokens}.
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
    name = "email_verifications",
    indexes = {
        @Index(name = "uq_email_verifications_active_user", columnList = "platform_user_id"),
        @Index(name = "idx_email_verifications_code", columnList = "code")
    })
public class VerificationCodeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "platform_user_id", nullable = false)
  private PlatformUserEntity platformUser;

  @Column(nullable = false, length = 10)
  private String code;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "used_at")
  private Instant usedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  /**
   * Retorna el UUID del usuario propietario (tenant o plataforma).
   */
  public UUID getOwnerUserId() {
    return platformUser != null ? platformUser.getId() : null;
  }
}
