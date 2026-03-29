package io.cmartinezs.keygo.app.billing.usage.usecase;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.app.billing.usage.port.UsageCounterRepositoryPort;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;
import io.cmartinezs.keygo.domain.billing.catalog.model.EnforcementMode;
import io.cmartinezs.keygo.domain.billing.catalog.model.MetricType;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;
import io.cmartinezs.keygo.domain.billing.usage.model.EntitlementCheck;

import java.util.List;
import java.util.Map;
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

  public EntitlementCheck execute(UUID clientAppId, SubscriberType subscriberType, UUID subscriberId, String metricCode) {
    // Find active subscription
    var subscriptionOpt = switch (subscriberType) {
      case TENANT -> subscriptionRepo.findByClientAppIdAndSubscriberTenantId(clientAppId, subscriberId);
      case TENANT_USER -> subscriptionRepo.findByClientAppIdAndSubscriberUserId(clientAppId, subscriberId);
    };

    if (subscriptionOpt.isEmpty() || !subscriptionOpt.get().isActive()) {
      // No active subscription → unlimited (backward compatible)
      return EntitlementCheck.unlimited(metricCode);
    }

    AppSubscription subscription = subscriptionOpt.get();
    List<AppPlanEntitlement> entitlements = entitlementRepo.findByAppPlanVersionId(subscription.getAppPlanVersionId());

    AppPlanEntitlement entitlement = entitlements.stream()
        .filter(e -> e.getMetricCode().equals(metricCode))
        .findFirst()
        .orElse(null);

    if (entitlement == null) {
      // No entitlement defined for this metric → unlimited
      return EntitlementCheck.unlimited(metricCode);
    }

    if (!entitlement.isEnabled()) {
      // Feature is explicitly disabled
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

    // QUOTA or RATE — check usage
    Map<String, Long> usage = usageRepo.getCurrentUsage(clientAppId, subscriberType, subscriberId);
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

