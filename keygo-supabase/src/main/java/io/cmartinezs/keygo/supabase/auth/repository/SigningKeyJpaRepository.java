package io.cmartinezs.keygo.supabase.auth.repository;

import io.cmartinezs.keygo.supabase.auth.entity.SigningKeyEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repositorio JPA para {@link SigningKeyEntity}.
 */
public interface SigningKeyJpaRepository extends JpaRepository<SigningKeyEntity, UUID> {

  /**
   * Busca la primera clave con el estado indicado.
   *
   * @param status estado (ACTIVE, RETIRED, REVOKED)
   * @return la clave si existe
   */
  Optional<SigningKeyEntity> findFirstByStatus(String status);

  /**
   * Busca todas las claves cuyo estado esté en la lista dada.
   *
   * @param statuses lista de estados
   * @return claves encontradas
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

