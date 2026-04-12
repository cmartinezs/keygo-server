package io.cmartinezs.keygo.supabase.billing.repository;

import io.cmartinezs.keygo.supabase.billing.entity.ContractorUserEntity;
import io.cmartinezs.keygo.supabase.billing.entity.ContractorUserKey;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractorUserJpaRepository extends JpaRepository<ContractorUserEntity, ContractorUserKey> {
  boolean existsByContractorIdAndPlatformUserIdAndRoleCode(UUID contractorId, UUID platformUserId, String roleCode);
}
