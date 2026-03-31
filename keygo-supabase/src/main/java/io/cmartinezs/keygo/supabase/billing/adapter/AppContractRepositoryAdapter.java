package io.cmartinezs.keygo.supabase.billing.adapter;

import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.supabase.billing.entity.AppContractEntity;
import io.cmartinezs.keygo.supabase.billing.mapper.BillingPersistenceMapper;
import io.cmartinezs.keygo.supabase.billing.repository.AppContractJpaRepository;
import io.cmartinezs.keygo.supabase.billing.repository.AppPlanVersionJpaRepository;
import io.cmartinezs.keygo.supabase.billing.repository.ContractorJpaRepository;
import io.cmartinezs.keygo.supabase.clientapp.repository.ClientAppJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: implements AppContractRepositoryPort using JPA (billing model v2).
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class AppContractRepositoryAdapter implements AppContractRepositoryPort {

  private final AppContractJpaRepository jpaRepo;
  private final ClientAppJpaRepository clientAppRepo;
  private final AppPlanVersionJpaRepository versionRepo;
  private final ContractorJpaRepository contractorRepo;

  public AppContractRepositoryAdapter(
      AppContractJpaRepository jpaRepo,
      ClientAppJpaRepository clientAppRepo,
      AppPlanVersionJpaRepository versionRepo,
      ContractorJpaRepository contractorRepo) {
    this.jpaRepo = jpaRepo;
    this.clientAppRepo = clientAppRepo;
    this.versionRepo = versionRepo;
    this.contractorRepo = contractorRepo;
  }

  @Override
  public AppContract save(AppContract contract) {
    return BillingPersistenceMapper.toDomain(jpaRepo.save(toEntity(contract)));
  }

  @Override
  public Optional<AppContract> findById(UUID id) {
    return jpaRepo.findById(id).map(BillingPersistenceMapper::toDomain);
  }

  @Override
  public Optional<AppContract> findByClientAppIdAndContractorEmail(UUID clientAppId, String email) {
    return jpaRepo.findByClientAppIdAndContractorEmail(clientAppId, email)
        .map(BillingPersistenceMapper::toDomain);
  }

  private AppContractEntity toEntity(AppContract c) {
    AppContractEntity.AppContractEntityBuilder builder = AppContractEntity.builder()
        .id(c.getId())
        .clientApp(clientAppRepo.getReferenceById(c.getClientAppId()))
        .selectedPlanVersion(versionRepo.getReferenceById(c.getSelectedPlanVersionId()))
        .billingPeriod(c.getBillingPeriod())
        .status(c.getStatus())
        .contractorEmail(c.getContractorEmail())
        .contractorFirstName(c.getContractorFirstName())
        .contractorLastName(c.getContractorLastName())
        .companyName(c.getCompanyName())
        .companyTaxId(c.getCompanyTaxId())
        .companyAddress(c.getCompanyAddress())
        .verificationCode(c.getVerificationCode())
        .verificationCodeExpiresAt(c.getVerificationCodeExpiresAt())
        .emailVerifiedAt(c.getEmailVerifiedAt())
        .paymentVerifiedAt(c.getPaymentVerifiedAt())
        .expiresAt(c.getExpiresAt());

    if (c.getContractorId() != null) {
      builder.contractor(contractorRepo.getReferenceById(c.getContractorId()));
    }
    return builder.build();
  }
}
