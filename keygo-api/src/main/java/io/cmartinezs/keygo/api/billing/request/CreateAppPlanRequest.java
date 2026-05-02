package io.cmartinezs.keygo.api.billing.request;

import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;
import io.cmartinezs.keygo.domain.billing.catalog.model.EnforcementMode;
import io.cmartinezs.keygo.domain.billing.catalog.model.MetricType;
import io.cmartinezs.keygo.domain.billing.catalog.model.PeriodType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

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
    @NotBlank(message = "code is required") String code,
    @NotBlank(message = "name is required") String name,
    String description,
    boolean isPublic,
    @PositiveOrZero(message = "sort_order must be zero or positive") int sortOrder,
    @NotBlank(message = "version is required") String version,
    @NotBlank(message = "currency is required") String currency,
    @PositiveOrZero(message = "trial_days must be zero or positive") int trialDays,
    LocalDate effectiveFrom,
    List<@Valid BillingOptionRequest> billingOptions,
    List<@Valid EntitlementRequest> entitlements
) {
  public record BillingOptionRequest(
      @NotNull(message = "billing_period is required") BillingPeriod billingPeriod,
      @NotNull(message = "base_price is required") BigDecimal basePrice,
      BigDecimal discountPct,
      boolean isDefault
  ) {}

  public record EntitlementRequest(
      @NotBlank(message = "metric_code is required") String metricCode,
      @NotNull(message = "metric_type is required") MetricType metricType,
      BigDecimal limitValue,
      PeriodType periodType,
      EnforcementMode enforcementMode,
      boolean isEnabled
  ) {}
}
