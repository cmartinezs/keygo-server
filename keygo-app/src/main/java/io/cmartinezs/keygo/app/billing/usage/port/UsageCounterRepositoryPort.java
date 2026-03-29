package io.cmartinezs.keygo.app.billing.usage.port;

import java.util.Map;
import java.util.UUID;

/**
 * Port OUT — persistence contract for usage counters.
 * @author cmartinezs
 * @version 1.0
 */
public interface UsageCounterRepositoryPort {
  /** Returns current usage values for all metrics (metricCode -> usedValue). Tenant (B2B) lookup. */
  Map<String, Long> getCurrentUsageForTenant(UUID clientAppId, UUID tenantId);
  /** Returns current usage values for all metrics (metricCode -> usedValue). User (B2C) lookup. */
  Map<String, Long> getCurrentUsageForUser(UUID clientAppId, UUID userId);
  /** Atomically increments a usage counter for a tenant. */
  void incrementForTenant(UUID clientAppId, UUID tenantId, String metricCode, long delta);
  /** Atomically increments a usage counter for a user. */
  void incrementForUser(UUID clientAppId, UUID userId, String metricCode, long delta);
}
