package io.cmartinezs.keygo.supabase.billing.adapter;

import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.supabase.billing.entity.AppContractEntity;
import io.cmartinezs.keygo.supabase.billing.mapper.BillingPersistenceMapper;
import io.cmartinezs.keygo.supabase.billing.repository.AppContractJpaRepository;
import io.cmartinezs.keygo.supabase.billing.repository.AppPlanVersionJpaRepository;
import io.cmartinezs.keygo.supabase.clientapp.repository.ClientAppJpaRepository;
import io.cmartinezs.keygo.supabase.tenant.repository.TenantJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: implements AppContractRepositoryPort using JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class AppContractRepositoryAdapter implements AppContractRepositoryPort {

  private final AppContractJpaRepository jpaRepo;
  private final ClientAppJpaRepository clientAppRepo;
  private final AppPlanVersionJpaRepository versionRepo;
  private final TenantJpaRepository tenantRepo;
  private final TenantUserJpaRepository tenantUserRepo;

  public AppContractRepositoryAdapter(
      AppContractJpaRepository jpaRepo,
      ClientAppJpaRepository clientAppRepo,
      AppPlanVersionJpaRepository versionRepo,
      TenantJpaRepository tenantRepo,
      TenantUserJpaRepository tenantUserRepo) {
    this.jpaRepo = jpaRepo;
    this.clientAppRepo = clientAppRepo;
    this.versionRepo = versionRepo;
    this.tenantRepo = tenantRepo;
    this.tenantUserRepo = tenantUserRepo;
  }

  @Override
  public AppContract save(AppContract contract) {
    AppContractEntity entity = toEntity(contract);
    return BillingPersistenceMapper.toDomain(jpaRepo.save(entity));
  }

  @Override
  public Optional<AppContract> findById(UUID id) {
    return jpaRepo.findById(id).map(BillingPersistenceMapper::toDomain);
  }

  @Override
  public Optional<AppContract> findByClientAppIdAndCompanySlug(UUID clientAppId, String companySlug) {
    return jpaRepo.findByClientAppIdAndCompanySlug(clientAppId, companySlug)
        .map(BillingPersistenceMapper::toDomain);
  }

  @Override
  public Optional<AppContract> findByClientAppIdAndContractorEmail(UUID clientAppId, String email) {
    return jpaRepo.findByClientAppIdAndContractorEmail(clientAppId, email)
        .map(BillingPersistenceMapper::toDomain);
  }

  @Override
  public boolean existsByClientAppIdAndCompanySlug(UUID clientAppId, String companySlug) {
    return jpaRepo.existsByClientAppIdAndCompanySlug(clientAppId, companySlug);
  }

  private AppContractEntity toEntity(AppContract c) {
    AppContractEntity.AppContractEntityBuilder builder = AppContractEntity.builder()
        .id(c.getId())
        .clientApp(clientAppRepo.getReferenceById(c.getClientAppId()))
        .selectedPlanVersion(versionRepo.getReferenceById(c.getSelectedPlanVersionId()))
        .billingPeriod(c.getBillingPeriod())
        .subscriberType(c.getSubscriberType())
        .status(c.getStatus())
        .contractorEmail(c.getContractorEmail())
        .contractorFirstName(c.getContractorFirstName())
        .contractorLastName(c.getContractorLastName())
        .companyName(c.getCompanyName())
        .companySlug(c.getCompanySlug())
        .companyTaxId(c.getCompanyTaxId())
        .companyAddress(c.getCompanyAddress())
        .emailVerifiedAt(c.getEmailVerifiedAt())
        .paymentVerifiedAt(c.getPaymentVerifiedAt())
        .expiresAt(c.getExpiresAt());

    if (c.getSubscriberTenantId() != null) {
      builder.subscriberTenant(tenantRepo.getReferenceById(c.getSubscriberTenantId()));
    }
    if (c.getSubscriberTenantUserId() != null) {
      builder.subscriberTenantUser(tenantUserRepo.getReferenceById(c.getSubscriberTenantUserId()));
    }
    return builder.build();
  }
}

