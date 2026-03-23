package io.cmartinezs.keygo.supabase.auth.adapter;

import io.cmartinezs.keygo.app.auth.port.RefreshTokenRepositoryPort;
import io.cmartinezs.keygo.domain.auth.model.RefreshToken;
import io.cmartinezs.keygo.domain.auth.model.RefreshTokenId;
import io.cmartinezs.keygo.domain.auth.model.RefreshTokenStatus;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import io.cmartinezs.keygo.supabase.auth.entity.RefreshTokenEntity;
import io.cmartinezs.keygo.supabase.auth.mapper.RefreshTokenPersistenceMapper;
import io.cmartinezs.keygo.supabase.auth.repository.RefreshTokenJpaRepository;
import io.cmartinezs.keygo.supabase.auth.repository.SessionJpaRepository;
import io.cmartinezs.keygo.supabase.clientapp.repository.ClientAppJpaRepository;
import io.cmartinezs.keygo.supabase.tenant.repository.TenantJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador: implementación de {@link RefreshTokenRepositoryPort} usando JPA.
 */
@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

  private final RefreshTokenJpaRepository refreshTokenJpaRepository;
  private final SessionJpaRepository sessionJpaRepository;
  private final TenantJpaRepository tenantJpaRepository;
  private final ClientAppJpaRepository clientAppJpaRepository;
  private final TenantUserJpaRepository tenantUserJpaRepository;

  public RefreshTokenRepositoryAdapter(
      RefreshTokenJpaRepository refreshTokenJpaRepository,
      SessionJpaRepository sessionJpaRepository,
      TenantJpaRepository tenantJpaRepository,
      ClientAppJpaRepository clientAppJpaRepository,
      TenantUserJpaRepository tenantUserJpaRepository) {
    this.refreshTokenJpaRepository = refreshTokenJpaRepository;
    this.sessionJpaRepository = sessionJpaRepository;
    this.tenantJpaRepository = tenantJpaRepository;
    this.clientAppJpaRepository = clientAppJpaRepository;
    this.tenantUserJpaRepository = tenantUserJpaRepository;
  }

  @Override
  public RefreshToken save(RefreshToken refreshToken) {
    var tenantEntity = tenantJpaRepository.findById(refreshToken.getTenantId().value())
        .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + refreshToken.getTenantId().value()));
    var clientAppEntity = clientAppJpaRepository.findById(refreshToken.getClientAppId().value())
        .orElseThrow(() -> new IllegalArgumentException("ClientApp not found: " + refreshToken.getClientAppId().value()));
    var userEntity = tenantUserJpaRepository.findById(refreshToken.getUserId().value())
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + refreshToken.getUserId().value()));
    var sessionEntity = sessionJpaRepository.findById(refreshToken.getSessionId().value())
        .orElseThrow(() -> new IllegalArgumentException("Session not found: " + refreshToken.getSessionId().value()));

    RefreshTokenEntity replacedByEntity = null;
    if (refreshToken.getReplacedByTokenId() != null) {
      replacedByEntity = refreshTokenJpaRepository.findById(refreshToken.getReplacedByTokenId().value())
          .orElse(null);
    }

    var entity = RefreshTokenPersistenceMapper.toEntity(
        refreshToken, tenantEntity, clientAppEntity, userEntity, sessionEntity, replacedByEntity);
    var saved = refreshTokenJpaRepository.save(entity);
    return RefreshTokenPersistenceMapper.toDomain(saved);
  }

  @Override
  public Optional<RefreshToken> findByTokenHash(String tokenHash) {
    return refreshTokenJpaRepository.findByTokenHash(tokenHash)
        .map(RefreshTokenPersistenceMapper::toDomain);
  }

  @Override
  public Optional<RefreshToken> findById(RefreshTokenId id) {
    return refreshTokenJpaRepository.findById(id.value())
        .map(RefreshTokenPersistenceMapper::toDomain);
  }

  @Override
  public void update(RefreshToken refreshToken) {
    refreshTokenJpaRepository.findById(refreshToken.getId().value()).ifPresent(entity -> {
      entity.setStatus(refreshToken.getStatus().getValue());
      entity.setUsedAt(refreshToken.getUsedAt());
      if (refreshToken.getReplacedByTokenId() != null) {
        refreshTokenJpaRepository.findById(refreshToken.getReplacedByTokenId().value())
            .ifPresent(entity::setReplacedBy);
      }
      refreshTokenJpaRepository.save(entity);
    });
  }

  @Override
  @Transactional
  public List<RefreshToken> revokeAllForSession(SessionId sessionId) {
    refreshTokenJpaRepository.revokeAllActiveBySessionId(sessionId.value());
    return refreshTokenJpaRepository.findBySessionId(sessionId.value()).stream()
        .filter(e -> RefreshTokenStatus.REVOKED.getValue().equals(e.getStatus()))
        .map(RefreshTokenPersistenceMapper::toDomain)
        .toList();
  }
}

