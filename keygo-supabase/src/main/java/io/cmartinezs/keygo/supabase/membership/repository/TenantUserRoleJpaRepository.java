package io.cmartinezs.keygo.supabase.membership.repository;

import io.cmartinezs.keygo.supabase.membership.entity.TenantUserRoleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for TenantUserRoleEntity.
 * <p>Repositorio Spring Data JPA para TenantUserRoleEntity.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public interface TenantUserRoleJpaRepository extends JpaRepository<TenantUserRoleEntity, UUID> {

  /** All role assignments (active and revoked) for a tenant user. */
  List<TenantUserRoleEntity> findByTenantUserId(UUID tenantUserId);

  /** Only active assignments (not yet revoked) for a tenant user. */
  @Query("SELECT t FROM TenantUserRoleEntity t WHERE t.tenantUser.id = :tenantUserId AND t.removedAt IS NULL")
  List<TenantUserRoleEntity> findActiveByTenantUserId(@Param("tenantUserId") UUID tenantUserId);

  /** Active assignment for a specific tenant user and role code. */
  @Query("SELECT t FROM TenantUserRoleEntity t WHERE t.tenantUser.id = :tenantUserId AND t.tenantRole.code = :roleCode AND t.removedAt IS NULL")
  Optional<TenantUserRoleEntity> findActiveByTenantUserIdAndRoleCode(
      @Param("tenantUserId") UUID tenantUserId,
      @Param("roleCode") String roleCode);

  /** Check if an active assignment exists. */
  @Query("SELECT COUNT(t) > 0 FROM TenantUserRoleEntity t WHERE t.tenantUser.id = :tenantUserId AND t.tenantRole.id = :tenantRoleId AND t.removedAt IS NULL")
  boolean existsActiveByTenantUserIdAndTenantRoleId(
      @Param("tenantUserId") UUID tenantUserId,
      @Param("tenantRoleId") UUID tenantRoleId);

  @Modifying
  @Query("DELETE FROM TenantUserRoleEntity t WHERE t.tenantUser.id = :tenantUserId AND t.tenantRole.id = :tenantRoleId")
  void deleteByTenantUserIdAndTenantRoleId(
      @Param("tenantUserId") UUID tenantUserId,
      @Param("tenantRoleId") UUID tenantRoleId);
}
