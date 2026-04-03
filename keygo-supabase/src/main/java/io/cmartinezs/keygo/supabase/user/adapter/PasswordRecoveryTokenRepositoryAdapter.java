package io.cmartinezs.keygo.supabase.user.adapter;

import io.cmartinezs.keygo.app.user.port.PasswordRecoveryTokenRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.PasswordRecoveryToken;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.supabase.user.entity.PasswordRecoveryTokenEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import io.cmartinezs.keygo.supabase.user.repository.PasswordRecoveryTokenJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Adapter implementing PasswordRecoveryTokenRepositoryPort using Spring Data JPA.
 * <p>Adaptador que implementa PasswordRecoveryTokenRepositoryPort usando Spring Data JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class PasswordRecoveryTokenRepositoryAdapter implements PasswordRecoveryTokenRepositoryPort {

  private final PasswordRecoveryTokenJpaRepository jpaRepository;
  private final TenantUserJpaRepository tenantUserJpaRepository;

  public PasswordRecoveryTokenRepositoryAdapter(
      PasswordRecoveryTokenJpaRepository jpaRepository,
      TenantUserJpaRepository tenantUserJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.tenantUserJpaRepository = tenantUserJpaRepository;
  }

  @Override
  @Transactional
  public PasswordRecoveryToken upsert(PasswordRecoveryToken domainToken) {
    TenantUserEntity userProxy = tenantUserJpaRepository.getReferenceById(domainToken.getUserId().value());

    Optional<PasswordRecoveryTokenEntity> existing =
        jpaRepository.findByTenantUser_Id(domainToken.getUserId().value());

    PasswordRecoveryTokenEntity entity;
    if (existing.isPresent()) {
      // Update existing row — invalidates the previous token
      entity = existing.get();
      entity.setToken(domainToken.getToken());
      entity.setExpiresAt(domainToken.getExpiresAt());
      entity.setUsedAt(null);
    } else {
      entity = PasswordRecoveryTokenEntity.builder()
          .tenantUser(userProxy)
          .token(domainToken.getToken())
          .expiresAt(domainToken.getExpiresAt())
          .build();
    }

    PasswordRecoveryTokenEntity saved = jpaRepository.save(entity);
    return toDomain(saved, domainToken.getTenantId());
  }

  @Override
  public Optional<PasswordRecoveryToken> findByToken(String token) {
    return jpaRepository.findByToken(token)
        .map(entity -> toDomain(entity, TenantId.of(entity.getTenantUser().getTenant().getId())));
  }

  @Override
  @Transactional
  public void markUsed(PasswordRecoveryToken token) {
    jpaRepository.markUsed(token.getToken(), Instant.now());
  }

  private PasswordRecoveryToken toDomain(PasswordRecoveryTokenEntity entity, TenantId tenantId) {
    return PasswordRecoveryToken.reconstitute(
        entity.getId(),
        UserId.of(entity.getTenantUser().getId()),
        tenantId,
        entity.getToken(),
        entity.getExpiresAt(),
        entity.getUsedAt(),
        entity.getCreatedAt());
  }
}
