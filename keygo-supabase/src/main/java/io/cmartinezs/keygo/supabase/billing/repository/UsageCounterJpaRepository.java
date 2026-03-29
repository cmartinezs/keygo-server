package io.cmartinezs.keygo.supabase.billing.repository;

import io.cmartinezs.keygo.supabase.billing.entity.UsageCounterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface UsageCounterJpaRepository extends JpaRepository<UsageCounterEntity, UUID> {

  List<UsageCounterEntity> findByClientAppIdAndSubscriberTenantIdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(
      UUID clientAppId, UUID tenantId, OffsetDateTime now1, OffsetDateTime now2);

  List<UsageCounterEntity> findByClientAppIdAndSubscriberTenantUserIdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(
      UUID clientAppId, UUID userId, OffsetDateTime now1, OffsetDateTime now2);

  /** Atomic increment using native PostgreSQL UPDATE. */
  @Modifying
  @Transactional
  @Query(value = """
      UPDATE usage_counters
      SET used_value = used_value + :delta, updated_at = now()
      WHERE client_app_id = :appId
        AND metric_code = :metricCode
        AND (subscriber_tenant_id = :tenantId OR subscriber_tenant_user_id = :userId)
        AND period_start <= now() AND period_end >= now()
      """, nativeQuery = true)
  int incrementAtomic(
      @Param("appId") UUID appId,
      @Param("metricCode") String metricCode,
      @Param("tenantId") UUID tenantId,
      @Param("userId") UUID userId,
      @Param("delta") long delta);
}

