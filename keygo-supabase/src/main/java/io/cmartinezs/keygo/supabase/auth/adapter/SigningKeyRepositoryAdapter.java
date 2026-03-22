package io.cmartinezs.keygo.supabase.auth.adapter;

import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyStatus;
import io.cmartinezs.keygo.supabase.auth.mapper.SigningKeyPersistenceMapper;
import io.cmartinezs.keygo.supabase.auth.repository.SigningKeyJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Adaptador JPA que implementa {@link SigningKeyRepositoryPort}.
 */
@Repository
public class SigningKeyRepositoryAdapter implements SigningKeyRepositoryPort {

  private final SigningKeyJpaRepository jpaRepository;

  public SigningKeyRepositoryAdapter(SigningKeyJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<SigningKey> findActiveKey() {
    return jpaRepository
        .findFirstByStatus(SigningKeyStatus.ACTIVE.name())
        .map(SigningKeyPersistenceMapper::toDomain);
  }

  @Override
  public List<SigningKey> findPublishableKeys() {
    List<String> publishableStatuses = List.of(
        SigningKeyStatus.ACTIVE.name(),
        SigningKeyStatus.RETIRED.name());
    return jpaRepository
        .findByStatusIn(publishableStatuses)
        .stream()
        .map(SigningKeyPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public SigningKey save(SigningKey key) {
    var entity = SigningKeyPersistenceMapper.toEntity(key);
    var saved = jpaRepository.save(entity);
    return SigningKeyPersistenceMapper.toDomain(saved);
  }
}


