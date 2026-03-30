package io.cmartinezs.keygo.supabase.billing.entity;

import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for app_plan_versions table.
 * Pricing per billing period is defined in {@link AppPlanBillingOptionEntity}.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_plan_versions")
public class AppPlanVersionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "app_plan_id", nullable = false)
  private AppPlanEntity appPlan;

  @Column(nullable = false, length = 20)
  private String version;

  @Column(nullable = false, length = 3)
  @Builder.Default
  private String currency = "USD";

  @Column(name = "setup_fee", nullable = false, precision = 12, scale = 2)
  @Builder.Default
  private BigDecimal setupFee = BigDecimal.ZERO;

  @Column(name = "trial_days", nullable = false)
  @Builder.Default
  private int trialDays = 0;

  @Column(name = "effective_from", nullable = false)
  private LocalDate effectiveFrom;

  @Column(name = "effective_to")
  private LocalDate effectiveTo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private AppPlanVersionStatus status = AppPlanVersionStatus.ACTIVE;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;
}
