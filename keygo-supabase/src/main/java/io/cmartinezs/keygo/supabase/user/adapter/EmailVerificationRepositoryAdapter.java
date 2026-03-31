package io.cmartinezs.keygo.supabase.user.adapter;

import io.cmartinezs.keygo.app.user.port.EmailVerificationRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.EmailVerification;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.supabase.user.entity.EmailVerificationEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import io.cmartinezs.keygo.supabase.user.repository.EmailVerificationJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adapter implementing EmailVerificationRepositoryPort using Spring Data JPA.
 * <p>Adaptador que implementa EmailVerificationRepositoryPort usando Spring Data JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class EmailVerificationRepositoryAdapter implements EmailVerificationRepositoryPort {

  private final EmailVerificationJpaRepository jpaRepository;
  private final TenantUserJpaRepository tenantUserJpaRepository;

  public EmailVerificationRepositoryAdapter(
      EmailVerificationJpaRepository jpaRepository,
      TenantUserJpaRepository tenantUserJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.tenantUserJpaRepository = tenantUserJpaRepository;
  }

  @Override
  public EmailVerification save(EmailVerification verification) {
    TenantUserEntity userProxy = tenantUserJpaRepository.getReferenceById(verification.getUserId().value());
    EmailVerificationEntity entity = toEntity(verification, userProxy);
    EmailVerificationEntity saved = jpaRepository.save(entity);
    return toDomain(saved, verification.getTenantId());
  }

  @Override
  public Optional<EmailVerification> findLatestByUserIdAndTenantId(UserId userId, TenantId tenantId) {
    return tenantUserJpaRepository.findByIdAndTenantId(userId.value(), tenantId.value())
        .flatMap(userEntity ->
            jpaRepository.findTopByTenantUserOrderByCreatedAtDesc(userEntity)
                .map(ev -> toDomain(ev, tenantId)));
  }

  /**
   * Atomically checks if a valid (non-expired, non-used) verification exists for the user.
   * If one exists, returns it without persisting anything.
   * If none exists (or the latest is expired/used), saves {@code newVerification} and returns it.
   * <p>Uses a pessimistic write lock on the latest row to prevent concurrent duplicate inserts.
   */
  @Override
  @Transactional
  public EmailVerification saveIfExpiredOrAbsent(UserId userId, TenantId tenantId, EmailVerification newVerification) {
    Optional<TenantUserEntity> userEntityOpt =
        tenantUserJpaRepository.findByIdAndTenantId(userId.value(), tenantId.value());

    if (userEntityOpt.isPresent()) {
      TenantUserEntity userEntity = userEntityOpt.get();
      Optional<EmailVerificationEntity> latestOpt =
          jpaRepository.findTopByTenantUserOrderByCreatedAtDescWithLock(userEntity);

      if (latestOpt.isPresent()) {
        EmailVerificationEntity latest = latestOpt.get();
        boolean expired = latest.getUsedAt() != null
            || latest.getExpiresAt().isBefore(java.time.Instant.now());
        if (!expired) {
          // Still valid — return existing without saving a new row
          return toDomain(latest, tenantId);
        }
      }
    }

    // Expired or absent — persist the new verification
    TenantUserEntity userProxy = tenantUserJpaRepository.getReferenceById(newVerification.getUserId().value());
    EmailVerificationEntity entity = toEntity(newVerification, userProxy);
    return toDomain(jpaRepository.save(entity), tenantId);
  }

  private EmailVerificationEntity toEntity(EmailVerification verification, TenantUserEntity userProxy) {
    return EmailVerificationEntity.builder()
        .id(verification.getId())
        .tenantUser(userProxy)
        .code(verification.getCode())
        .expiresAt(verification.getExpiresAt())
        .usedAt(verification.getUsedAt())
        .createdAt(verification.getCreatedAt())
        .build();
  }

  private EmailVerification toDomain(EmailVerificationEntity entity, TenantId tenantId) {
    return EmailVerification.reconstitute(
        entity.getId(),
        UserId.of(entity.getTenantUser().getId()),
        tenantId,
        entity.getCode(),
        entity.getExpiresAt(),
        entity.getUsedAt(),
        entity.getCreatedAt());
  }
}



