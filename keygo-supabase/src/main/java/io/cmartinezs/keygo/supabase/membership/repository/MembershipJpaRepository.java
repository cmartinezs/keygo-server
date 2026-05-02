package io.cmartinezs.keygo.supabase.membership.repository;

import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;
import io.cmartinezs.keygo.supabase.membership.entity.MembershipEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for MembershipEntity.
 * <p>Repositorio Spring Data JPA para MembershipEntity.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public interface MembershipJpaRepository extends JpaRepository<MembershipEntity, UUID>, JpaSpecificationExecutor<MembershipEntity> {

  /**
   * Find a membership by user ID and client app ID.
   * <p>Encuentra una membresía por ID de usuario e ID de app de cliente.
   */
  @Query("SELECT m FROM MembershipEntity m WHERE m.user.id = :userId AND m.clientApp.id = :clientAppId")
  Optional<MembershipEntity> findByUserIdAndClientAppId(
      @Param("userId") UUID userId,
      @Param("clientAppId") UUID clientAppId);

  /**
   * List all memberships for a user.
   * <p>Lista todas las membresías de un usuario.
   */
  @Query("SELECT m FROM MembershipEntity m WHERE m.user.id = :userId")
  List<MembershipEntity> findByUserId(@Param("userId") UUID userId);

  /**
   * List all memberships for a client app.
   * <p>Lista todas las membresías de una app de cliente.
   */
  @Query("SELECT m FROM MembershipEntity m WHERE m.clientApp.id = :clientAppId")
  List<MembershipEntity> findByClientAppId(@Param("clientAppId") UUID clientAppId);

  /**
   * List all memberships for a user within a specific tenant.
   * <p>Lista membresías de un usuario dentro de un tenant específico.
   */
  @Query("SELECT m FROM MembershipEntity m WHERE m.user.id = :userId AND m.user.tenant.slug = :tenantSlug")
  List<MembershipEntity> findByUserIdAndTenantSlug(
      @Param("userId") UUID userId,
      @Param("tenantSlug") String tenantSlug);

  /**
   * List all memberships for a client app within a specific tenant.
   * <p>Lista membresías de una app dentro de un tenant específico.
   */
  @Query("SELECT m FROM MembershipEntity m WHERE m.clientApp.id = :clientAppId AND m.clientApp.tenant.slug = :tenantSlug")
  List<MembershipEntity> findByClientAppIdAndTenantSlug(
      @Param("clientAppId") UUID clientAppId,
      @Param("tenantSlug") String tenantSlug);

  /**
   * Find a membership by ID within a specific tenant (validated via user's tenant).
   * <p>Busca una membresía por ID dentro de un tenant específico (validado por tenant del usuario).
   */
  @Query("SELECT m FROM MembershipEntity m WHERE m.id = :membershipId AND m.user.tenant.slug = :tenantSlug")
  Optional<MembershipEntity> findByIdAndTenantSlug(
      @Param("membershipId") UUID membershipId,
      @Param("tenantSlug") String tenantSlug);

  /**
   * Check if a membership exists.
   * <p>Verifica si una membresía existe.
   */
  boolean existsByUserIdAndClientAppId(UUID userId, UUID clientAppId);

  /**
   * Find role codes for the active membership of a given user and client app.
   * <p>Encuentra los códigos de rol de la membresía activa de un usuario en una app.
   */
  @Query(value =
      "SELECT ar.code FROM app_roles ar " +
      "JOIN app_membership_roles mr ON ar.id = mr.role_id AND ar.client_app_id = mr.client_app_id " +
      "JOIN app_memberships m ON mr.membership_id = m.id AND mr.client_app_id = m.client_app_id " +
      "WHERE m.tenant_user_id = :userId AND m.client_app_id = :clientAppId AND m.status = 'ACTIVE'",
      nativeQuery = true)
  List<String> findRoleCodesByUserIdAndClientAppId(
      @Param("userId") UUID userId,
      @Param("clientAppId") UUID clientAppId);

  /**
   * Find effective role codes (direct + inherited via role hierarchy) for a user in a client app.
   * Uses a recursive CTE to traverse app_role_hierarchy starting from the user's direct roles.
   */
  @Query(value = """
      WITH RECURSIVE direct_roles AS (
        SELECT ar.id, ar.code
        FROM   app_roles ar
        JOIN   app_membership_roles mr ON ar.id = mr.role_id AND ar.client_app_id = mr.client_app_id
        JOIN   app_memberships m       ON mr.membership_id = m.id AND mr.client_app_id = m.client_app_id
        WHERE  m.tenant_user_id = :userId
          AND  m.client_app_id = :clientAppId
          AND  m.status = 'ACTIVE'
      ),
      role_ancestors AS (
        SELECT id, code FROM direct_roles
        UNION
        SELECT ar.id, ar.code
        FROM   app_roles ar
        JOIN   app_role_hierarchy arh ON ar.id = arh.parent_role_id AND ar.client_app_id = arh.client_app_id
        JOIN   role_ancestors ra      ON arh.child_role_id = ra.id
      )
      SELECT DISTINCT code FROM role_ancestors
      """, nativeQuery = true)
  List<String> findEffectiveRoleCodesByUserIdAndClientAppId(
      @Param("userId") UUID userId,
      @Param("clientAppId") UUID clientAppId);

  /** Count memberships with the given status. */
  long countByStatus(MembershipStatus status);

  /** Count memberships grouped by status (single GROUP BY query for dashboard). */
  @Query("SELECT m.status, COUNT(m) FROM MembershipEntity m GROUP BY m.status")
  List<Object[]> countGroupByStatus();
}
