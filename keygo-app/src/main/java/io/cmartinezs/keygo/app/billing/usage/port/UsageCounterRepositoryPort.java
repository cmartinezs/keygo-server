package io.cmartinezs.keygo.app.billing.usage.port;

import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;

import java.util.Map;
import java.util.UUID;

/**
 * Port OUT — persistence contract for usage counters.
 * @author cmartinezs
 * @version 1.0
 */
public interface UsageCounterRepositoryPort {
  /** Returns current usage values for all metrics (metricCode -> usedValue). */
  Map<String, Long> getCurrentUsage(UUID clientAppId, SubscriberType subscriberType, UUID subscriberId);
  /** Atomically increments a usage counter. */
  void increment(UUID clientAppId, SubscriberType subscriberType, UUID subscriberId, String metricCode, long delta);
}

