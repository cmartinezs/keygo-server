package io.cmartinezs.keygo.supabase.billing.adapter;

import io.cmartinezs.keygo.app.billing.contractor.port.ContractorUserRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorUser;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorUserRole;
import io.cmartinezs.keygo.supabase.billing.entity.ContractorUserEntity;
import io.cmartinezs.keygo.supabase.billing.repository.ContractorUserJpaRepository;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ContractorUserRepositoryAdapter implements ContractorUserRepositoryPort {

  private final ContractorUserJpaRepository jpaRepository;

  public ContractorUserRepositoryAdapter(ContractorUserJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public ContractorUser assign(UUID contractorId, UUID platformUserId, ContractorUserRole role) {
    ContractorUserEntity saved = jpaRepository.save(ContractorUserEntity.builder()
        .contractorId(contractorId)
        .platformUserId(platformUserId)
        .roleCode(role.name())
        .build());
    return ContractorUser.builder()
        .contractorId(saved.getContractorId())
        .platformUserId(saved.getPlatformUserId())
        .role(ContractorUserRole.valueOf(saved.getRoleCode()))
        .assignedAt(saved.getAssignedAt())
        .build();
  }

  @Override
  public boolean hasRole(UUID contractorId, UUID platformUserId, ContractorUserRole role) {
    return jpaRepository.existsByContractorIdAndPlatformUserIdAndRoleCode(
        contractorId, platformUserId, role.name());
  }
}
