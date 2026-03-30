package io.cmartinezs.keygo.supabase.billing.entity;

import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA entity for app_plan_billing_options table.
 * <p>
 * Each row represents a billing period (MONTHLY, YEARLY, ONE_TIME) available
 * for a given plan version. An absence of rows means the plan is free.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_plan_billing_options",
    indexes = @Index(name = "idx_app_plan_billing_options_version", columnList = "app_plan_version_id"))
public class AppPlanBillingOptionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "app_plan_version_id", nullable = false)
  private AppPlanVersionEntity appPlanVersion;

  @Enumerated(EnumType.STRING)
  @Column(name = "billing_period", nullable = false, length = 20)
  private BillingPeriod billingPeriod;

  @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
  @Builder.Default
  private BigDecimal basePrice = BigDecimal.ZERO;

  /** Discount percentage vs equivalent monthly billing (0–100). Informational for UI. */
  @Column(name = "discount_pct", nullable = false, precision = 5, scale = 2)
  @Builder.Default
  private BigDecimal discountPct = BigDecimal.ZERO;

  @Column(name = "is_default", nullable = false)
  @Builder.Default
  private boolean isDefault = false;
}

