package io.cmartinezs.keygo.app.billing.platform.usecase;

import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.billing.platform.exception.ContractorNotFoundException;
import io.cmartinezs.keygo.app.billing.subscription.exception.SubscriptionInvalidStateException;
import io.cmartinezs.keygo.app.billing.subscription.exception.SubscriptionNotFoundException;
import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Use case: mark the platform subscription for cancellation at period end.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class CancelPlatformSubscriptionUseCase {

  private final ContractorRepositoryPort contractorRepo;
  private final AppSubscriptionRepositoryPort subscriptionRepo;

  public CancelPlatformSubscriptionUseCase(
      ContractorRepositoryPort contractorRepo,
      AppSubscriptionRepositoryPort subscriptionRepo) {
    this.contractorRepo = contractorRepo;
    this.subscriptionRepo = subscriptionRepo;
  }

  public AppSubscription execute(UUID platformUserId) {
    var contractor = contractorRepo.findByPlatformUserId(platformUserId)
        .orElseThrow(() -> new ContractorNotFoundException(platformUserId));

    var subscription = subscriptionRepo.findPlatformSubscriptionByContractorId(contractor.getId())
        .orElseThrow(() -> new SubscriptionNotFoundException("contractorId", contractor.getId().toString()));

    if (!subscription.isActive()) {
      throw new SubscriptionInvalidStateException(subscription.getStatus().name());
    }

    subscription.markCancelAtPeriodEnd(OffsetDateTime.now());
    return subscriptionRepo.save(subscription);
  }
}
