package io.cmartinezs.keygo.supabase.auth.entity;

import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
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
 * Entidad JPA: Código de autorización OAuth 2.0.
 *
 * <p>Mapea la tabla `authorization_codes` de la base de datos.
 */
@Entity
@Table(name = "authorization_codes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationCodeEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "code", nullable = false, unique = true, length = 256)
  private String code;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_app_id", nullable = false)
  private ClientAppEntity clientApp;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id", nullable = false)
  private TenantEntity tenant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private TenantUserEntity user;

  @Column(name = "code_challenge", nullable = false, length = 256)
  private String codeChallenge;

  @Column(name = "code_challenge_method", nullable = false, length = 10)
  private String codeChallengeMethod;

  @Column(name = "requested_scopes", nullable = false, columnDefinition = "TEXT")
  private String requestedScopes;

  @Column(name = "redirect_uri", nullable = false, length = 2048)
  private String redirectUri;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "used_at")
  private Instant usedAt;
}

