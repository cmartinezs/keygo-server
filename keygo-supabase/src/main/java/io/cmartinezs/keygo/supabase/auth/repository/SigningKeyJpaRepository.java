package io.cmartinezs.keygo.supabase.auth.repository;

import io.cmartinezs.keygo.supabase.auth.entity.SigningKeyEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio JPA para {@link SigningKeyEntity}.
 */
public interface SigningKeyJpaRepository extends JpaRepository<SigningKeyEntity, UUID> {

  /** Clave global ACTIVE (tenant IS NULL). */
  Optional<SigningKeyEntity> findFirstByTenantIsNullAndStatus(String status);

  /** Clave ACTIVE de un tenant concreto. */
  Optional<SigningKeyEntity> findFirstByTenant_IdAndStatus(UUID tenantId, String status);

  /** Claves globales publicables (ACTIVE + RETIRED, tenant IS NULL). */
  List<SigningKeyEntity> findByTenantIsNullAndStatusIn(List<String> statuses);

  /** Claves publicables de un tenant + globales (OR tenant IS NULL). */
  @Query("SELECT sk FROM SigningKeyEntity sk WHERE (sk.tenant.id = :tenantId OR sk.tenant IS NULL) AND sk.status IN :statuses")
  List<SigningKeyEntity> findPublishableByTenantIdOrGlobal(
      @Param("tenantId") UUID tenantId,
      @Param("statuses") List<String> statuses);

  /**
   * Todas las claves publicables globalmente (sin filtro de tenant).
   * Usado para la verificación de tokens (backward compat).
   */
  List<SigningKeyEntity> findByStatusIn(List<String> statuses);

  /**
   * Cuenta las claves con el estado indicado.
   *
   * @param status estado (ACTIVE, RETIRED, REVOKED)
   * @return número de claves
   */
  long countByStatus(String status);

  /**
   * Cuenta claves agrupadas por estado (query GROUP BY única para dashboard).
   *
   * @return lista de [status, count]
   */
  @Query("SELECT sk.status, COUNT(sk) FROM SigningKeyEntity sk GROUP BY sk.status")
  List<Object[]> countGroupByStatus();
}
