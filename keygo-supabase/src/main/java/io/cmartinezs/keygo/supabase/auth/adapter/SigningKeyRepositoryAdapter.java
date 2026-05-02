package io.cmartinezs.keygo.supabase.auth.adapter;

import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyStatus;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.supabase.auth.mapper.SigningKeyPersistenceMapper;
import io.cmartinezs.keygo.supabase.auth.repository.SigningKeyJpaRepository;
import io.cmartinezs.keygo.supabase.tenant.repository.TenantJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Adaptador JPA que implementa {@link SigningKeyRepositoryPort}.
 */
@Repository
public class SigningKeyRepositoryAdapter implements SigningKeyRepositoryPort {

  private static final List<String> PUBLISHABLE_STATUSES = List.of(
      SigningKeyStatus.ACTIVE.name(),
      SigningKeyStatus.RETIRED.name());

  private final SigningKeyJpaRepository jpaRepository;
  private final TenantJpaRepository tenantJpaRepository;

  public SigningKeyRepositoryAdapter(
      SigningKeyJpaRepository jpaRepository,
      TenantJpaRepository tenantJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.tenantJpaRepository = tenantJpaRepository;
  }

  /** Clave ACTIVE global (tenant IS NULL). Usado por bootstrap/inicializador. */
  @Override
  public Optional<SigningKey> findActiveKey() {
    return jpaRepository
        .findFirstByTenantIsNullAndStatus(SigningKeyStatus.ACTIVE.name())
        .map(SigningKeyPersistenceMapper::toDomain);
  }

  /**
   * Clave ACTIVE del tenant indicado, con fallback a la clave global si no hay tenant-específica.
   */
  @Override
  public Optional<SigningKey> findActiveKeyForTenant(TenantId tenantId) {
    return jpaRepository
        .findFirstByTenant_IdAndStatus(tenantId.value(), SigningKeyStatus.ACTIVE.name())
        .or(() -> jpaRepository.findFirstByTenantIsNullAndStatus(SigningKeyStatus.ACTIVE.name()))
        .map(SigningKeyPersistenceMapper::toDomain);
  }

  /** Todas las claves publicables sin filtro de tenant (para verificación de tokens). */
  @Override
  public List<SigningKey> findPublishableKeys() {
    return jpaRepository
        .findByStatusIn(PUBLISHABLE_STATUSES)
        .stream()
        .map(SigningKeyPersistenceMapper::toDomain)
        .toList();
  }

  /** Claves publicables del tenant + globales. Usado por JWKS endpoint. */
  @Override
  public List<SigningKey> findPublishableKeysForTenant(TenantId tenantId) {
    return jpaRepository
        .findPublishableByTenantIdOrGlobal(tenantId.value(), PUBLISHABLE_STATUSES)
        .stream()
        .map(SigningKeyPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public SigningKey save(SigningKey key) {
    var tenantEntity = (key.getTenantId() != null)
        ? tenantJpaRepository.getReferenceById(key.getTenantId().value())
        : null;
    var entity = SigningKeyPersistenceMapper.toEntity(key, tenantEntity);
    var saved = jpaRepository.save(entity);
    return SigningKeyPersistenceMapper.toDomain(saved);
  }
}
