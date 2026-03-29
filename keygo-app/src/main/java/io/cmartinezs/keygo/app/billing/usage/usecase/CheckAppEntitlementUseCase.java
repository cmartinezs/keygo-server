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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case: check whether an operation is allowed based on plan entitlements.
 * Backward compatible: if no subscription or entitlement is found, returns allowed=true.
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

  /** Check entitlement for a B2B tenant subscriber. */
  public EntitlementCheck executeForTenant(UUID clientAppId, UUID tenantId, String metricCode) {
    Optional<AppSubscription> sub = subscriptionRepo.findByClientAppIdAndSubscriberTenantId(clientAppId, tenantId);
    Map<String, Long> usage = sub.map(s -> usageRepo.getCurrentUsageForTenant(clientAppId, tenantId))
        .orElse(Map.of());
    return check(sub, usage, metricCode);
  }

  /** Check entitlement for a B2C user subscriber. */
  public EntitlementCheck executeForUser(UUID clientAppId, UUID userId, String metricCode) {
    Optional<AppSubscription> sub = subscriptionRepo.findByClientAppIdAndSubscriberUserId(clientAppId, userId);
    Map<String, Long> usage = sub.map(s -> usageRepo.getCurrentUsageForUser(clientAppId, userId))
        .orElse(Map.of());
    return check(sub, usage, metricCode);
  }

  private EntitlementCheck check(Optional<AppSubscription> subscriptionOpt, Map<String, Long> usage, String metricCode) {
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
          .currentValue(0)
          .limitValue(0L)
          .allowed(false)
          .build();
    }

    if (MetricType.BOOLEAN.equals(entitlement.getMetricType())) {
      return EntitlementCheck.builder()
          .metricCode(metricCode)
          .metricType(MetricType.BOOLEAN)
          .enforcementMode(entitlement.getEnforcementMode())
          .currentValue(0)
          .limitValue(null)
          .allowed(entitlement.isEnabled())
          .build();
    }

    long currentValue = usage.getOrDefault(metricCode, 0L);
    Long limitValue = entitlement.getLimitValue();

    boolean allowed = limitValue == null || currentValue < limitValue
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
