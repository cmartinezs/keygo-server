package io.cmartinezs.keygo.app.billing.subscription.usecase;

import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Use case: mark a subscription for cancellation at period end (billing model v2).
 * @author cmartinezs
 * @version 1.0
 */
public class CancelAppSubscriptionUseCase {

  private final AppSubscriptionRepositoryPort subscriptionRepo;

  public CancelAppSubscriptionUseCase(AppSubscriptionRepositoryPort subscriptionRepo) {
    this.subscriptionRepo = subscriptionRepo;
  }

  /** Cancel by Contractor. */
  public AppSubscription executeForContractor(UUID clientAppId, UUID contractorId) {
    AppSubscription sub = subscriptionRepo.findByClientAppIdAndContractorId(clientAppId, contractorId)
        .orElseThrow(() -> new IllegalArgumentException("Subscription not found for contractor: " + contractorId));
    return cancel(sub);
  }

  private AppSubscription cancel(AppSubscription sub) {
    if (!sub.isActive()) {
      throw new IllegalStateException("Subscription is not active, cannot cancel: " + sub.getStatus());
    }
    sub.markCancelAtPeriodEnd(OffsetDateTime.now());
    return subscriptionRepo.save(sub);
  }
}
