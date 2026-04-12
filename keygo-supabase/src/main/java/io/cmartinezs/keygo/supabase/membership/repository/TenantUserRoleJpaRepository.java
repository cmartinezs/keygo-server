package io.cmartinezs.keygo.supabase.membership.repository;

import io.cmartinezs.keygo.supabase.membership.entity.TenantUserRoleEntity;
import io.cmartinezs.keygo.supabase.membership.entity.TenantUserRoleKey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for TenantUserRoleEntity.
 * <p>Repositorio Spring Data JPA para TenantUserRoleEntity.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public interface TenantUserRoleJpaRepository extends JpaRepository<TenantUserRoleEntity, TenantUserRoleKey> {

  List<TenantUserRoleEntity> findByTenantUserId(UUID tenantUserId);

  Optional<TenantUserRoleEntity> findByTenantUserIdAndTenantRoleId(UUID tenantUserId, UUID tenantRoleId);

  boolean existsByTenantUserIdAndTenantRoleId(UUID tenantUserId, UUID tenantRoleId);

  void deleteByTenantUserIdAndTenantRoleId(UUID tenantUserId, UUID tenantRoleId);
}
