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
 * JPA entity for email verification persistence.
 * <p>Entidad JPA para persistencia de verificaciones de email.
 * Each row represents one verification attempt per tenant user.
 * The latest row per user (ordered by created_at DESC) is the active one.
 * <p>Cada fila representa un intento de verificación por usuario de tenant.
 * La fila más reciente por usuario (ordenada por created_at DESC) es la activa.
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
        @Index(name = "idx_email_verifications_tenant_user_id", columnList = "tenant_user_id"),
        @Index(name = "idx_email_verifications_code", columnList = "code")
    })
public class EmailVerificationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tenant_user_id", nullable = false)
  private TenantUserEntity tenantUser;

  @Column(nullable = false, length = 10)
  private String code;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "used_at")
  private Instant usedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}

