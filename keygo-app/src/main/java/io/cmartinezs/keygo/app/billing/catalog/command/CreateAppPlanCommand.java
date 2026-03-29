package io.cmartinezs.keygo.app.billing.catalog.command;

import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;
import io.cmartinezs.keygo.domain.billing.catalog.model.EnforcementMode;
import io.cmartinezs.keygo.domain.billing.catalog.model.MetricType;
import io.cmartinezs.keygo.domain.billing.catalog.model.PeriodType;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Command to create a new billing plan with its first version and entitlements.
 */
public record CreateAppPlanCommand(
    UUID clientAppId,
    String code,
    String name,
    String description,
    SubscriberType subscriberType,
    boolean isPublic,
    String version,
    BillingPeriod billingPeriod,
    BigDecimal basePrice,
    String currency,
    int trialDays,
    LocalDate effectiveFrom,
    List<EntitlementDef> entitlements
) {
  public record EntitlementDef(
      String metricCode,
      MetricType metricType,
      Long limitValue,
      PeriodType periodType,
      EnforcementMode enforcementMode,
      boolean isEnabled
  ) {}
}

