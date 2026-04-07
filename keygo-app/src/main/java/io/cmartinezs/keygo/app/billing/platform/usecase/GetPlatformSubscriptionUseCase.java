package io.cmartinezs.keygo.app.billing.platform.usecase;

import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.billing.platform.exception.ContractorNotFoundException;
import io.cmartinezs.keygo.app.billing.subscription.exception.SubscriptionNotFoundException;
import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;

import java.util.UUID;

/**
 * Use case: retrieve the active platform subscription for a contractor (by platformUserId).
 *
 * @author cmartinezs
 * @version 1.0
 */
public class GetPlatformSubscriptionUseCase {

  private final ContractorRepositoryPort contractorRepo;
  private final AppSubscriptionRepositoryPort subscriptionRepo;

  public GetPlatformSubscriptionUseCase(
      ContractorRepositoryPort contractorRepo,
      AppSubscriptionRepositoryPort subscriptionRepo) {
    this.contractorRepo = contractorRepo;
    this.subscriptionRepo = subscriptionRepo;
  }

  public AppSubscription execute(UUID platformUserId) {
    var contractor = contractorRepo.findByPlatformUserId(platformUserId)
        .orElseThrow(() -> new ContractorNotFoundException(platformUserId));
    return subscriptionRepo.findPlatformSubscriptionByContractorId(contractor.getId())
        .orElseThrow(() -> new SubscriptionNotFoundException("contractorId", contractor.getId().toString()));
  }
}
