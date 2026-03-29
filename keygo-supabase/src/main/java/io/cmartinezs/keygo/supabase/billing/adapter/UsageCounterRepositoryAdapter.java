package io.cmartinezs.keygo.supabase.billing.adapter;

import io.cmartinezs.keygo.app.billing.usage.port.UsageCounterRepositoryPort;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;
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
  public Map<String, Long> getCurrentUsage(UUID clientAppId, SubscriberType subscriberType, UUID subscriberId) {
    OffsetDateTime now = OffsetDateTime.now();
    var counters = SubscriberType.TENANT.equals(subscriberType)
        ? jpaRepo.findByClientAppIdAndSubscriberTenantIdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(
            clientAppId, subscriberId, now, now)
        : jpaRepo.findByClientAppIdAndSubscriberTenantUserIdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(
            clientAppId, subscriberId, now, now);

    return counters.stream()
        .collect(Collectors.toMap(e -> e.getMetricCode(), e -> e.getUsedValue()));
  }

  @Override
  public void increment(UUID clientAppId, SubscriberType subscriberType, UUID subscriberId, String metricCode, long delta) {
    UUID tenantId = SubscriberType.TENANT.equals(subscriberType) ? subscriberId : null;
    UUID userId   = SubscriberType.TENANT_USER.equals(subscriberType) ? subscriberId : null;
    jpaRepo.incrementAtomic(clientAppId, metricCode, tenantId, userId, delta);
  }
}

