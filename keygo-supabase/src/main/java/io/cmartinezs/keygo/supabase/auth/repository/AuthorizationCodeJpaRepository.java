package io.cmartinezs.keygo.supabase.auth.repository;

import io.cmartinezs.keygo.supabase.auth.entity.AuthorizationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository para AuthorizationCodeEntity.
 */
@Repository
public interface AuthorizationCodeJpaRepository extends JpaRepository<AuthorizationCodeEntity, UUID> {

  /**
   * Busca un código por su valor.
   *
   * @param code valor del código
   * @return el código si existe
   */
  Optional<AuthorizationCodeEntity> findByCode(String code);

  /**
   * Count authorization codes with the given status.
   * ⚠️ Status values are lowercase: pending, used, expired, revoked.
   */
  long countByStatus(String status);

  /**
   * Count authorization codes grouped by status (single GROUP BY query for dashboard).
   * ⚠️ Status values are lowercase.
   */
  @Query("SELECT ac.status, COUNT(ac) FROM AuthorizationCodeEntity ac GROUP BY ac.status")
  List<Object[]> countGroupByStatus();
}

