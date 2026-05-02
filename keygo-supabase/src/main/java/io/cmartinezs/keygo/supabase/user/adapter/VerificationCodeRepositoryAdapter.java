package io.cmartinezs.keygo.supabase.user.adapter;

import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.VerificationCode;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;
import io.cmartinezs.keygo.supabase.user.entity.PasswordResetTokenEntity;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import io.cmartinezs.keygo.supabase.user.entity.VerificationCodeEntity;
import io.cmartinezs.keygo.supabase.user.repository.PasswordResetTokenJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.PlatformUserJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.VerificationCodeJpaRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador JPA unificado para {@link VerificationCodeRepositoryPort}.
 *
 * <p>EMAIL_VERIFICATION y PASSWORD_RESET se almacenan como códigos en
 * {@code email_verifications}. PASSWORD_RECOVERY se almacena como token hashado
 * en {@code password_reset_tokens}.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class VerificationCodeRepositoryAdapter implements VerificationCodeRepositoryPort {

  private final VerificationCodeJpaRepository jpaRepository;
  private final PasswordResetTokenJpaRepository passwordResetTokenJpaRepository;
  private final PlatformUserJpaRepository platformUserJpaRepository;
  private final TenantUserJpaRepository tenantUserJpaRepository;

  public VerificationCodeRepositoryAdapter(
      VerificationCodeJpaRepository jpaRepository,
      PasswordResetTokenJpaRepository passwordResetTokenJpaRepository,
      TenantUserJpaRepository tenantUserJpaRepository,
      PlatformUserJpaRepository platformUserJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.passwordResetTokenJpaRepository = passwordResetTokenJpaRepository;
    this.tenantUserJpaRepository = tenantUserJpaRepository;
    this.platformUserJpaRepository = platformUserJpaRepository;
  }

  @Override
  @Transactional
  public VerificationCode upsert(VerificationCode domainCode) {
    UUID platformUserId = resolvePlatformUserId(domainCode.getUserId().value());
    return switch (domainCode.getPurpose()) {
      case EMAIL_VERIFICATION, PASSWORD_RESET ->
          upsertCodeVerification(domainCode, platformUserId);
      case PASSWORD_RECOVERY ->
          upsertPasswordRecoveryToken(domainCode, platformUserId);
    };
  }

  @Override
  public Optional<VerificationCode> findById(UUID id) {
    Optional<VerificationCode> codeVerification =
        jpaRepository.findById(id).map(entity -> toDomain(entity, VerificationPurpose.PASSWORD_RESET));
    if (codeVerification.isPresent()) {
      return codeVerification;
    }

    return passwordResetTokenJpaRepository
        .findById(id)
        .map(entity -> toDomain(entity, VerificationPurpose.PASSWORD_RECOVERY, entity.getTokenHash()));
  }

  @Override
  public Optional<VerificationCode> findByUserIdAndPurpose(UserId userId, VerificationPurpose purpose) {
    UUID platformUserId = resolvePlatformUserId(userId.value());
    return switch (purpose) {
      case EMAIL_VERIFICATION, PASSWORD_RESET ->
          jpaRepository.findByPlatformUser_Id(platformUserId)
              .map(entity -> toDomain(entity, purpose));
      case PASSWORD_RECOVERY ->
          passwordResetTokenJpaRepository.findByPlatformUser_Id(platformUserId)
              .map(entity -> toDomain(entity, purpose, entity.getTokenHash()));
    };
  }

  @Override
  public Optional<VerificationCode> findByCodeAndPurpose(String code, VerificationPurpose purpose) {
    return switch (purpose) {
      case EMAIL_VERIFICATION, PASSWORD_RESET ->
          jpaRepository.findByCode(code).map(entity -> toDomain(entity, purpose));
      case PASSWORD_RECOVERY ->
          passwordResetTokenJpaRepository
              .findByTokenHash(hashToken(code))
              .map(entity -> toDomain(entity, purpose, code));
    };
  }

  @Override
  @Transactional
  public void markUsed(VerificationCode code) {
    if (code.getPurpose() == VerificationPurpose.PASSWORD_RECOVERY) {
      passwordResetTokenJpaRepository.markUsedById(code.getId(), Instant.now());
      return;
    }
    jpaRepository.markUsedById(code.getId(), Instant.now());
  }

  @Override
  @Transactional
  public VerificationCode upsertIfExpiredOrAbsent(
      UserId userId, VerificationPurpose purpose, VerificationCode newCode) {
    if (purpose == VerificationPurpose.PASSWORD_RECOVERY) {
      return upsert(newCode);
    }

    UUID platformUserId = resolvePlatformUserId(userId.value());
    PlatformUserEntity userRef = platformUserJpaRepository.getReferenceById(platformUserId);
    Optional<VerificationCodeEntity> latestOpt = jpaRepository.findLatestPlatformUserWithLock(userRef);

    if (latestOpt.isPresent()) {
      VerificationCodeEntity latest = latestOpt.get();
      boolean expired = latest.getUsedAt() != null || latest.getExpiresAt().isBefore(Instant.now());
      if (!expired) {
        return toDomain(latest, purpose);
      }
    }

    return upsert(newCode);
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────

  private VerificationCode upsertCodeVerification(
      VerificationCode domainCode, UUID platformUserId) {
    Optional<VerificationCodeEntity> existing = jpaRepository.findByPlatformUser_Id(platformUserId);

    VerificationCodeEntity entity =
        existing.orElseGet(
            () ->
                VerificationCodeEntity.builder()
                    .platformUser(platformUserJpaRepository.getReferenceById(platformUserId))
                    .build());

    entity.setCode(domainCode.getCode());
    entity.setExpiresAt(domainCode.getExpiresAt());
    entity.setUsedAt(null);

    VerificationCodeEntity saved = jpaRepository.save(entity);
    return toDomain(saved, domainCode.getPurpose());
  }

  private VerificationCode upsertPasswordRecoveryToken(
      VerificationCode domainCode, UUID platformUserId) {
    Optional<PasswordResetTokenEntity> existing =
        passwordResetTokenJpaRepository.findByPlatformUser_Id(platformUserId);

    PasswordResetTokenEntity entity =
        existing.orElseGet(
            () ->
                PasswordResetTokenEntity.builder()
                    .platformUser(platformUserJpaRepository.getReferenceById(platformUserId))
                    .build());

    entity.setTokenHash(hashToken(domainCode.getCode()));
    entity.setExpiresAt(domainCode.getExpiresAt());
    entity.setUsedAt(null);

    PasswordResetTokenEntity saved = passwordResetTokenJpaRepository.save(entity);
    return toDomain(saved, domainCode.getPurpose(), domainCode.getCode());
  }

  private VerificationCode toDomain(
      VerificationCodeEntity entity, VerificationPurpose purpose) {
    return VerificationCode.reconstitute(
        entity.getId(),
        UserId.of(entity.getOwnerUserId()),
        purpose,
        entity.getCode(),
        entity.getExpiresAt(),
        entity.getUsedAt(),
        entity.getCreatedAt());
  }

  private VerificationCode toDomain(
      PasswordResetTokenEntity entity, VerificationPurpose purpose, String codeValue) {
    return VerificationCode.reconstitute(
        entity.getId(),
        UserId.of(entity.getPlatformUser().getId()),
        purpose,
        codeValue,
        entity.getExpiresAt(),
        entity.getUsedAt(),
        entity.getCreatedAt());
  }

  private UUID resolvePlatformUserId(UUID userId) {
    return tenantUserJpaRepository
        .findById(userId)
        .map(tenantUser -> tenantUser.getPlatformUser().getId())
        .orElse(userId);
  }

  private String hashToken(String rawToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder(bytes.length * 2);
      for (byte b : bytes) {
        builder.append(String.format("%02x", b));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
