package io.cmartinezs.keygo.supabase.billing.adapter;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanRepositoryPort;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanStatus;
import io.cmartinezs.keygo.supabase.billing.entity.AppPlanEntity;
import io.cmartinezs.keygo.supabase.billing.mapper.BillingPersistenceMapper;
import io.cmartinezs.keygo.supabase.billing.repository.AppPlanJpaRepository;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.clientapp.repository.ClientAppJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: implements AppPlanRepositoryPort using JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class AppPlanRepositoryAdapter implements AppPlanRepositoryPort {

  private final AppPlanJpaRepository jpaRepo;
  private final ClientAppJpaRepository clientAppRepo;

  public AppPlanRepositoryAdapter(AppPlanJpaRepository jpaRepo, ClientAppJpaRepository clientAppRepo) {
    this.jpaRepo = jpaRepo;
    this.clientAppRepo = clientAppRepo;
  }

  @Override
  public List<AppPlan> findPublicByClientAppId(UUID clientAppId) {
    return jpaRepo.findByClientAppIdAndIsPublicTrueAndStatusOrderBySortOrderAsc(clientAppId, AppPlanStatus.ACTIVE)
        .stream().map(BillingPersistenceMapper::toDomain).toList();
  }

  @Override
  public List<AppPlan> findAllByClientAppId(UUID clientAppId) {
    return jpaRepo.findByClientAppId(clientAppId)
        .stream().map(BillingPersistenceMapper::toDomain).toList();
  }

  @Override
  public Optional<AppPlan> findByClientAppIdAndCode(UUID clientAppId, String code) {
    return jpaRepo.findByClientAppIdAndCode(clientAppId, code)
        .map(BillingPersistenceMapper::toDomain);
  }

  @Override
  public boolean existsByClientAppIdAndCode(UUID clientAppId, String code) {
    return jpaRepo.existsByClientAppIdAndCode(clientAppId, code);
  }

  @Override
  public AppPlan save(AppPlan plan) {
    ClientAppEntity clientApp = clientAppRepo.getReferenceById(plan.getClientAppId());
    AppPlanEntity entity = AppPlanEntity.builder()
        .id(plan.getId())
        .clientApp(clientApp)
        .code(plan.getCode())
        .name(plan.getName())
        .description(plan.getDescription())
        .status(plan.getStatus())
        .isPublic(plan.isPublic())
        .sortOrder(plan.getSortOrder())
        .build();
    return BillingPersistenceMapper.toDomain(jpaRepo.save(entity));
  }
}
