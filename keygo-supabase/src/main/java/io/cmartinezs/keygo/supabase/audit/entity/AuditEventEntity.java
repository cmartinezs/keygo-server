package io.cmartinezs.keygo.supabase.audit.entity;

import io.cmartinezs.keygo.supabase.auth.entity.PlatformSessionEntity;
import io.cmartinezs.keygo.supabase.auth.entity.SessionEntity;
import io.cmartinezs.keygo.supabase.billing.entity.ContractorEntity;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
@Table(name = "audit_events")
public class AuditEventEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "occurred_at", nullable = false)
  private OffsetDateTime occurredAt;

  @Column(name = "actor_type", nullable = false, length = 30)
  private String actorType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_platform_user_id")
  private PlatformUserEntity actorPlatformUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_tenant_user_id")
  private TenantUserEntity actorTenantUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contractor_id")
  private ContractorEntity contractor;

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

  @Column(name = "source_layer", nullable = false, length = 30)
  private String sourceLayer;

  @Column(name = "source_channel", nullable = false, length = 30)
  private String sourceChannel;

  @Column(name = "source_app", length = 100)
  private String sourceApp;

  @Column(name = "source_module", length = 100)
  private String sourceModule;

  @Column(name = "source_feature", length = 100)
  private String sourceFeature;

  @Column(name = "source_screen", length = 150)
  private String sourceScreen;

  @Column(name = "source_route", length = 500)
  private String sourceRoute;

  @Column(name = "request_id", length = 100)
  private String requestId;

  @Column(name = "correlation_id", length = 100)
  private String correlationId;

  @Column(name = "trace_id", length = 100)
  private String traceId;

  @Column(name = "span_id", length = 100)
  private String spanId;

  @Column(name = "http_method", length = 10)
  private String httpMethod;

  @Column(name = "http_path", length = 1000)
  private String httpPath;

  @Column(name = "http_status_code")
  private Integer httpStatusCode;

  @Column(name = "event_category", nullable = false, length = 50)
  private String eventCategory;

  @Column(name = "event_type", nullable = false, length = 100)
  private String eventType;

  @Column(name = "event_action", nullable = false, length = 100)
  private String eventAction;

  @Column(name = "event_outcome", nullable = false, length = 20)
  private String eventOutcome;

  @Column(name = "severity", nullable = false, length = 20)
  private String severity;

  @Column(name = "target_type", length = 100)
  private String targetType;

  @Column(name = "target_id")
  private UUID targetId;

  @Column(name = "target_display", length = 255)
  private String targetDisplay;

  @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
  private String summary;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
  @Builder.Default
  private String metadata = "{}";

  @Column(name = "duration_ms")
  private Long durationMs;

  @Column(name = "records_affected")
  private Integer recordsAffected;

  @Column(name = "risk_score", precision = 5, scale = 2)
  private BigDecimal riskScore;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
