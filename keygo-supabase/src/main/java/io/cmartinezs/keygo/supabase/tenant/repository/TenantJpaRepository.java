package io.cmartinezs.keygo.supabase.tenant.repository;

import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for TenantEntity.
 * Repositorio Spring Data JPA para TenantEntity.
 *
 * @author cmartinezs
 * @version 1.0
 */
public interface TenantJpaRepository
    extends JpaRepository<TenantEntity, UUID>, JpaSpecificationExecutor<TenantEntity> {

  Optional<TenantEntity> findBySlug(String slug);

  boolean existsBySlug(String slug);

  /** Count tenants with the given status. */
  long countByStatus(TenantStatus status);
}

