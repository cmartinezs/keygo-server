package io.cmartinezs.keygo.supabase.billing.adapter;

import io.cmartinezs.keygo.app.billing.usage.port.UsageCounterRepositoryPort;
import io.cmartinezs.keygo.supabase.billing.repository.UsageCounterJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter: implements UsageCounterRepositoryPort using JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class UsageCounterRepositoryAdapter implements UsageCounterRepositoryPort {

  private final UsageCounterJpaRepository jpaRepo;

  public UsageCounterRepositoryAdapter(UsageCounterJpaRepository jpaRepo) {
    this.jpaRepo = jpaRepo;
  }

  @Override
  public Map<String, Long> getCurrentUsageForTenant(UUID clientAppId, UUID tenantId) {
    OffsetDateTime now = OffsetDateTime.now();
    return jpaRepo.findByClientAppIdAndSubscriberTenantIdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(
            clientAppId, tenantId, now, now)
        .stream()
        .collect(Collectors.toMap(e -> e.getMetricCode(), e -> e.getUsedValue()));
  }

  @Override
  public Map<String, Long> getCurrentUsageForUser(UUID clientAppId, UUID userId) {
    OffsetDateTime now = OffsetDateTime.now();
    return jpaRepo.findByClientAppIdAndSubscriberTenantUserIdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(
            clientAppId, userId, now, now)
        .stream()
        .collect(Collectors.toMap(e -> e.getMetricCode(), e -> e.getUsedValue()));
  }

  @Override
  public void incrementForTenant(UUID clientAppId, UUID tenantId, String metricCode, long delta) {
    jpaRepo.incrementAtomic(clientAppId, metricCode, tenantId, null, delta);
  }

  @Override
  public void incrementForUser(UUID clientAppId, UUID userId, String metricCode, long delta) {
    jpaRepo.incrementAtomic(clientAppId, metricCode, null, userId, delta);
  }
}
