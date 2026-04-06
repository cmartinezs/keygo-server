package io.cmartinezs.keygo.supabase.auth.entity;

import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
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
 * Entidad JPA: Refresh Token OAuth2.
 *
 * <p>Mapea la tabla {@code refresh_tokens} de la base de datos.
 * Almacena el hash SHA-256 (hex) del token plano, nunca el token en texto claro.
 *
 * <p>Modelo restructurado (RFC restructure-multitenant):
 * <ul>
 *   <li>{@code clientApp} — nullable (NULL = RT de sesión de plataforma)
 *   <li>{@code tenantUser} — nullable (contexto tenant para lookup rápido de roles en rotación)
 *   <li>Removidos: {@code tenant} y {@code user} (ya no existen en la tabla)
 * </ul>
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id", nullable = false)
  private SessionEntity session;

  /** FK client_apps. Nullable — NULL para RT de sesión de plataforma. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_app_id")
  private ClientAppEntity clientApp;

  /** FK tenant_users. Nullable — para lookup rápido de roles en rotación de tenant app. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_user_id")
  private TenantUserEntity tenantUser;

  @Column(name = "requested_scopes", nullable = false, columnDefinition = "TEXT")
  private String requestedScopes;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "used_at")
  private Instant usedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "replaced_by_id")
  private RefreshTokenEntity replacedBy;

  /**
   * Clave RSA que firmó el access_token emitido junto a este RT.
   * Nullable — RT legacy anterior a V22 no tiene este dato.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "signing_key_id")
  private SigningKeyEntity signingKey;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}

