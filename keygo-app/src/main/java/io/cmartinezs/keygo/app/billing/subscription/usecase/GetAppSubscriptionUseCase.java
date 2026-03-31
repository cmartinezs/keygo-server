package io.cmartinezs.keygo.app.billing.subscription.usecase;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;

import java.util.UUID;

/**
 * Use case: retrieve the active subscription for a Contractor toward a client app (billing model v2).
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

  /** Lookup subscription by Contractor. */
  public AppSubscription executeForContractor(UUID clientAppId, UUID contractorId) {
    return subscriptionRepo.findByClientAppIdAndContractorId(clientAppId, contractorId)
        .orElseThrow(() -> new IllegalArgumentException(
            "No active subscription found for contractor: " + contractorId));
  }
}
