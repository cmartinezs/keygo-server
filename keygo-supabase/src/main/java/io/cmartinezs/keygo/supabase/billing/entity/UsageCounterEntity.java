package io.cmartinezs.keygo.supabase.billing.entity;

import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for usage_counters table.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usage_counters",
    indexes = {
        @Index(name = "idx_usage_counters_app_tenant", columnList = "client_app_id, subscriber_tenant_id"),
        @Index(name = "idx_usage_counters_app_user",   columnList = "client_app_id, subscriber_tenant_user_id")
    })
public class UsageCounterEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_app_id", nullable = false)
  private ClientAppEntity clientApp;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscriber_tenant_id")
  private TenantEntity subscriberTenant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscriber_tenant_user_id")
  private TenantUserEntity subscriberTenantUser;

  @Column(name = "metric_code", nullable = false, length = 100)
  private String metricCode;

  @Column(name = "period_start", nullable = false)
  private OffsetDateTime periodStart;

  @Column(name = "period_end", nullable = false)
  private OffsetDateTime periodEnd;

  @Column(name = "used_value", nullable = false)
  @Builder.Default
  private long usedValue = 0;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
