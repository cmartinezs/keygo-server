package io.cmartinezs.keygo.supabase.membership.adapter;

import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.membership.model.AppRoleId;
import io.cmartinezs.keygo.domain.membership.model.RoleCode;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.membership.entity.AppRoleEntity;
import io.cmartinezs.keygo.supabase.membership.exception.AppRolePersistenceException;
import io.cmartinezs.keygo.supabase.membership.mapper.MembershipPersistenceMapper;
import io.cmartinezs.keygo.supabase.membership.repository.AppRoleJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adapter: implements AppRoleRepositoryPort using JPA persistence.
 * <p>Adaptador: implementa AppRoleRepositoryPort usando persistencia JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class AppRoleRepositoryAdapter implements AppRoleRepositoryPort {

  private final AppRoleJpaRepository jpaRepository;

  public AppRoleRepositoryAdapter(AppRoleJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<AppRole> findById(AppRoleId roleId) {
    return jpaRepository.findById(roleId.value())
        .map(MembershipPersistenceMapper::toDomain);
  }

  @Override
  public Optional<AppRole> findByClientAppAndCode(UUID clientAppId, RoleCode roleCode) {
    return jpaRepository.findByClientAppIdAndCode(clientAppId, roleCode.value())
        .map(MembershipPersistenceMapper::toDomain);
  }

  @Override
  public List<AppRole> findByClientAppId(UUID clientAppId) {
    return jpaRepository.findByClientAppId(clientAppId)
        .stream()
        .map(MembershipPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public boolean existsByClientAppAndCode(UUID clientAppId, RoleCode roleCode) {
    return jpaRepository.existsByClientAppIdAndCode(clientAppId, roleCode.value());
  }

  @Override
  public AppRole save(AppRole role) {
    AppRoleEntity entity = new AppRoleEntity();
    entity.setId(role.getId().value());
    entity.setCode(role.getCode().value());
    entity.setDisplayName(role.getDisplayName());
    entity.setDescription(role.getDescription());

    // Set FK reference (non-managed entity, only ID)
    ClientAppEntity appRef = new ClientAppEntity();
    appRef.setId(role.getClientAppId().value());
    entity.setClientApp(appRef);

    AppRoleEntity saved = jpaRepository.save(entity);
    return MembershipPersistenceMapper.toDomain(saved);
  }

  @Override
  public AppRole update(AppRole role) {
    AppRoleEntity entity = jpaRepository.findById(role.getId().value())
        .orElseThrow(() -> new AppRolePersistenceException(role.getId().value()));

    entity.setDisplayName(role.getDisplayName());
    entity.setDescription(role.getDescription());
    AppRoleEntity updated = jpaRepository.save(entity);
    return MembershipPersistenceMapper.toDomain(updated);
  }

  @Override
  public void deleteById(AppRoleId roleId) {
    jpaRepository.deleteById(roleId.value());
  }
}

