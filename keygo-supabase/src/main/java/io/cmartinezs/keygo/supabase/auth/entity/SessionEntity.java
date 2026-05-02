package io.cmartinezs.keygo.supabase.auth.entity;

import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
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
 * Entidad JPA para el contexto OAuth persistido en {@code oauth_sessions}.
 */
@Entity
@Table(name = "oauth_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "platform_session_id", nullable = false)
  private PlatformSessionEntity platformSession;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "platform_user_id", nullable = false)
  private PlatformUserEntity platformUser;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "tenant_user_id")
  private UUID tenantUserId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumns({
      @JoinColumn(
          name = "tenant_user_id",
          referencedColumnName = "id",
          insertable = false,
          updatable = false),
      @JoinColumn(
          name = "tenant_id",
          referencedColumnName = "tenant_id",
          insertable = false,
          updatable = false)
  })
  private TenantUserEntity tenantUser;

  @Column(name = "client_app_id", nullable = false)
  private UUID clientAppId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumns({
      @JoinColumn(
          name = "client_app_id",
          referencedColumnName = "id",
          insertable = false,
          updatable = false),
      @JoinColumn(
          name = "tenant_id",
          referencedColumnName = "tenant_id",
          insertable = false,
          updatable = false)
  })
  private ClientAppEntity clientApp;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "signing_key_id")
  private SigningKeyEntity signingKey;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "last_accessed_at", nullable = false)
  private Instant lastAccessedAt;

  @Column(name = "login_prompt", length = 30)
  private String loginPrompt;

  @Column(name = "auth_context_class", length = 100)
  private String authContextClass;

  @Column(name = "granted_scopes", columnDefinition = "TEXT")
  private String grantedScopes;

  @Column(name = "consent_required", nullable = false)
  @Builder.Default
  private boolean consentRequired = false;

  @Column(name = "consent_granted", nullable = false)
  @Builder.Default
  private boolean consentGranted = false;

  @Column(name = "issued_ip_address", length = 64)
  private String issuedIpAddress;

  @Column(name = "started_at", nullable = false)
  private Instant startedAt;

  @Column(name = "ended_at")
  private Instant endedAt;

  @Column(name = "termination_reason", length = 50)
  private String terminationReason;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
