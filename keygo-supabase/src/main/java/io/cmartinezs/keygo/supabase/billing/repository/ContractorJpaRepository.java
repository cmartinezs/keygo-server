package io.cmartinezs.keygo.supabase.billing.repository;

import io.cmartinezs.keygo.supabase.billing.entity.ContractorEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * JPA repository for contractors table.
 * @author cmartinezs
 * @version 1.0
 */
public interface ContractorJpaRepository extends JpaRepository<ContractorEntity, UUID> {
  @Query("""
      SELECT DISTINCT c
      FROM ContractorEntity c
      LEFT JOIN c.primaryContactPlatformUser pcp
      LEFT JOIN ContractorUserEntity cu ON cu.contractorId = c.id
      WHERE pcp.id = :platformUserId
         OR cu.platformUserId = :platformUserId
      """)
  Optional<ContractorEntity> findByAssociatedPlatformUserId(UUID platformUserId);

  /**
   * Resolves a contractor by an associated platform user email, either as primary contact or
   * contractor user.
   */
  @Query("""
      SELECT DISTINCT c
      FROM ContractorEntity c
      LEFT JOIN c.primaryContactPlatformUser pcp
      LEFT JOIN ContractorUserEntity cu ON cu.contractorId = c.id
      LEFT JOIN cu.platformUser pu
      WHERE pcp.email = :email
         OR pu.email = :email
      """)
  Optional<ContractorEntity> findByAssociatedPlatformUserEmail(String email);
}

