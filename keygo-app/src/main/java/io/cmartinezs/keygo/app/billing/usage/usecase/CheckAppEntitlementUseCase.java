package io.cmartinezs.keygo.app.billing.usage.usecase;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.app.billing.usage.port.UsageCounterRepositoryPort;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;
import io.cmartinezs.keygo.domain.billing.catalog.model.EnforcementMode;
import io.cmartinezs.keygo.domain.billing.catalog.model.MetricType;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;
import io.cmartinezs.keygo.domain.billing.usage.model.EntitlementCheck;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case: check whether an operation is allowed based on plan entitlements — billing model v2.
 * If no subscription or entitlement is found, returns allowed=true (backward compatible).
 * @author cmartinezs
 * @version 1.0
 */
public class CheckAppEntitlementUseCase {

  private final AppSubscriptionRepositoryPort subscriptionRepo;
  private final AppPlanVersionRepositoryPort versionRepo;
  private final AppPlanEntitlementRepositoryPort entitlementRepo;
  private final UsageCounterRepositoryPort usageRepo;

  public CheckAppEntitlementUseCase(
      AppSubscriptionRepositoryPort subscriptionRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppPlanEntitlementRepositoryPort entitlementRepo,
      UsageCounterRepositoryPort usageRepo) {
    this.subscriptionRepo = subscriptionRepo;
    this.versionRepo = versionRepo;
    this.entitlementRepo = entitlementRepo;
    this.usageRepo = usageRepo;
  }

  /** Check entitlement for a Contractor (billing model v2). */
  public EntitlementCheck executeForContractor(UUID clientAppId, UUID contractorId, String metricCode) {
    Optional<AppSubscription> sub = subscriptionRepo.findByClientAppIdAndContractorId(clientAppId, contractorId);
    Map<String, BigDecimal> usage =
        sub.map(s -> usageRepo.getCurrentUsageForContractor(clientAppId, contractorId))
        .orElse(Map.of());
    return check(sub, usage, metricCode);
  }

  private EntitlementCheck check(
      Optional<AppSubscription> subscriptionOpt, Map<String, BigDecimal> usage, String metricCode) {
    if (subscriptionOpt.isEmpty() || !subscriptionOpt.get().isActive()) {
      return EntitlementCheck.unlimited(metricCode);
    }

    AppSubscription subscription = subscriptionOpt.get();
    List<AppPlanEntitlement> entitlements = entitlementRepo.findByAppPlanVersionId(subscription.getAppPlanVersionId());

    AppPlanEntitlement entitlement = entitlements.stream()
        .filter(e -> e.getMetricCode().equals(metricCode))
        .findFirst()
        .orElse(null);

    if (entitlement == null) {
      return EntitlementCheck.unlimited(metricCode);
    }

    if (!entitlement.isEnabled()) {
      return EntitlementCheck.builder()
          .metricCode(metricCode)
          .metricType(entitlement.getMetricType())
          .enforcementMode(entitlement.getEnforcementMode())
          .currentValue(BigDecimal.ZERO)
          .limitValue(BigDecimal.ZERO)
          .allowed(false)
          .build();
    }

    if (MetricType.BOOLEAN.equals(entitlement.getMetricType())) {
      return EntitlementCheck.builder()
          .metricCode(metricCode)
          .metricType(MetricType.BOOLEAN)
          .enforcementMode(entitlement.getEnforcementMode())
          .currentValue(BigDecimal.ZERO)
          .limitValue(null)
          .allowed(entitlement.isEnabled())
          .build();
    }

    BigDecimal currentValue = usage.getOrDefault(metricCode, BigDecimal.ZERO);
    BigDecimal limitValue = entitlement.getLimitValue();
    boolean allowed = limitValue == null || currentValue.compareTo(limitValue) < 0
        || EnforcementMode.SOFT.equals(entitlement.getEnforcementMode());

    return EntitlementCheck.builder()
        .metricCode(metricCode)
        .metricType(entitlement.getMetricType())
        .enforcementMode(entitlement.getEnforcementMode())
        .currentValue(currentValue)
        .limitValue(limitValue)
        .allowed(allowed)
        .build();
  }
}
