package io.cmartinezs.keygo.api.billing.response;

import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.billing.catalog.model.EnforcementMode;
import io.cmartinezs.keygo.domain.billing.catalog.model.MetricType;
import io.cmartinezs.keygo.domain.billing.catalog.model.PeriodType;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Response data for app plan catalog endpoints.
 */
public record AppPlanData(
    UUID id,
    UUID clientAppId,
    String code,
    String name,
    String description,
    SubscriberType subscriberType,
    String status,
    boolean isPublic,
    List<AppPlanVersionData> versions,
    List<AppPlanEntitlementData> entitlements
) {
  public record AppPlanVersionData(
      UUID id,
      String version,
      String currency,
      String billingPeriod,
      BigDecimal basePrice,
      BigDecimal setupFee,
      int trialDays,
      LocalDate effectiveFrom,
      LocalDate effectiveTo,
      String status
  ) {}

  public record AppPlanEntitlementData(
      UUID id,
      String metricCode,
      MetricType metricType,
      Long limitValue,
      PeriodType periodType,
      EnforcementMode enforcementMode,
      boolean isEnabled
  ) {}

  /** Solo para referencia de schema OpenAPI — no instanciar en lógica de negocio. */
  public static final class Response extends BaseResponse<AppPlanData> {}

  /** Solo para referencia de schema OpenAPI (lista). */
  public static final class ListResponse extends BaseResponse<List<AppPlanData>> {}

  public static AppPlanData from(AppPlan plan, List<AppPlanVersion> versions, List<AppPlanEntitlement> entitlements) {
    return new AppPlanData(
        plan.getId(),
        plan.getClientAppId(),
        plan.getCode(),
        plan.getName(),
        plan.getDescription(),
        plan.getSubscriberType(),
        plan.getStatus().name(),
        plan.isPublic(),
        versions.stream().map(v -> new AppPlanVersionData(
            v.getId(), v.getVersion(), v.getCurrency(),
            v.getBillingPeriod().name(), v.getBasePrice(), v.getSetupFee(),
            v.getTrialDays(), v.getEffectiveFrom(), v.getEffectiveTo(),
            v.getStatus().name()
        )).toList(),
        entitlements.stream().map(e -> new AppPlanEntitlementData(
            e.getId(), e.getMetricCode(), e.getMetricType(),
            e.getLimitValue(), e.getPeriodType(), e.getEnforcementMode(), e.isEnabled()
        )).toList()
    );
  }
}
