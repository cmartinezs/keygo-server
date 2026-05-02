package io.cmartinezs.keygo.supabase.user.repository;

import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for tenant-scoped user persistence.
 * <p>Repositorio JPA para persistencia de usuarios con alcance de tenant.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public interface TenantUserJpaRepository extends JpaRepository<TenantUserEntity, UUID>, JpaSpecificationExecutor<TenantUserEntity> {

  /**
   * Find a user by its UUID and tenant ID.
   * <p>Busca un usuario por su UUID y tenant ID.
   */
  @Query(
      "SELECT tu FROM TenantUserEntity tu "
          + "JOIN FETCH tu.platformUser pu "
          + "WHERE tu.id = :id AND tu.tenant.id = :tenantId")
  Optional<TenantUserEntity> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

  /**
   * Find a user by email within a tenant.
   * <p>Busca un usuario por email dentro de un tenant.
   */
  @Query(
      "SELECT tu FROM TenantUserEntity tu "
          + "JOIN FETCH tu.platformUser pu "
          + "WHERE tu.tenant.id = :tenantId AND lower(pu.email) = lower(:email)")
  Optional<TenantUserEntity> findByTenantIdAndEmail(@Param("tenantId") UUID tenantId, @Param("email") String email);

  /**
   * Find a user by username within a tenant.
   * <p>Busca un usuario por username dentro de un tenant.
   */
  @Query(
      "SELECT tu FROM TenantUserEntity tu "
          + "JOIN FETCH tu.platformUser pu "
          + "WHERE tu.tenant.id = :tenantId AND tu.localUsername = :username")
  Optional<TenantUserEntity> findByTenantIdAndUsername(
      @Param("tenantId") UUID tenantId, @Param("username") String username);

  @Query(
      "SELECT tu FROM TenantUserEntity tu "
          + "JOIN FETCH tu.platformUser pu "
          + "WHERE tu.tenant.id = :tenantId AND tu.platformUser.id = :platformUserId")
  Optional<TenantUserEntity> findByTenantIdAndPlatformUserId(
      @Param("tenantId") UUID tenantId, @Param("platformUserId") UUID platformUserId);

  /**
   * Check whether a user with the given email exists within a tenant.
   * <p>Verifica si existe un usuario con el email dado dentro de un tenant.
   */
  @Query(
      "SELECT CASE WHEN COUNT(tu) > 0 THEN true ELSE false END "
          + "FROM TenantUserEntity tu "
          + "WHERE tu.tenant.id = :tenantId AND lower(tu.platformUser.email) = lower(:email)")
  boolean existsByTenantIdAndEmail(@Param("tenantId") UUID tenantId, @Param("email") String email);

  /**
   * Check whether a user with the given username exists within a tenant.
   * <p>Verifica si existe un usuario con el username dado dentro de un tenant.
   */
  boolean existsByTenantIdAndLocalUsername(UUID tenantId, String localUsername);

  /**
   * Find all usernames that start with the given prefix within a tenant.
   * <p>Busca todos los usernames que empiezan con el prefijo dado dentro de un tenant.
   */
  @Query(
      "SELECT tu.localUsername FROM TenantUserEntity tu "
          + "WHERE tu.tenant.id = :tenantId AND tu.localUsername LIKE :prefix || '%'")
  List<String> findUsernamesByPrefix(@Param("tenantId") UUID tenantId, @Param("prefix") String prefix);

  /**
   * Find all users belonging to a tenant.
   * <p>Busca todos los usuarios de un tenant.
   */
  @Query(
      "SELECT tu FROM TenantUserEntity tu "
          + "JOIN FETCH tu.platformUser pu "
          + "WHERE tu.tenant.id = :tenantId")
  List<TenantUserEntity> findAllByTenantId(@Param("tenantId") UUID tenantId);

  /**
   * Count users with the given status across all tenants.
   * <p>Cuenta usuarios con el estado dado en todos los tenants.
   */
  long countByStatus(UserStatus status);

  /** Count users grouped by status (single GROUP BY query for dashboard). */
  @Query("SELECT u.status, COUNT(u) FROM TenantUserEntity u GROUP BY u.status")
  List<Object[]> countGroupByStatus();

  /** Count users created after the given cutoff (across all tenants). */
  long countByCreatedAtAfter(OffsetDateTime cutoff);

  /** Count users that have no membership in any app. */
  @Query("SELECT COUNT(u) FROM TenantUserEntity u WHERE u.id NOT IN (SELECT m.user.id FROM MembershipEntity m)")
  long countUsersWithoutMembership();
}
