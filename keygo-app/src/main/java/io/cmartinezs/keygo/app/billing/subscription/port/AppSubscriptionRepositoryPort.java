package io.cmartinezs.keygo.app.billing.subscription.port;

import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;

import java.util.Optional;
import java.util.UUID;

/**
 * Port OUT — persistence contract for AppSubscription.
 * @author cmartinezs
 * @version 1.0
 */
public interface AppSubscriptionRepositoryPort {
  AppSubscription save(AppSubscription subscription);
  Optional<AppSubscription> findByClientAppIdAndSubscriberTenantId(UUID clientAppId, UUID tenantId);
  Optional<AppSubscription> findByClientAppIdAndSubscriberUserId(UUID clientAppId, UUID userId);
}

