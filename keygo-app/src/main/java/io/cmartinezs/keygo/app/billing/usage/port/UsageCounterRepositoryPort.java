package io.cmartinezs.keygo.app.billing.usage.port;

import java.util.Map;
import java.util.UUID;

/**
 * Port OUT — persistence contract for usage counters (billing model v2, contractor-centric).
 * @author cmartinezs
 * @version 1.0
 */
public interface UsageCounterRepositoryPort {
  /** Returns current usage values for all metrics (metricCode -> usedValue) for a Contractor. */
  Map<String, Long> getCurrentUsageForContractor(UUID clientAppId, UUID contractorId);
  /** Atomically increments a usage counter for a Contractor. */
  void incrementForContractor(UUID clientAppId, UUID contractorId, String metricCode, long delta);
}
