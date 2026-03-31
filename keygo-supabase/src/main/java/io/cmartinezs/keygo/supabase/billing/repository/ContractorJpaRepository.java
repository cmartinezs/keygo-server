package io.cmartinezs.keygo.supabase.billing.repository;

import io.cmartinezs.keygo.supabase.billing.entity.ContractorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for contractors table.
 * @author cmartinezs
 * @version 1.0
 */
public interface ContractorJpaRepository extends JpaRepository<ContractorEntity, UUID> {
  Optional<ContractorEntity> findByTenantUserId(UUID tenantUserId);
}

