package io.cmartinezs.keygo.supabase.auth.adapter;

import io.cmartinezs.keygo.app.auth.port.RefreshTokenRepositoryPort;
import io.cmartinezs.keygo.domain.auth.model.RefreshToken;
import io.cmartinezs.keygo.domain.auth.model.RefreshTokenId;
import io.cmartinezs.keygo.domain.auth.model.RefreshTokenStatus;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import io.cmartinezs.keygo.supabase.auth.entity.RefreshTokenEntity;
import io.cmartinezs.keygo.supabase.auth.entity.SigningKeyEntity;
import io.cmartinezs.keygo.supabase.auth.mapper.RefreshTokenPersistenceMapper;
import io.cmartinezs.keygo.supabase.auth.repository.RefreshTokenJpaRepository;
import io.cmartinezs.keygo.supabase.auth.repository.SessionJpaRepository;
import io.cmartinezs.keygo.supabase.auth.repository.SigningKeyJpaRepository;
import io.cmartinezs.keygo.supabase.clientapp.repository.ClientAppJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

  private final RefreshTokenJpaRepository refreshTokenJpaRepository;
  private final SessionJpaRepository sessionJpaRepository;
  private final ClientAppJpaRepository clientAppJpaRepository;
  private final TenantUserJpaRepository tenantUserJpaRepository;
  private final SigningKeyJpaRepository signingKeyJpaRepository;

  public RefreshTokenRepositoryAdapter(
      RefreshTokenJpaRepository refreshTokenJpaRepository,
      SessionJpaRepository sessionJpaRepository,
      ClientAppJpaRepository clientAppJpaRepository,
      TenantUserJpaRepository tenantUserJpaRepository,
      SigningKeyJpaRepository signingKeyJpaRepository) {
    this.refreshTokenJpaRepository = refreshTokenJpaRepository;
    this.sessionJpaRepository = sessionJpaRepository;
    this.clientAppJpaRepository = clientAppJpaRepository;
    this.tenantUserJpaRepository = tenantUserJpaRepository;
    this.signingKeyJpaRepository = signingKeyJpaRepository;
  }

  @Override
  public RefreshToken save(RefreshToken refreshToken) {
    var sessionEntity =
        sessionJpaRepository
            .findById(refreshToken.getSessionId().value())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Session not found: " + refreshToken.getSessionId().value()));

    var clientAppEntity =
        refreshToken.getClientAppId() != null
            ? clientAppJpaRepository
                .findById(refreshToken.getClientAppId().value())
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "ClientApp not found: " + refreshToken.getClientAppId().value()))
            : sessionEntity.getClientApp();

    var tenantUserEntity =
        refreshToken.getTenantUserId() != null
            ? tenantUserJpaRepository.findById(refreshToken.getTenantUserId()).orElse(null)
            : sessionEntity.getTenantUser();

    RefreshTokenEntity replacedByEntity = null;
    if (refreshToken.getReplacedByTokenId() != null) {
      replacedByEntity =
          refreshTokenJpaRepository.findById(refreshToken.getReplacedByTokenId().value()).orElse(null);
    }

    SigningKeyEntity signingKeyEntity = null;
    if (refreshToken.getSigningKeyId() != null) {
      try {
        signingKeyEntity =
            signingKeyJpaRepository.getReferenceById(UUID.fromString(refreshToken.getSigningKeyId()));
      } catch (IllegalArgumentException ignored) {
        signingKeyEntity = null;
      }
    }

    var entity =
        RefreshTokenPersistenceMapper.toEntity(
            refreshToken,
            clientAppEntity,
            tenantUserEntity,
            sessionEntity.getPlatformUser(),
            sessionEntity.getTenantId(),
            sessionEntity,
            replacedByEntity,
            signingKeyEntity);

    return RefreshTokenPersistenceMapper.toDomain(refreshTokenJpaRepository.save(entity));
  }

  @Override
  public Optional<RefreshToken> findByTokenHash(String tokenHash) {
    return refreshTokenJpaRepository.findByTokenHash(tokenHash)
        .map(RefreshTokenPersistenceMapper::toDomain);
  }

  @Override
  public Optional<RefreshToken> findById(RefreshTokenId id) {
    return refreshTokenJpaRepository.findById(id.value()).map(RefreshTokenPersistenceMapper::toDomain);
  }

  @Override
  public void update(RefreshToken refreshToken) {
    refreshTokenJpaRepository
        .findById(refreshToken.getId().value())
        .ifPresent(
            entity -> {
              entity.setStatus(refreshToken.getStatus().getValue());
              entity.setUsedAt(refreshToken.getUsedAt());
              if (refreshToken.getReplacedByTokenId() != null) {
                refreshTokenJpaRepository
                    .findById(refreshToken.getReplacedByTokenId().value())
                    .ifPresent(entity::setReplacedBy);
              }
              refreshTokenJpaRepository.save(entity);
            });
  }

  @Override
  @Transactional
  public List<RefreshToken> revokeAllForSession(SessionId sessionId) {
    refreshTokenJpaRepository.revokeAllActiveBySessionId(sessionId.value());
    return refreshTokenJpaRepository.findBySession_Id(sessionId.value()).stream()
        .filter(entity -> RefreshTokenStatus.REVOKED.getValue().equals(entity.getStatus()))
        .map(RefreshTokenPersistenceMapper::toDomain)
        .toList();
  }
}
