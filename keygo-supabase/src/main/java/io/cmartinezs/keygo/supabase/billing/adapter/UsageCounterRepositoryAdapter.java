package io.cmartinezs.keygo.supabase.billing.adapter;

import io.cmartinezs.keygo.app.billing.usage.port.UsageCounterRepositoryPort;
import io.cmartinezs.keygo.supabase.billing.repository.UsageCounterJpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter: implements UsageCounterRepositoryPort using JPA (billing model v2).
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
  public Map<String, BigDecimal> getCurrentUsageForContractor(UUID clientAppId, UUID contractorId) {
    OffsetDateTime now = OffsetDateTime.now();
    return jpaRepo
        .findByClientAppIdAndContractorIdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(
            clientAppId, contractorId, now, now)
        .stream()
        .collect(Collectors.toMap(e -> e.getMetricCode(), e -> e.getUsedValue()));
  }

  @Override
  public void incrementForContractor(
      UUID clientAppId, UUID contractorId, String metricCode, BigDecimal delta) {
    jpaRepo.incrementAtomic(clientAppId, contractorId, metricCode, delta);
  }
}
