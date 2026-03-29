package io.cmartinezs.keygo.supabase.billing.adapter;

import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;
import io.cmartinezs.keygo.supabase.billing.entity.AppSubscriptionEntity;
import io.cmartinezs.keygo.supabase.billing.mapper.BillingPersistenceMapper;
import io.cmartinezs.keygo.supabase.billing.repository.AppContractJpaRepository;
import io.cmartinezs.keygo.supabase.billing.repository.AppPlanVersionJpaRepository;
import io.cmartinezs.keygo.supabase.billing.repository.AppSubscriptionJpaRepository;
import io.cmartinezs.keygo.supabase.clientapp.repository.ClientAppJpaRepository;
import io.cmartinezs.keygo.supabase.tenant.repository.TenantJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: implements AppSubscriptionRepositoryPort using JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class AppSubscriptionRepositoryAdapter implements AppSubscriptionRepositoryPort {

  private final AppSubscriptionJpaRepository jpaRepo;
  private final ClientAppJpaRepository clientAppRepo;
  private final AppPlanVersionJpaRepository versionRepo;
  private final AppContractJpaRepository contractRepo;
  private final TenantJpaRepository tenantRepo;
  private final TenantUserJpaRepository tenantUserRepo;

  public AppSubscriptionRepositoryAdapter(
      AppSubscriptionJpaRepository jpaRepo,
      ClientAppJpaRepository clientAppRepo,
      AppPlanVersionJpaRepository versionRepo,
      AppContractJpaRepository contractRepo,
      TenantJpaRepository tenantRepo,
      TenantUserJpaRepository tenantUserRepo) {
    this.jpaRepo = jpaRepo;
    this.clientAppRepo = clientAppRepo;
    this.versionRepo = versionRepo;
    this.contractRepo = contractRepo;
    this.tenantRepo = tenantRepo;
    this.tenantUserRepo = tenantUserRepo;
  }

  @Override
  public AppSubscription save(AppSubscription sub) {
    AppSubscriptionEntity.AppSubscriptionEntityBuilder builder = AppSubscriptionEntity.builder()
        .id(sub.getId())
        .clientApp(clientAppRepo.getReferenceById(sub.getClientAppId()))
        .appPlanVersion(versionRepo.getReferenceById(sub.getAppPlanVersionId()))
        .subscriberType(sub.getSubscriberType())
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
    if (sub.getSubscriberTenantId() != null) {
      builder.subscriberTenant(tenantRepo.getReferenceById(sub.getSubscriberTenantId()));
    }
    if (sub.getSubscriberTenantUserId() != null) {
      builder.subscriberTenantUser(tenantUserRepo.getReferenceById(sub.getSubscriberTenantUserId()));
    }
    return BillingPersistenceMapper.toDomain(jpaRepo.save(builder.build()));
  }

  @Override
  public Optional<AppSubscription> findByClientAppIdAndSubscriberTenantId(UUID clientAppId, UUID tenantId) {
    return jpaRepo.findByClientAppIdAndSubscriberTenantId(clientAppId, tenantId)
        .map(BillingPersistenceMapper::toDomain);
  }

  @Override
  public Optional<AppSubscription> findByClientAppIdAndSubscriberUserId(UUID clientAppId, UUID userId) {
    return jpaRepo.findByClientAppIdAndSubscriberTenantUserId(clientAppId, userId)
        .map(BillingPersistenceMapper::toDomain);
  }
}

