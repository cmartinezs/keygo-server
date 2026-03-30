package io.cmartinezs.keygo.supabase.billing.adapter;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanBillingOptionRepositoryPort;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanBillingOption;
import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;
import io.cmartinezs.keygo.supabase.billing.entity.AppPlanBillingOptionEntity;
import io.cmartinezs.keygo.supabase.billing.entity.AppPlanVersionEntity;
import io.cmartinezs.keygo.supabase.billing.mapper.BillingPersistenceMapper;
import io.cmartinezs.keygo.supabase.billing.repository.AppPlanBillingOptionJpaRepository;
import io.cmartinezs.keygo.supabase.billing.repository.AppPlanVersionJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: implements AppPlanBillingOptionRepositoryPort using JPA.
 * <p>
 * A plan version may have zero billing options (= free plan) or one or more
 * options (each representing a billing period with its price).
 *
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class AppPlanBillingOptionRepositoryAdapter implements AppPlanBillingOptionRepositoryPort {

  private final AppPlanBillingOptionJpaRepository jpaRepo;
  private final AppPlanVersionJpaRepository versionRepo;

  public AppPlanBillingOptionRepositoryAdapter(
      AppPlanBillingOptionJpaRepository jpaRepo,
      AppPlanVersionJpaRepository versionRepo) {
    this.jpaRepo = jpaRepo;
    this.versionRepo = versionRepo;
  }

  @Override
  public List<AppPlanBillingOption> findByAppPlanVersionId(UUID appPlanVersionId) {
    return jpaRepo.findByAppPlanVersionId(appPlanVersionId)
        .stream().map(BillingPersistenceMapper::toDomain).toList();
  }

  @Override
  public Optional<AppPlanBillingOption> findByAppPlanVersionIdAndBillingPeriod(
      UUID appPlanVersionId, BillingPeriod billingPeriod) {
    return jpaRepo.findByAppPlanVersionIdAndBillingPeriod(appPlanVersionId, billingPeriod)
        .map(BillingPersistenceMapper::toDomain);
  }

  @Override
  public void saveAll(List<AppPlanBillingOption> options) {
    List<AppPlanBillingOptionEntity> entities = options.stream().map(o -> {
      AppPlanVersionEntity versionRef = versionRepo.getReferenceById(o.getAppPlanVersionId());
      return AppPlanBillingOptionEntity.builder()
          .id(o.getId())
          .appPlanVersion(versionRef)
          .billingPeriod(o.getBillingPeriod())
          .basePrice(o.getBasePrice())
          .discountPct(o.getDiscountPct())
          .isDefault(o.isDefault())
          .build();
    }).toList();
    jpaRepo.saveAll(entities);
  }
}

