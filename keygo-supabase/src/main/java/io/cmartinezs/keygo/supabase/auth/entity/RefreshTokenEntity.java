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

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "oauth_session_id", nullable = false)
  private SessionEntity session;

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

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
