package io.cmartinezs.keygo.supabase.tenant.repository;

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

  /**
   * Find a tenant entity by its unique slug.
   * Busca una entidad tenant por su slug único.
   *
   * @param slug the slug to search by
   * @return an Optional with the found entity, or empty if not found
   */
  Optional<TenantEntity> findBySlug(String slug);

  /**
   * Check whether a tenant with the given slug already exists.
   * Verifica si ya existe un tenant con el slug dado.
   *
   * @param slug the slug to check
   * @return true if a tenant with this slug exists
   */
  boolean existsBySlug(String slug);
}

