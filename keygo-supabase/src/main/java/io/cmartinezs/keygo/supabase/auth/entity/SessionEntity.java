package io.cmartinezs.keygo.supabase.auth.entity;

import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Entidad JPA: Sesión de usuario OAuth2.
 *
 * <p>Mapea la tabla {@code sessions} de la base de datos.
 *
 * <p>Modelo restructurado (RFC restructure-multitenant):
 * <ul>
 *   <li>{@code platformUser} — FK platform_users (nullable para MVP)
 *   <li>{@code clientApp} — FK client_apps (nullable — NULL = sesión de plataforma)
 *   <li>Removidos: {@code tenant} y {@code user} (ya no existen en la tabla)
 * </ul>
 */
@Entity
@Table(name = "sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  /** FK platform_users. Nullable para MVP (tenant-only users sin plataforma). */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "platform_user_id")
  private PlatformUserEntity platformUser;

  /** FK client_apps. NULL = sesión de plataforma (KeyGo UI). */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_app_id")
  private ClientAppEntity clientApp;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "last_accessed_at", nullable = false)
  private Instant lastAccessedAt;

  @Column(name = "user_agent", columnDefinition = "TEXT")
  private String userAgent;

  @Column(name = "ip_address", length = 64)
  private String ipAddress;

  /**
   * Clave RSA que firmó los tokens de apertura de esta sesión.
   * Nullable — sesiones legacy anteriores a V22 no tienen este dato.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "signing_key_id")
  private SigningKeyEntity signingKey;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}

