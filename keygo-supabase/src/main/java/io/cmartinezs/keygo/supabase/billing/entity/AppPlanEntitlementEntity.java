package io.cmartinezs.keygo.supabase.billing.entity;

import io.cmartinezs.keygo.domain.billing.catalog.model.EnforcementMode;
import io.cmartinezs.keygo.domain.billing.catalog.model.MetricType;
import io.cmartinezs.keygo.domain.billing.catalog.model.PeriodType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for app_plan_entitlements table.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_plan_entitlements")
public class AppPlanEntitlementEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "app_plan_version_id", nullable = false)
  private AppPlanVersionEntity appPlanVersion;

  @Column(name = "metric_code", nullable = false, length = 100)
  private String metricCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "metric_type", nullable = false, length = 20)
  private MetricType metricType;

  @Column(name = "limit_value", precision = 18, scale = 4)
  private BigDecimal limitValue;

  @Enumerated(EnumType.STRING)
  @Column(name = "period_type", nullable = false, length = 20)
  @Builder.Default
  private PeriodType periodType = PeriodType.NONE;

  @Enumerated(EnumType.STRING)
  @Column(name = "enforcement_mode", nullable = false, length = 20)
  @Builder.Default
  private EnforcementMode enforcementMode = EnforcementMode.HARD;

  @Column(name = "is_enabled", nullable = false)
  @Builder.Default
  private boolean isEnabled = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}

