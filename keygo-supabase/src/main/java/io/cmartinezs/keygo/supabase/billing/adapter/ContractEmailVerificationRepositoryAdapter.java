package io.cmartinezs.keygo.supabase.billing.adapter;

import io.cmartinezs.keygo.app.billing.contracting.port.ContractEmailVerificationRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractEmailVerification;
import io.cmartinezs.keygo.supabase.billing.entity.ContractEmailVerificationEntity;
import io.cmartinezs.keygo.supabase.billing.repository.AppContractJpaRepository;
import io.cmartinezs.keygo.supabase.billing.repository.ContractEmailVerificationJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ContractEmailVerificationRepositoryAdapter
    implements ContractEmailVerificationRepositoryPort {

  private final ContractEmailVerificationJpaRepository jpaRepository;
  private final AppContractJpaRepository contractJpaRepository;

  public ContractEmailVerificationRepositoryAdapter(
      ContractEmailVerificationJpaRepository jpaRepository,
      AppContractJpaRepository contractJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.contractJpaRepository = contractJpaRepository;
  }

  @Override
  @Transactional
  public ContractEmailVerification upsert(ContractEmailVerification verification) {
    ContractEmailVerificationEntity entity = jpaRepository.findByContract_Id(verification.getContractId())
        .orElseGet(() -> ContractEmailVerificationEntity.builder()
            .contract(contractJpaRepository.getReferenceById(verification.getContractId()))
            .build());

    entity.setCode(verification.getCode());
    entity.setExpiresAt(verification.getExpiresAt());
    entity.setUsedAt(verification.getUsedAt());

    return toDomain(jpaRepository.save(entity));
  }

  @Override
  public Optional<ContractEmailVerification> findByContractId(UUID contractId) {
    return jpaRepository.findByContract_Id(contractId).map(this::toDomain);
  }

  @Override
  @Transactional
  public void markUsed(ContractEmailVerification verification) {
    jpaRepository.markUsedById(verification.getId(), verification.getUsedAt());
  }

  private ContractEmailVerification toDomain(ContractEmailVerificationEntity entity) {
    return ContractEmailVerification.builder()
        .id(entity.getId())
        .contractId(entity.getContract().getId())
        .code(entity.getCode())
        .expiresAt(entity.getExpiresAt())
        .usedAt(entity.getUsedAt())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }
}
