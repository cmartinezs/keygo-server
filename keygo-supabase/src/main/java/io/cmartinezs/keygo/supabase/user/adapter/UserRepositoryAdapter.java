package io.cmartinezs.keygo.supabase.user.adapter;

import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.Username;
import io.cmartinezs.keygo.supabase.user.mapper.UserPersistenceMapper;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing UserRepositoryPort using Spring Data JPA.
 * <p>Adaptador que implementa UserRepositoryPort usando Spring Data JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

  private final TenantUserJpaRepository jpaRepository;
  private final UserPersistenceMapper mapper;

  public UserRepositoryAdapter(TenantUserJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
    this.mapper = new UserPersistenceMapper();
  }

  @Override
  public User save(User user) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(user)));
  }

  @Override
  public Optional<User> findByIdAndTenantId(UserId userId, TenantId tenantId) {
    return jpaRepository.findByIdAndTenantId(userId.value(), tenantId.value())
        .map(mapper::toDomain);
  }

  @Override
  public Optional<User> findByTenantIdAndEmail(TenantId tenantId, EmailAddress email) {
    return jpaRepository.findByTenantIdAndEmail(tenantId.value(), email.value())
        .map(mapper::toDomain);
  }

  @Override
  public Optional<User> findByTenantIdAndUsername(TenantId tenantId, Username username) {
    return jpaRepository.findByTenantIdAndUsername(tenantId.value(), username.value())
        .map(mapper::toDomain);
  }

  @Override
  public boolean existsByTenantIdAndEmail(TenantId tenantId, EmailAddress email) {
    return jpaRepository.existsByTenantIdAndEmail(tenantId.value(), email.value());
  }

  @Override
  public boolean existsByTenantIdAndUsername(TenantId tenantId, Username username) {
    return jpaRepository.existsByTenantIdAndUsername(tenantId.value(), username.value());
  }

  @Override
  public List<User> findAllByTenantId(TenantId tenantId) {
    return jpaRepository.findAllByTenantId(tenantId.value()).stream()
        .map(mapper::toDomain)
        .toList();
  }
}

