package io.cmartinezs.keygo.supabase.user.adapter;

import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.VerificationCode;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import io.cmartinezs.keygo.supabase.user.entity.VerificationCodeEntity;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.VerificationCodeJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA unificado para {@link VerificationCodeRepositoryPort}.
 *
 * <p>Consolida los anteriores {@code EmailVerificationRepositoryAdapter},
 * {@code PasswordResetCodeRepositoryAdapter} y {@code PasswordRecoveryTokenRepositoryAdapter}.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class VerificationCodeRepositoryAdapter implements VerificationCodeRepositoryPort {

  private final VerificationCodeJpaRepository jpaRepository;
  private final TenantUserJpaRepository tenantUserJpaRepository;

  public VerificationCodeRepositoryAdapter(
      VerificationCodeJpaRepository jpaRepository,
      TenantUserJpaRepository tenantUserJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.tenantUserJpaRepository = tenantUserJpaRepository;
  }

  @Override
  @Transactional
  public VerificationCode upsert(VerificationCode domainCode) {
    TenantUserEntity userProxy = tenantUserJpaRepository.getReferenceById(domainCode.getUserId().value());

    Optional<VerificationCodeEntity> existing =
        jpaRepository.findByTenantUser_IdAndPurpose(
            domainCode.getUserId().value(),
            domainCode.getPurpose().name());

    VerificationCodeEntity entity;
    if (existing.isPresent()) {
      entity = existing.get();
      entity.setCode(domainCode.getCode());
      entity.setExpiresAt(domainCode.getExpiresAt());
      entity.setUsedAt(null);
    } else {
      entity = toEntity(domainCode, userProxy);
    }

    VerificationCodeEntity saved = jpaRepository.save(entity);
    return toDomain(saved);
  }

  @Override
  public Optional<VerificationCode> findById(UUID id) {
    return jpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<VerificationCode> findByUserIdAndPurpose(UserId userId, VerificationPurpose purpose) {
    return jpaRepository.findByTenantUser_IdAndPurpose(userId.value(), purpose.name())
        .map(this::toDomain);
  }

  @Override
  public Optional<VerificationCode> findByCodeAndPurpose(String code, VerificationPurpose purpose) {
    return jpaRepository.findByCodeAndPurpose(code, purpose.name())
        .map(this::toDomain);
  }

  @Override
  @Transactional
  public void markUsed(VerificationCode code) {
    jpaRepository.markUsedById(code.getId(), Instant.now());
  }

  @Override
  @Transactional
  public VerificationCode upsertIfExpiredOrAbsent(
      UserId userId, VerificationPurpose purpose, VerificationCode newCode) {

    Optional<TenantUserEntity> userEntityOpt =
        tenantUserJpaRepository.findById(userId.value());

    if (userEntityOpt.isPresent()) {
      TenantUserEntity userEntity = userEntityOpt.get();
      Optional<VerificationCodeEntity> latestOpt =
          jpaRepository.findLatestWithLock(userEntity, purpose.name());

      if (latestOpt.isPresent()) {
        VerificationCodeEntity latest = latestOpt.get();
        boolean expired = latest.getUsedAt() != null
            || latest.getExpiresAt().isBefore(Instant.now());
        if (!expired) {
          return toDomain(latest);
        }
      }
    }

    TenantUserEntity userProxy = tenantUserJpaRepository.getReferenceById(newCode.getUserId().value());
    VerificationCodeEntity entity = toEntity(newCode, userProxy);
    return toDomain(jpaRepository.save(entity));
  }

  private VerificationCodeEntity toEntity(VerificationCode domain, TenantUserEntity userProxy) {
    var builder = VerificationCodeEntity.builder()
        .tenantUser(userProxy)
        .purpose(domain.getPurpose().name())
        .code(domain.getCode())
        .expiresAt(domain.getExpiresAt())
        .usedAt(domain.getUsedAt())
        .createdAt(domain.getCreatedAt());
    if (domain.getId() != null) {
      builder.id(domain.getId());
    }
    return builder.build();
  }

  private VerificationCode toDomain(VerificationCodeEntity entity) {
    return VerificationCode.reconstitute(
        entity.getId(),
        UserId.of(entity.getTenantUser().getId()),
        VerificationPurpose.valueOf(entity.getPurpose()),
        entity.getCode(),
        entity.getExpiresAt(),
        entity.getUsedAt(),
        entity.getCreatedAt());
  }
}
