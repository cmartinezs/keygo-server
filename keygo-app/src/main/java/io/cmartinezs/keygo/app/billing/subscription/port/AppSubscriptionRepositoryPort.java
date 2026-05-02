package io.cmartinezs.keygo.app.billing.subscription.port;

import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;

import java.util.Optional;
import java.util.UUID;

/**
 * Port OUT — persistence contract for AppSubscription (billing model v2).
 * @author cmartinezs
 * @version 1.0
 */
public interface AppSubscriptionRepositoryPort {
  AppSubscription save(AppSubscription subscription);
  Optional<AppSubscription> findByClientAppIdAndContractorId(UUID clientAppId, UUID contractorId);

  /** Platform subscription: WHERE client_app_id IS NULL AND contractor_id = ? */
  Optional<AppSubscription> findPlatformSubscriptionByContractorId(UUID contractorId);
}
