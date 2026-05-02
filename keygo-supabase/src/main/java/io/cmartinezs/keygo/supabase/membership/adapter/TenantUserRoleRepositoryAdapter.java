package io.cmartinezs.keygo.supabase.membership.adapter;

import io.cmartinezs.keygo.app.membership.port.TenantUserRoleRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.TenantUserRole;
import io.cmartinezs.keygo.supabase.membership.entity.TenantRoleEntity;
import io.cmartinezs.keygo.supabase.membership.entity.TenantUserRoleEntity;
import io.cmartinezs.keygo.supabase.membership.exception.TenantRolePersistenceException;
import io.cmartinezs.keygo.supabase.membership.mapper.MembershipPersistenceMapper;
import io.cmartinezs.keygo.supabase.membership.repository.TenantRoleJpaRepository;
import io.cmartinezs.keygo.supabase.membership.repository.TenantUserRoleJpaRepository;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adapter: implements TenantUserRoleRepositoryPort using JPA persistence.
 * <p>Adaptador: implementa TenantUserRoleRepositoryPort usando persistencia JPA.
 * Revocation deletes the join row because the baseline table has no soft-delete column.
 * <p>La revocación elimina la fila del vínculo porque la tabla baseline no tiene columna de soft-delete.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class TenantUserRoleRepositoryAdapter implements TenantUserRoleRepositoryPort {

  private final TenantUserRoleJpaRepository jpaRepository;
  private final TenantUserJpaRepository tenantUserJpaRepository;
  private final TenantRoleJpaRepository tenantRoleJpaRepository;

  public TenantUserRoleRepositoryAdapter(
      TenantUserRoleJpaRepository jpaRepository,
      TenantUserJpaRepository tenantUserJpaRepository,
      TenantRoleJpaRepository tenantRoleJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.tenantUserJpaRepository = tenantUserJpaRepository;
    this.tenantRoleJpaRepository = tenantRoleJpaRepository;
  }

  @Override
  @Transactional
  public TenantUserRole assign(UUID tenantUserId, UUID tenantRoleId) {
    TenantUserEntity tenantUser = tenantUserJpaRepository.findById(tenantUserId)
        .orElseThrow(() -> new TenantRolePersistenceException(
            "TenantUser not found: " + tenantUserId));
    TenantRoleEntity tenantRole = tenantRoleJpaRepository.findById(tenantRoleId)
        .orElseThrow(() -> new TenantRolePersistenceException(
            "TenantRole not found: " + tenantRoleId));

    TenantUserRoleEntity entity = TenantUserRoleEntity.builder()
        .tenantUserId(tenantUser.getId())
        .tenantRoleId(tenantRole.getId())
        .tenantId(tenantUser.getTenant().getId())
        .tenantUser(tenantUser)
        .tenantRole(tenantRole)
        .build();

    return MembershipPersistenceMapper.toDomain(jpaRepository.save(entity));
  }

  @Override
  @Transactional
  public void revoke(UUID tenantUserId, UUID tenantRoleId) {
    jpaRepository.deleteByTenantUserIdAndTenantRoleId(tenantUserId, tenantRoleId);
  }

  @Override
  public List<TenantUserRole> findActiveByTenantUserId(UUID tenantUserId) {
    return jpaRepository.findByTenantUserId(tenantUserId)
        .stream()
        .map(MembershipPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public List<TenantUserRole> findAllByTenantUserId(UUID tenantUserId) {
    return jpaRepository.findByTenantUserId(tenantUserId)
        .stream()
        .map(MembershipPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public boolean hasActiveRole(UUID tenantUserId, UUID tenantRoleId) {
    return jpaRepository.existsByTenantUserIdAndTenantRoleId(tenantUserId, tenantRoleId);
  }
}
