package io.cmartinezs.keygo.supabase.membership.repository;

import io.cmartinezs.keygo.supabase.membership.entity.TenantRoleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for TenantRoleEntity.
 * <p>Repositorio Spring Data JPA para TenantRoleEntity.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public interface TenantRoleJpaRepository extends JpaRepository<TenantRoleEntity, UUID> {

  Optional<TenantRoleEntity> findByTenantIdAndCode(UUID tenantId, String code);

  List<TenantRoleEntity> findByTenantId(UUID tenantId);

  boolean existsByTenantIdAndCode(UUID tenantId, String code);
}
