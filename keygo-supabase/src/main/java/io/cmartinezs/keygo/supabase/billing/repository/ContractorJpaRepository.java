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
  Optional<ContractorEntity> findByPlatformUserId(UUID platformUserId);

  /**
   * Finds a contractor by the email of its linked PlatformUser.
   * Uses Spring Data JPA nested property traversal: platformUser → email.
   */
  Optional<ContractorEntity> findByPlatformUser_Email(String email);
}

