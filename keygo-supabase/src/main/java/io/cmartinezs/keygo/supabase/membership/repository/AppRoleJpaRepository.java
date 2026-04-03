package io.cmartinezs.keygo.supabase.membership.repository;

import io.cmartinezs.keygo.supabase.membership.entity.AppRoleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for AppRoleEntity.
 * <p>Repositorio Spring Data JPA para AppRoleEntity.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public interface AppRoleJpaRepository extends JpaRepository<AppRoleEntity, UUID>, JpaSpecificationExecutor<AppRoleEntity> {

  /**
   * Find a role by client app ID and role code.
   * <p>Encuentra un rol por ID de app de cliente y código de rol.
   */
  Optional<AppRoleEntity> findByClientAppIdAndCode(UUID clientAppId, String code);

  /**
   * List all roles for a client app.
   * <p>Lista todos los roles de una app de cliente.
   */
  List<AppRoleEntity> findByClientAppId(UUID clientAppId);

  /**
   * Check if a role exists.
   * <p>Verifica si un rol existe.
   */
  boolean existsByClientAppIdAndCode(UUID clientAppId, String code);
}

