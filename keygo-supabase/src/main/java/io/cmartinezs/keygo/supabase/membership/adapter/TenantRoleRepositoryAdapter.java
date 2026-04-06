package io.cmartinezs.keygo.supabase.membership.adapter;

import io.cmartinezs.keygo.app.membership.port.TenantRoleRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.TenantRole;
import io.cmartinezs.keygo.domain.membership.model.TenantRoleId;
import io.cmartinezs.keygo.supabase.membership.entity.TenantRoleEntity;
import io.cmartinezs.keygo.supabase.membership.exception.TenantRolePersistenceException;
import io.cmartinezs.keygo.supabase.membership.mapper.MembershipPersistenceMapper;
import io.cmartinezs.keygo.supabase.membership.repository.TenantRoleJpaRepository;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import io.cmartinezs.keygo.supabase.tenant.repository.TenantJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adapter: implements TenantRoleRepositoryPort using JPA persistence.
 * <p>Adaptador: implementa TenantRoleRepositoryPort usando persistencia JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class TenantRoleRepositoryAdapter implements TenantRoleRepositoryPort {

  private final TenantRoleJpaRepository jpaRepository;
  private final TenantJpaRepository tenantJpaRepository;

  public TenantRoleRepositoryAdapter(
      TenantRoleJpaRepository jpaRepository,
      TenantJpaRepository tenantJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.tenantJpaRepository = tenantJpaRepository;
  }

  @Override
  public TenantRole create(TenantRole tenantRole) {
    TenantEntity tenantEntity = tenantJpaRepository.findById(tenantRole.getTenantId().value())
        .orElseThrow(() -> new TenantRolePersistenceException(
            "Tenant not found: " + tenantRole.getTenantId()));

    TenantRoleEntity entity = TenantRoleEntity.builder()
        .tenant(tenantEntity)
        .code(tenantRole.getCode())
        .name(tenantRole.getName())
        .description(tenantRole.getDescription())
        .active(tenantRole.isActive())
        .build();

    return MembershipPersistenceMapper.toDomain(jpaRepository.save(entity));
  }

  @Override
  public Optional<TenantRole> findByTenantAndCode(UUID tenantId, String code) {
    return jpaRepository.findByTenantIdAndCode(tenantId, code)
        .map(MembershipPersistenceMapper::toDomain);
  }

  @Override
  public List<TenantRole> findByTenantId(UUID tenantId) {
    return jpaRepository.findByTenantId(tenantId)
        .stream()
        .map(MembershipPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public List<TenantRole> findActiveByTenantId(UUID tenantId) {
    return jpaRepository.findByTenantIdAndActive(tenantId, true)
        .stream()
        .map(MembershipPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public TenantRole update(TenantRole tenantRole) {
    TenantRoleEntity entity = jpaRepository.findById(tenantRole.getId().value())
        .orElseThrow(() -> new TenantRolePersistenceException(
            "TenantRole not found: " + tenantRole.getId()));
    entity.setName(tenantRole.getName());
    entity.setDescription(tenantRole.getDescription());
    entity.setActive(tenantRole.isActive());
    return MembershipPersistenceMapper.toDomain(jpaRepository.save(entity));
  }

  @Override
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public boolean existsByTenantAndCode(UUID tenantId, String code) {
    return jpaRepository.existsByTenantIdAndCode(tenantId, code);
  }
}
