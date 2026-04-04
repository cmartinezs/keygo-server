package io.cmartinezs.keygo.supabase.user.adapter;

import io.cmartinezs.keygo.app.user.port.PasswordResetCodeRepositoryPort;
import io.cmartinezs.keygo.domain.user.model.PasswordResetCode;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.supabase.user.entity.PasswordResetCodeEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import io.cmartinezs.keygo.supabase.user.repository.PasswordResetCodeJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Adaptador JPA para {@link PasswordResetCodeRepositoryPort}.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class PasswordResetCodeRepositoryAdapter implements PasswordResetCodeRepositoryPort {

  private final PasswordResetCodeJpaRepository jpaRepository;
  private final TenantUserJpaRepository tenantUserJpaRepository;

  public PasswordResetCodeRepositoryAdapter(
      PasswordResetCodeJpaRepository jpaRepository,
      TenantUserJpaRepository tenantUserJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.tenantUserJpaRepository = tenantUserJpaRepository;
  }

  @Override
  @Transactional
  public PasswordResetCode upsert(PasswordResetCode domainCode) {
    TenantUserEntity userProxy = tenantUserJpaRepository.getReferenceById(domainCode.getUserId().value());

    Optional<PasswordResetCodeEntity> existing =
        jpaRepository.findByTenantUser_Id(domainCode.getUserId().value());

    PasswordResetCodeEntity entity;
    if (existing.isPresent()) {
      // Actualizar la fila existente — invalida el código anterior
      entity = existing.get();
      entity.setCode(domainCode.getCode());
      entity.setExpiresAt(domainCode.getExpiresAt());
      entity.setUsedAt(null);
    } else {
      entity = PasswordResetCodeEntity.builder()
          .tenantUser(userProxy)
          .code(domainCode.getCode())
          .expiresAt(domainCode.getExpiresAt())
          .build();
    }

    PasswordResetCodeEntity saved = jpaRepository.save(entity);
    return toDomain(saved);
  }

  @Override
  public Optional<PasswordResetCode> findByUserId(UserId userId) {
    return jpaRepository.findByTenantUser_Id(userId.value())
        .map(this::toDomain);
  }

  @Override
  @Transactional
  public void markUsed(PasswordResetCode code) {
    jpaRepository.markUsedById(code.getId(), Instant.now());
  }

  private PasswordResetCode toDomain(PasswordResetCodeEntity entity) {
    return PasswordResetCode.reconstitute(
        entity.getId(),
        UserId.of(entity.getTenantUser().getId()),
        entity.getCode(),
        entity.getExpiresAt(),
        entity.getUsedAt(),
        entity.getCreatedAt());
  }
}

