package io.cmartinezs.keygo.app.billing.catalog.command;

import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;
import io.cmartinezs.keygo.domain.billing.catalog.model.EnforcementMode;
import io.cmartinezs.keygo.domain.billing.catalog.model.MetricType;
import io.cmartinezs.keygo.domain.billing.catalog.model.PeriodType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Command to create a new billing plan with its first version and entitlements.
 * <p>
 * {@code billingOptions} defines the available billing periods for this version.
 * An empty list means the plan is free (no payment required).
 * {@code sortOrder} controls the display order in the catalog (lower = cheaper / shown first).
 */
public record CreateAppPlanCommand(
    UUID clientAppId,
    String code,
    String name,
    String description,
    boolean isPublic,
    int sortOrder,
    String version,
    String currency,
    int trialDays,
    LocalDate effectiveFrom,
    List<BillingOptionDef> billingOptions,
    List<EntitlementDef> entitlements
) {
  public record BillingOptionDef(
      BillingPeriod billingPeriod,
      BigDecimal basePrice,
      BigDecimal discountPct,
      boolean isDefault
  ) {}

  public record EntitlementDef(
      String metricCode,
      MetricType metricType,
      BigDecimal limitValue,
      PeriodType periodType,
      EnforcementMode enforcementMode,
      boolean isEnabled
  ) {}
}
