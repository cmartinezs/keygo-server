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
    // Resolve TenantUserEntity (proxy with only the id is sufficient for FK)
    TenantUserEntity userProxy = tenantUserJpaRepository.getReferenceById(verification.getUserId().value());

    EmailVerificationEntity entity = EmailVerificationEntity.builder()
        .id(verification.getId())
        .tenantUser(userProxy)
        .code(verification.getCode())
        .expiresAt(verification.getExpiresAt())
        .usedAt(verification.getUsedAt())
        .createdAt(verification.getCreatedAt())
        .build();

    EmailVerificationEntity saved = jpaRepository.save(entity);
    return toDomain(saved, verification.getTenantId());
  }

  @Override
  public Optional<EmailVerification> findLatestByUserIdAndTenantId(UserId userId, TenantId tenantId) {
    // Look up the TenantUserEntity to use as the query parameter
    return tenantUserJpaRepository.findByIdAndTenantId(userId.value(), tenantId.value())
        .flatMap(userEntity ->
            jpaRepository.findTopByTenantUserOrderByCreatedAtDesc(userEntity)
                .map(ev -> toDomain(ev, tenantId)));
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

