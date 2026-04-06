package io.cmartinezs.keygo.supabase.membership.adapter;

import io.cmartinezs.keygo.app.membership.port.PlatformRoleRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.PlatformRole;
import io.cmartinezs.keygo.domain.membership.model.PlatformRoleId;
import io.cmartinezs.keygo.supabase.membership.entity.PlatformRoleEntity;
import io.cmartinezs.keygo.supabase.membership.mapper.MembershipPersistenceMapper;
import io.cmartinezs.keygo.supabase.membership.repository.PlatformRoleJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adapter: implements PlatformRoleRepositoryPort using JPA persistence.
 * <p>Adaptador: implementa PlatformRoleRepositoryPort usando persistencia JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class PlatformRoleRepositoryAdapter implements PlatformRoleRepositoryPort {

  private final PlatformRoleJpaRepository jpaRepository;

  public PlatformRoleRepositoryAdapter(PlatformRoleJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<PlatformRole> findByCode(String code) {
    return jpaRepository.findByCode(code)
        .map(MembershipPersistenceMapper::toDomain);
  }

  @Override
  public List<PlatformRole> findAll() {
    return jpaRepository.findAll()
        .stream()
        .map(MembershipPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public PlatformRole save(PlatformRole platformRole) {
    PlatformRoleEntity entity = jpaRepository.findById(platformRole.getId().value())
        .orElseGet(PlatformRoleEntity::new);
    entity.setCode(platformRole.getCode());
    entity.setName(platformRole.getName());
    entity.setDescription(platformRole.getDescription());
    return MembershipPersistenceMapper.toDomain(jpaRepository.save(entity));
  }

  @Override
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public boolean existsByCode(String code) {
    return jpaRepository.existsByCode(code);
  }
}
