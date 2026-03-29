package io.cmartinezs.keygo.app.billing.subscription.usecase;

import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Use case: mark a subscription for cancellation at the end of the current period.
 * @author cmartinezs
 * @version 1.0
 */
public class CancelAppSubscriptionUseCase {

  private final AppSubscriptionRepositoryPort subscriptionRepo;

  public CancelAppSubscriptionUseCase(AppSubscriptionRepositoryPort subscriptionRepo) {
    this.subscriptionRepo = subscriptionRepo;
  }

  public AppSubscription execute(UUID clientAppId, SubscriberType subscriberType, UUID subscriberId) {
    AppSubscription sub = findSubscription(clientAppId, subscriberType, subscriberId);
    if (!sub.isActive()) {
      throw new IllegalStateException("Subscription is not active, cannot cancel: " + sub.getStatus());
    }
    sub.markCancelAtPeriodEnd(OffsetDateTime.now());
    return subscriptionRepo.save(sub);
  }

  private AppSubscription findSubscription(UUID clientAppId, SubscriberType type, UUID subscriberId) {
    return switch (type) {
      case TENANT -> subscriptionRepo.findByClientAppIdAndSubscriberTenantId(clientAppId, subscriberId)
          .orElseThrow(() -> new IllegalArgumentException("Subscription not found for tenant: " + subscriberId));
      case TENANT_USER -> subscriptionRepo.findByClientAppIdAndSubscriberUserId(clientAppId, subscriberId)
          .orElseThrow(() -> new IllegalArgumentException("Subscription not found for user: " + subscriberId));
    };
  }
}

