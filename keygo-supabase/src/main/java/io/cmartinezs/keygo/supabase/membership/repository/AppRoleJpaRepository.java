package io.cmartinezs.keygo.supabase.membership.repository;

import io.cmartinezs.keygo.supabase.membership.entity.AppRoleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

  /**
   * Count roles for a client app.
   * <p>Cuenta roles de una app de cliente.
   */
  int countByClientAppId(UUID clientAppId);

  /**
   * Find the default role for a client app.
   * <p>Encuentra el rol por defecto de una app de cliente.
   */
  Optional<AppRoleEntity> findByClientAppIdAndIsDefaultTrue(UUID clientAppId);

  /**
   * Clear the default flag on all roles of a client app.
   * <p>Limpia is_default en todos los roles de una app de cliente.
   */
  @Modifying
  @Transactional
  @Query("UPDATE AppRoleEntity r SET r.isDefault = false WHERE r.clientAppId = :clientAppId AND r.isDefault = true")
  void clearDefaultByClientAppId(@Param("clientAppId") UUID clientAppId);

  /**
   * Find all roles assigned to a membership via app_membership_roles join.
   * <p>Busca todos los roles asignados a una membresía vía join con app_membership_roles.
   */
  @Query(value =
      "SELECT ar.* FROM app_roles ar "
      + "JOIN app_membership_roles mr ON ar.id = mr.role_id AND ar.client_app_id = mr.client_app_id "
      + "WHERE mr.membership_id = :membershipId",
      nativeQuery = true)
  List<AppRoleEntity> findByMembershipId(@Param("membershipId") UUID membershipId);
}

