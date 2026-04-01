package io.cmartinezs.keygo.app.billing.subscription.usecase;

import io.cmartinezs.keygo.app.billing.subscription.exception.SubscriptionInvalidStateException;
import io.cmartinezs.keygo.app.billing.subscription.exception.SubscriptionNotFoundException;
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
        .orElseThrow(() -> new SubscriptionNotFoundException("contractorId", contractorId.toString()));
    return cancel(sub);
  }

  private AppSubscription cancel(AppSubscription sub) {
    if (!sub.isActive()) {
      throw new SubscriptionInvalidStateException(sub.getStatus().name());
    }
    sub.markCancelAtPeriodEnd(OffsetDateTime.now());
    return subscriptionRepo.save(sub);
  }
}
