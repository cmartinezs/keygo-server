package io.cmartinezs.keygo.supabase.billing.adapter;

import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;
import io.cmartinezs.keygo.supabase.billing.entity.AppSubscriptionEntity;
import io.cmartinezs.keygo.supabase.billing.mapper.BillingPersistenceMapper;
import io.cmartinezs.keygo.supabase.billing.repository.AppContractJpaRepository;
import io.cmartinezs.keygo.supabase.billing.repository.AppPlanVersionJpaRepository;
import io.cmartinezs.keygo.supabase.billing.repository.AppSubscriptionJpaRepository;
import io.cmartinezs.keygo.supabase.billing.repository.ContractorJpaRepository;
import io.cmartinezs.keygo.supabase.clientapp.repository.ClientAppJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: implements AppSubscriptionRepositoryPort using JPA (billing model v2).
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class AppSubscriptionRepositoryAdapter implements AppSubscriptionRepositoryPort {

  private final AppSubscriptionJpaRepository jpaRepo;
  private final ClientAppJpaRepository clientAppRepo;
  private final AppPlanVersionJpaRepository versionRepo;
  private final AppContractJpaRepository contractRepo;
  private final ContractorJpaRepository contractorRepo;

  public AppSubscriptionRepositoryAdapter(
      AppSubscriptionJpaRepository jpaRepo,
      ClientAppJpaRepository clientAppRepo,
      AppPlanVersionJpaRepository versionRepo,
      AppContractJpaRepository contractRepo,
      ContractorJpaRepository contractorRepo) {
    this.jpaRepo = jpaRepo;
    this.clientAppRepo = clientAppRepo;
    this.versionRepo = versionRepo;
    this.contractRepo = contractRepo;
    this.contractorRepo = contractorRepo;
  }

  @Override
  public AppSubscription save(AppSubscription sub) {
    AppSubscriptionEntity.AppSubscriptionEntityBuilder builder = AppSubscriptionEntity.builder()
        .id(sub.getId())
        .clientApp(clientAppRepo.getReferenceById(sub.getClientAppId()))
        .appPlanVersion(versionRepo.getReferenceById(sub.getAppPlanVersionId()))
        .contractor(contractorRepo.getReferenceById(sub.getContractorId()))
        .status(sub.getStatus())
        .currentPeriodStart(sub.getCurrentPeriodStart())
        .currentPeriodEnd(sub.getCurrentPeriodEnd())
        .cancelAtPeriodEnd(sub.isCancelAtPeriodEnd())
        .cancelledAt(sub.getCancelledAt())
        .nextBillingAt(sub.getNextBillingAt())
        .autoRenew(sub.isAutoRenew());

    if (sub.getContractId() != null) {
      builder.contract(contractRepo.getReferenceById(sub.getContractId()));
    }
    return BillingPersistenceMapper.toDomain(jpaRepo.save(builder.build()));
  }

  @Override
  public Optional<AppSubscription> findByClientAppIdAndContractorId(UUID clientAppId, UUID contractorId) {
    return jpaRepo.findByClientAppIdAndContractorId(clientAppId, contractorId)
        .map(BillingPersistenceMapper::toDomain);
  }
}
