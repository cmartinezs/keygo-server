package io.cmartinezs.keygo.supabase.billing.entity;

import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for usage_counters table — billing model v2 (contractor-centric).
 * Atomic counters per (app, contractor, metric, period).
 * Increment with UPDATE ... SET used_value = used_value + delta (no app-level locking).
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
        @Index(name = "idx_usage_counters_app_contractor", columnList = "client_app_id, contractor_id"),
        @Index(name = "idx_usage_counters_contractor",     columnList = "contractor_id")
    })
public class UsageCounterEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_app_id", nullable = false)
  private ClientAppEntity clientApp;

  /** Contractor who owns the counter. Replaces polymorphic subscriber_tenant_id/user_id. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contractor_id", nullable = false)
  private ContractorEntity contractor;

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
