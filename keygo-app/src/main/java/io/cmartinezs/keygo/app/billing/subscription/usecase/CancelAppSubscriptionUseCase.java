package io.cmartinezs.keygo.app.billing.subscription.usecase;

import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;

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

  /** Cancel by tenant (B2B). */
  public AppSubscription executeForTenant(UUID clientAppId, UUID tenantId) {
    AppSubscription sub = subscriptionRepo.findByClientAppIdAndSubscriberTenantId(clientAppId, tenantId)
        .orElseThrow(() -> new IllegalArgumentException("Subscription not found for tenant: " + tenantId));
    return cancel(sub);
  }

  /** Cancel by user (B2C). */
  public AppSubscription executeForUser(UUID clientAppId, UUID userId) {
    AppSubscription sub = subscriptionRepo.findByClientAppIdAndSubscriberUserId(clientAppId, userId)
        .orElseThrow(() -> new IllegalArgumentException("Subscription not found for user: " + userId));
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
