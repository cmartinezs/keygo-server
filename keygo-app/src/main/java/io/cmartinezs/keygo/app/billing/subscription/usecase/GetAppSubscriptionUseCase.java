package io.cmartinezs.keygo.app.billing.subscription.usecase;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;

import java.util.List;
import java.util.UUID;

/**
 * Result record for subscription use cases.
 */
record AppSubscriptionResult(AppSubscription subscription, AppPlanVersion planVersion, List<AppPlanEntitlement> entitlements) {}

/**
 * Use case: retrieve the active subscription for a subscriber toward a client app.
 * Supports both B2B (tenantId) and B2C (userId) lookups.
 * @author cmartinezs
 * @version 1.0
 */
public class GetAppSubscriptionUseCase {

  private final AppSubscriptionRepositoryPort subscriptionRepo;
  private final AppPlanVersionRepositoryPort versionRepo;
  private final AppPlanEntitlementRepositoryPort entitlementRepo;

  public GetAppSubscriptionUseCase(
      AppSubscriptionRepositoryPort subscriptionRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppPlanEntitlementRepositoryPort entitlementRepo) {
    this.subscriptionRepo = subscriptionRepo;
    this.versionRepo = versionRepo;
    this.entitlementRepo = entitlementRepo;
  }

  /** Lookup by tenant (B2B). */
  public AppSubscription executeForTenant(UUID clientAppId, UUID tenantId) {
    return subscriptionRepo.findByClientAppIdAndSubscriberTenantId(clientAppId, tenantId)
        .orElseThrow(() -> new IllegalArgumentException(
            "No active subscription found for tenant: " + tenantId));
  }

  /** Lookup by user (B2C). */
  public AppSubscription executeForUser(UUID clientAppId, UUID userId) {
    return subscriptionRepo.findByClientAppIdAndSubscriberUserId(clientAppId, userId)
        .orElseThrow(() -> new IllegalArgumentException(
            "No active subscription found for user: " + userId));
  }
}
