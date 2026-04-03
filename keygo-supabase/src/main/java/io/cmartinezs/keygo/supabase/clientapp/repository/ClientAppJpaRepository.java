package io.cmartinezs.keygo.supabase.clientapp.repository;

import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for ClientAppEntity.
 * <p>Repositorio Spring Data JPA para ClientAppEntity.
 * @author cmartinezs
 * @version 1.0
 */
public interface ClientAppJpaRepository extends JpaRepository<ClientAppEntity, UUID>, JpaSpecificationExecutor<ClientAppEntity> {

  Optional<ClientAppEntity> findByClientId(String clientId);

  Optional<ClientAppEntity> findByClientIdAndTenantId(String clientId, UUID tenantId);

  List<ClientAppEntity> findAllByTenantId(UUID tenantId);

  boolean existsByClientId(String clientId);

  /** Count apps with the given status. */
  long countByStatus(ClientAppStatus status);

  /** Count apps grouped by status (single GROUP BY query for dashboard). */
  @Query("SELECT a.status, COUNT(a) FROM ClientAppEntity a GROUP BY a.status")
  List<Object[]> countGroupByStatus();

  /** Count apps by client type (PUBLIC / CONFIDENTIAL). */
  long countByType(ClientType type);

  /** Count apps grouped by type (single GROUP BY query for dashboard). */
  @Query("SELECT a.type, COUNT(a) FROM ClientAppEntity a GROUP BY a.type")
  List<Object[]> countGroupByType();

  /** Count apps created after the given cutoff. */
  long countByCreatedAtAfter(OffsetDateTime cutoff);

  /** Count apps that have no redirect URIs configured. */
  @Query("SELECT COUNT(a) FROM ClientAppEntity a WHERE a.redirectUris IS EMPTY")
  long countAppsWithoutRedirectUris();

  /** Top N apps by number of memberships. Returns [clientId, name, tenantSlug, count]. */
  @Query(value =
      "SELECT a.client_id, a.name, t.slug, COUNT(m.id) AS cnt " +
      "FROM client_apps a " +
      "JOIN tenants t ON t.id = a.tenant_id " +
      "LEFT JOIN memberships m ON m.client_app_id = a.id " +
      "GROUP BY a.client_id, a.name, t.slug " +
      "ORDER BY cnt DESC LIMIT :limit",
      nativeQuery = true)
  List<Object[]> findTopAppsByMembershipCount(int limit);
}

