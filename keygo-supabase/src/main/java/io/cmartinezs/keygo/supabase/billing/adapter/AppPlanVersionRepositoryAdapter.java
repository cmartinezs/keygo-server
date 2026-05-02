package io.cmartinezs.keygo.supabase.billing.adapter;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersionStatus;
import io.cmartinezs.keygo.supabase.billing.entity.AppPlanEntity;
import io.cmartinezs.keygo.supabase.billing.entity.AppPlanVersionEntity;
import io.cmartinezs.keygo.supabase.billing.mapper.BillingPersistenceMapper;
import io.cmartinezs.keygo.supabase.billing.repository.AppPlanJpaRepository;
import io.cmartinezs.keygo.supabase.billing.repository.AppPlanVersionJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: implements AppPlanVersionRepositoryPort using JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class AppPlanVersionRepositoryAdapter implements AppPlanVersionRepositoryPort {

  private final AppPlanVersionJpaRepository jpaRepo;
  private final AppPlanJpaRepository planRepo;

  public AppPlanVersionRepositoryAdapter(AppPlanVersionJpaRepository jpaRepo, AppPlanJpaRepository planRepo) {
    this.jpaRepo = jpaRepo;
    this.planRepo = planRepo;
  }

  @Override
  public List<AppPlanVersion> findActiveByAppPlanId(UUID appPlanId) {
    return jpaRepo.findByAppPlanIdAndStatus(appPlanId, AppPlanVersionStatus.ACTIVE)
        .stream().map(BillingPersistenceMapper::toDomain).toList();
  }

  @Override
  public Optional<AppPlanVersion> findById(UUID id) {
    return jpaRepo.findById(id).map(BillingPersistenceMapper::toDomain);
  }

  @Override
  public AppPlanVersion save(AppPlanVersion version) {
    AppPlanEntity planRef = planRepo.getReferenceById(version.getAppPlanId());
    AppPlanVersionEntity entity = AppPlanVersionEntity.builder()
        .id(version.getId())
        .appPlan(planRef)
        .version(version.getVersion())
        .currency(version.getCurrency())
        .setupFee(version.getSetupFee())
        .trialDays(version.getTrialDays())
        .effectiveFrom(version.getEffectiveFrom())
        .effectiveTo(version.getEffectiveTo())
        .status(version.getStatus())
        .build();
    return BillingPersistenceMapper.toDomain(jpaRepo.save(entity));
  }

  @Override
  public void saveAll(List<AppPlanVersion> versions) {
    versions.forEach(this::save);
  }
}

