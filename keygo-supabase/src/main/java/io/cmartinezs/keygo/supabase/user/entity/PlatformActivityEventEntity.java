package io.cmartinezs.keygo.supabase.user.entity;

import io.cmartinezs.keygo.supabase.auth.entity.PlatformSessionEntity;
import io.cmartinezs.keygo.supabase.auth.entity.SessionEntity;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "platform_activity_events")
public class PlatformActivityEventEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "platform_user_id", nullable = false)
  private PlatformUserEntity platformUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id")
  private TenantEntity tenant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_app_id")
  private ClientAppEntity clientApp;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "platform_session_id")
  private PlatformSessionEntity platformSession;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "oauth_session_id")
  private SessionEntity oauthSession;

  @Column(name = "event_type", nullable = false, length = 100)
  private String eventType;

  @Column(name = "event_category", nullable = false, length = 50)
  private String eventCategory;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
  @Builder.Default
  private String metadata = "{}";

  @Column(name = "occurred_at", nullable = false)
  private OffsetDateTime occurredAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
