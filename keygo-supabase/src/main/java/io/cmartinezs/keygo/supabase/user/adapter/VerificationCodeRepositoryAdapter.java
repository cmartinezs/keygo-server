package io.cmartinezs.keygo.supabase.user.adapter;

import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.VerificationCode;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import io.cmartinezs.keygo.supabase.user.entity.VerificationCodeEntity;
import io.cmartinezs.keygo.supabase.user.repository.PlatformUserJpaRepository;
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
 * <p>Soporta tanto usuarios de tenant ({@code tenant_user_id}) como usuarios de plataforma
 * ({@code platform_user_id}). Detecta automáticamente a qué tabla pertenece el {@code userId}.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class VerificationCodeRepositoryAdapter implements VerificationCodeRepositoryPort {

  private final VerificationCodeJpaRepository jpaRepository;
  private final TenantUserJpaRepository tenantUserJpaRepository;
  private final PlatformUserJpaRepository platformUserJpaRepository;

  public VerificationCodeRepositoryAdapter(
      VerificationCodeJpaRepository jpaRepository,
      TenantUserJpaRepository tenantUserJpaRepository,
      PlatformUserJpaRepository platformUserJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.tenantUserJpaRepository = tenantUserJpaRepository;
    this.platformUserJpaRepository = platformUserJpaRepository;
  }

  @Override
  @Transactional
  public VerificationCode upsert(VerificationCode domainCode) {
    UUID userId = domainCode.getUserId().value();
    boolean isPlatformUser = isPlatformUser(userId);

    Optional<VerificationCodeEntity> existing = isPlatformUser
        ? jpaRepository.findByPlatformUser_IdAndPurpose(userId, domainCode.getPurpose().name())
        : jpaRepository.findByTenantUser_IdAndPurpose(userId, domainCode.getPurpose().name());

    VerificationCodeEntity entity;
    if (existing.isPresent()) {
      entity = existing.get();
      entity.setCode(domainCode.getCode());
      entity.setExpiresAt(domainCode.getExpiresAt());
      entity.setUsedAt(null);
    } else {
      entity = toEntity(domainCode, isPlatformUser);
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
    UUID id = userId.value();
    Optional<VerificationCodeEntity> result = isPlatformUser(id)
        ? jpaRepository.findByPlatformUser_IdAndPurpose(id, purpose.name())
        : jpaRepository.findByTenantUser_IdAndPurpose(id, purpose.name());
    return result.map(this::toDomain);
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

    UUID id = userId.value();
    boolean isPlatform = isPlatformUser(id);

    Optional<VerificationCodeEntity> latestOpt;
    if (isPlatform) {
      PlatformUserEntity userRef = platformUserJpaRepository.getReferenceById(id);
      latestOpt = jpaRepository.findLatestPlatformUserWithLock(userRef, purpose.name());
    } else {
      TenantUserEntity userRef = tenantUserJpaRepository.getReferenceById(id);
      latestOpt = jpaRepository.findLatestWithLock(userRef, purpose.name());
    }

    if (latestOpt.isPresent()) {
      VerificationCodeEntity latest = latestOpt.get();
      boolean expired = latest.getUsedAt() != null
          || latest.getExpiresAt().isBefore(Instant.now());
      if (!expired) {
        return toDomain(latest);
      }
    }

    VerificationCodeEntity entity = toEntity(newCode, isPlatform);
    return toDomain(jpaRepository.save(entity));
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────

  private boolean isPlatformUser(UUID userId) {
    return platformUserJpaRepository.existsById(userId);
  }

  private VerificationCodeEntity toEntity(VerificationCode domain, boolean isPlatformUser) {
    UUID userId = domain.getUserId().value();
    var builder = VerificationCodeEntity.builder()
        .purpose(domain.getPurpose().name())
        .code(domain.getCode())
        .expiresAt(domain.getExpiresAt())
        .usedAt(domain.getUsedAt())
        .createdAt(domain.getCreatedAt());

    if (domain.getId() != null) {
      builder.id(domain.getId());
    }

    if (isPlatformUser) {
      builder.platformUser(platformUserJpaRepository.getReferenceById(userId));
    } else {
      builder.tenantUser(tenantUserJpaRepository.getReferenceById(userId));
    }

    return builder.build();
  }

  private VerificationCode toDomain(VerificationCodeEntity entity) {
    UUID ownerUserId = entity.getOwnerUserId();
    return VerificationCode.reconstitute(
        entity.getId(),
        UserId.of(ownerUserId),
        VerificationPurpose.valueOf(entity.getPurpose()),
        entity.getCode(),
        entity.getExpiresAt(),
        entity.getUsedAt(),
        entity.getCreatedAt());
  }
}
