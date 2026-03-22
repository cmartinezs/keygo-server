package io.cmartinezs.keygo.supabase.auth.repository;

import io.cmartinezs.keygo.supabase.auth.entity.AuthorizationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
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
}

