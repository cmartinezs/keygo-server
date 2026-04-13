package io.cmartinezs.keygo.api.billing.response;

import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanBillingOption;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.billing.catalog.model.EnforcementMode;
import io.cmartinezs.keygo.domain.billing.catalog.model.MetricType;
import io.cmartinezs.keygo.domain.billing.catalog.model.PeriodType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response data for app plan catalog endpoints.
 * <p>
 * {@code sortOrder} allows the UI to display plans from cheapest to most expensive.
 * Each version carries a {@code billingOptions} list:
 * <ul>
 *   <li>Empty list → free plan (no payment required).</li>
 *   <li>Non-empty list → one entry per available billing period (MONTHLY, YEARLY, …).</li>
 * </ul>
 */
public record AppPlanData(
    UUID id,
    UUID clientAppId,
    String code,
    String name,
    String description,
    String status,
    boolean isPublic,
    int sortOrder,
    List<AppPlanVersionData> versions,
    List<AppPlanEntitlementData> entitlements
) {
  /** Billing period option within a plan version. */
  public record AppPlanBillingOptionData(
      String billingPeriod,
      BigDecimal basePrice,
      BigDecimal discountPct,
      boolean isDefault
  ) {}

  /** Version snapshot — pricing is in billingOptions; empty = free plan. */
  public record AppPlanVersionData(
      UUID id,
      String version,
      String currency,
      BigDecimal setupFee,
      int trialDays,
      LocalDate effectiveFrom,
      LocalDate effectiveTo,
      String status,
      boolean free,
      List<AppPlanBillingOptionData> billingOptions
  ) {}

  public record AppPlanEntitlementData(
      UUID id,
      String metricCode,
      MetricType metricType,
      BigDecimal limitValue,
      PeriodType periodType,
      EnforcementMode enforcementMode,
      boolean isEnabled
  ) {}

  /** Build from a full AppPlanResult (plan + versions + billingOptionsByVersion + entitlements). */
  public static AppPlanData from(
      AppPlan plan,
      List<AppPlanVersion> versions,
      Map<UUID, List<AppPlanBillingOption>> billingOptionsByVersion,
      List<AppPlanEntitlement> entitlements) {

    List<AppPlanVersionData> versionData = versions.stream().map(v -> {
      List<AppPlanBillingOption> opts =
          billingOptionsByVersion.getOrDefault(v.getId(), Collections.emptyList());
      List<AppPlanBillingOptionData> optionData = opts.stream()
          .map(o -> new AppPlanBillingOptionData(
              o.getBillingPeriod().name(),
              o.getBasePrice(),
              o.getDiscountPct(),
              o.isDefault()))
          .toList();
      return new AppPlanVersionData(
          v.getId(), v.getVersion(), v.getCurrency(),
          v.getSetupFee(), v.getTrialDays(),
          v.getEffectiveFrom(), v.getEffectiveTo(),
          v.getStatus().name(),
          opts.isEmpty(),   // free = no billing options
          optionData
      );
    }).toList();

    List<AppPlanEntitlementData> entitlementData = entitlements.stream()
        .map(e -> new AppPlanEntitlementData(
            e.getId(), e.getMetricCode(), e.getMetricType(),
            e.getLimitValue(), e.getPeriodType(), e.getEnforcementMode(), e.isEnabled()))
        .toList();

    return new AppPlanData(
        plan.getId(), plan.getClientAppId(),
        plan.getCode(), plan.getName(), plan.getDescription(),
        plan.getStatus().name(), plan.isPublic(), plan.getSortOrder(),
        versionData, entitlementData
    );
  }
}
