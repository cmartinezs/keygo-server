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
 */
public record CreateAppPlanRequest(
    String code,
    String name,
    String description,
    boolean isPublic,
    String version,
    BillingPeriod billingPeriod,
    BigDecimal basePrice,
    String currency,
    int trialDays,
    LocalDate effectiveFrom,
    List<EntitlementRequest> entitlements
) {
  public record EntitlementRequest(
      String metricCode,
      MetricType metricType,
      Long limitValue,
      PeriodType periodType,
      EnforcementMode enforcementMode,
      boolean isEnabled
  ) {}
}
