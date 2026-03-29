package io.cmartinezs.keygo.app.billing.subscription.usecase;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;

import java.util.List;
import java.util.UUID;

/**
 * Result record for subscription use cases.
 */
record AppSubscriptionResult(AppSubscription subscription, AppPlanVersion planVersion, List<AppPlanEntitlement> entitlements) {}

/**
 * Use case: retrieve the active subscription for a tenant toward a client app.
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

  public AppSubscription execute(UUID clientAppId, SubscriberType subscriberType, UUID subscriberId) {
    return switch (subscriberType) {
      case TENANT -> subscriptionRepo.findByClientAppIdAndSubscriberTenantId(clientAppId, subscriberId)
          .orElseThrow(() -> new IllegalArgumentException(
              "No active subscription found for tenant: " + subscriberId));
      case TENANT_USER -> subscriptionRepo.findByClientAppIdAndSubscriberUserId(clientAppId, subscriberId)
          .orElseThrow(() -> new IllegalArgumentException(
              "No active subscription found for user: " + subscriberId));
    };
  }
}

