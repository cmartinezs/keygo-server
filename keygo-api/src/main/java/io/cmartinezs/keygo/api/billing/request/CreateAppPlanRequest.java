package io.cmartinezs.keygo.api.billing.request;

import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;
import io.cmartinezs.keygo.domain.billing.catalog.model.EnforcementMode;
import io.cmartinezs.keygo.domain.billing.catalog.model.MetricType;
import io.cmartinezs.keygo.domain.billing.catalog.model.PeriodType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request body for creating an app billing plan.
 * <p>
 * {@code sortOrder} controls the display order in the catalog (lower = cheaper / shown first).
 * {@code billingOptions} defines the available billing periods for this version.
 * An empty list (or null) means the plan is free (no payment required).
 */
public record CreateAppPlanRequest(
    String code,
    String name,
    String description,
    boolean isPublic,
    int sortOrder,
    String version,
    String currency,
    int trialDays,
    LocalDate effectiveFrom,
    List<BillingOptionRequest> billingOptions,
    List<EntitlementRequest> entitlements
) {
  public record BillingOptionRequest(
      BillingPeriod billingPeriod,
      BigDecimal basePrice,
      BigDecimal discountPct,
      boolean isDefault
  ) {}

  public record EntitlementRequest(
      String metricCode,
      MetricType metricType,
      Long limitValue,
      PeriodType periodType,
      EnforcementMode enforcementMode,
      boolean isEnabled
  ) {}
}
