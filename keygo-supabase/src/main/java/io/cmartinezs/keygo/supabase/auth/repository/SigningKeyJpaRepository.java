package io.cmartinezs.keygo.supabase.auth.repository;

import io.cmartinezs.keygo.supabase.auth.entity.SigningKeyEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

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
}

