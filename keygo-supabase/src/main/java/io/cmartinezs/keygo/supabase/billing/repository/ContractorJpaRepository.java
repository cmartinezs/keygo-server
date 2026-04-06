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

  /**
   * Finds a contractor by the email and tenant of its linked TenantUser.
   * Uses Spring Data JPA nested property traversal: tenantUser → tenant.id + email.
   */
  Optional<ContractorEntity> findByTenantUser_Tenant_IdAndTenantUser_Email(UUID tenantId, String email);
}

