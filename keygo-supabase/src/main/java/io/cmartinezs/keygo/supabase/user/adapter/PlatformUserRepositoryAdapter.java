package io.cmartinezs.keygo.supabase.user.adapter;

import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.Username;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import io.cmartinezs.keygo.supabase.user.mapper.PlatformUserPersistenceMapper;
import io.cmartinezs.keygo.supabase.user.repository.PlatformUserJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Adapter: implements PlatformUserRepositoryPort using JPA persistence.
 * <p>Adaptador: implementa PlatformUserRepositoryPort usando persistencia JPA.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class PlatformUserRepositoryAdapter implements PlatformUserRepositoryPort {

  private final PlatformUserJpaRepository jpaRepository;
  private final PlatformUserPersistenceMapper mapper;

  public PlatformUserRepositoryAdapter(PlatformUserJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
    this.mapper = new PlatformUserPersistenceMapper();
  }

  @Override
  public PlatformUser save(PlatformUser user) {
    PlatformUserEntity entity = mapper.toEntity(user);
    PlatformUserEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<PlatformUser> findByEmail(EmailAddress email) {
    return jpaRepository.findByEmail(email.value()).map(mapper::toDomain);
  }

  @Override
  public Optional<PlatformUser> findByUsername(Username username) {
    return jpaRepository.findByUsername(username.value()).map(mapper::toDomain);
  }

  @Override
  public Optional<PlatformUser> findById(UserId userId) {
    return jpaRepository.findById(userId.value()).map(mapper::toDomain);
  }

  @Override
  public boolean existsByEmail(EmailAddress email) {
    return jpaRepository.existsByEmail(email.value());
  }

  @Override
  public boolean existsByUsername(Username username) {
    return jpaRepository.existsByUsername(username.value());
  }
}
