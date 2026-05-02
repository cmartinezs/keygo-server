package io.cmartinezs.keygo.supabase.billing.adapter;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;
import io.cmartinezs.keygo.supabase.billing.entity.AppPlanEntitlementEntity;
import io.cmartinezs.keygo.supabase.billing.entity.AppPlanVersionEntity;
import io.cmartinezs.keygo.supabase.billing.mapper.BillingPersistenceMapper;
import io.cmartinezs.keygo.supabase.billing.repository.AppPlanEntitlementJpaRepository;
import io.cmartinezs.keygo.supabase.billing.repository.AppPlanVersionJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Adapter: implements AppPlanEntitlementRepositoryPort using JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class AppPlanEntitlementRepositoryAdapter implements AppPlanEntitlementRepositoryPort {

  private final AppPlanEntitlementJpaRepository jpaRepo;
  private final AppPlanVersionJpaRepository versionRepo;

  public AppPlanEntitlementRepositoryAdapter(
      AppPlanEntitlementJpaRepository jpaRepo,
      AppPlanVersionJpaRepository versionRepo) {
    this.jpaRepo = jpaRepo;
    this.versionRepo = versionRepo;
  }

  @Override
  public List<AppPlanEntitlement> findByAppPlanVersionId(UUID appPlanVersionId) {
    return jpaRepo.findByAppPlanVersionId(appPlanVersionId)
        .stream().map(BillingPersistenceMapper::toDomain).toList();
  }

  @Override
  public void saveAll(List<AppPlanEntitlement> entitlements) {
    List<AppPlanEntitlementEntity> entities = entitlements.stream().map(e -> {
      AppPlanVersionEntity versionRef = versionRepo.getReferenceById(e.getAppPlanVersionId());
      return AppPlanEntitlementEntity.builder()
          .id(e.getId())
          .appPlanVersion(versionRef)
          .metricCode(e.getMetricCode())
          .metricType(e.getMetricType())
          .limitValue(e.getLimitValue())
          .periodType(e.getPeriodType())
          .enforcementMode(e.getEnforcementMode())
          .isEnabled(e.isEnabled())
          .build();
    }).toList();
    jpaRepo.saveAll(entities);
  }
}

