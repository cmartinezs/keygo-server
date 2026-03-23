package io.cmartinezs.keygo.supabase.user.repository;

import io.cmartinezs.keygo.supabase.user.entity.EmailVerificationEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for EmailVerificationEntity.
 * <p>Repositorio JPA para EmailVerificationEntity.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerificationEntity, UUID> {

  /**
   * Find the most recent verification for a given tenant user, ordered by creation date descending.
   * <p>Busca la verificación más reciente para un usuario de tenant, ordenada por fecha de creación desc.
   */
  Optional<EmailVerificationEntity> findTopByTenantUserOrderByCreatedAtDesc(TenantUserEntity tenantUser);
}

