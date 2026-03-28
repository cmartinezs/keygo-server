package io.cmartinezs.keygo.supabase.tenant.repository;

import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
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

  /** Count tenants grouped by status (single GROUP BY query for dashboard). */
  @Query("SELECT t.status, COUNT(t) FROM TenantEntity t GROUP BY t.status")
  List<Object[]> countGroupByStatus();

  /** Count tenants created after the given cutoff. */
  long countByCreatedAtAfter(OffsetDateTime cutoff);

  /** Count tenants that have no client apps. */
  @Query("SELECT COUNT(t) FROM TenantEntity t WHERE t.id NOT IN (SELECT a.tenant.id FROM ClientAppEntity a)")
  long countTenantsWithoutApps();

  /** Count tenants that have no tenant users. */
  @Query("SELECT COUNT(t) FROM TenantEntity t WHERE t.id NOT IN (SELECT u.tenant.id FROM TenantUserEntity u)")
  long countTenantsWithoutUsers();

  /** Top N tenants by number of users. Returns [slug, name, count]. */
  @Query(value =
      "SELECT t.slug, t.name, COUNT(u.id) AS cnt " +
      "FROM tenants t LEFT JOIN tenant_users u ON u.tenant_id = t.id " +
      "GROUP BY t.slug, t.name ORDER BY cnt DESC LIMIT :limit",
      nativeQuery = true)
  List<Object[]> findTopTenantsByUserCount(int limit);
}

