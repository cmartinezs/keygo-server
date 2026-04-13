package io.cmartinezs.keygo.supabase.auth.adapter;

import io.cmartinezs.keygo.app.auth.port.SessionRepositoryPort;
import io.cmartinezs.keygo.domain.auth.model.Session;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import io.cmartinezs.keygo.supabase.auth.entity.PlatformSessionEntity;
import io.cmartinezs.keygo.supabase.auth.entity.SigningKeyEntity;
import io.cmartinezs.keygo.supabase.auth.mapper.SessionPersistenceMapper;
import io.cmartinezs.keygo.supabase.auth.repository.PlatformSessionJpaRepository;
import io.cmartinezs.keygo.supabase.auth.repository.SessionJpaRepository;
import io.cmartinezs.keygo.supabase.auth.repository.SigningKeyJpaRepository;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.clientapp.repository.ClientAppJpaRepository;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import io.cmartinezs.keygo.supabase.user.repository.PlatformUserJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SessionRepositoryAdapter implements SessionRepositoryPort {

  private static final String INTERNAL_PLATFORM_CLIENT_ID = "keygo-ui";

  private final SessionJpaRepository sessionJpaRepository;
  private final PlatformSessionJpaRepository platformSessionJpaRepository;
  private final PlatformUserJpaRepository platformUserJpaRepository;
  private final TenantUserJpaRepository tenantUserJpaRepository;
  private final ClientAppJpaRepository clientAppJpaRepository;
  private final SigningKeyJpaRepository signingKeyJpaRepository;

  public SessionRepositoryAdapter(
      SessionJpaRepository sessionJpaRepository,
      PlatformSessionJpaRepository platformSessionJpaRepository,
      PlatformUserJpaRepository platformUserJpaRepository,
      TenantUserJpaRepository tenantUserJpaRepository,
      ClientAppJpaRepository clientAppJpaRepository,
      SigningKeyJpaRepository signingKeyJpaRepository) {
    this.sessionJpaRepository = sessionJpaRepository;
    this.platformSessionJpaRepository = platformSessionJpaRepository;
    this.platformUserJpaRepository = platformUserJpaRepository;
    this.tenantUserJpaRepository = tenantUserJpaRepository;
    this.clientAppJpaRepository = clientAppJpaRepository;
    this.signingKeyJpaRepository = signingKeyJpaRepository;
  }

  @Override
  @Transactional
  public Session save(Session session) {
    PlatformUserEntity platformUserEntity =
        platformUserJpaRepository
            .findById(session.getPlatformUserId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "PlatformUser not found: " + session.getPlatformUserId()));

    ClientAppEntity clientAppEntity =
        session.getClientAppId() != null
            ? clientAppJpaRepository
                .findById(session.getClientAppId().value())
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "ClientApp not found: " + session.getClientAppId().value()))
            : resolveInternalClientApp();

    UUID tenantId = clientAppEntity.getTenant().getId();
    TenantUserEntity tenantUserEntity = resolveTenantUser(session, tenantId);
    SigningKeyEntity signingKeyEntity = resolveSigningKey(session.getSigningKeyId());

    PlatformSessionEntity platformSessionEntity =
        PlatformSessionEntity.builder()
            .id(session.getId().value())
            .platformUser(platformUserEntity)
            .status(session.getStatus().getValue())
            .expiresAt(session.getExpiresAt())
            .lastAccessedAt(session.getLastAccessedAt())
            .userAgent(session.getUserAgent())
            .ipAddress(session.getIpAddress())
            .startedAt(session.getCreatedAt())
            .build();

    var oauthSession =
        SessionPersistenceMapper.toEntity(
            session,
            tenantId,
            tenantUserEntity,
            platformSessionJpaRepository.save(platformSessionEntity),
            platformUserEntity,
            clientAppEntity,
            signingKeyEntity);

    return SessionPersistenceMapper.toDomain(sessionJpaRepository.save(oauthSession));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Session> findById(SessionId sessionId) {
    return sessionJpaRepository.findById(sessionId.value()).map(SessionPersistenceMapper::toDomain);
  }

  @Override
  @Transactional
  public void update(Session session) {
    sessionJpaRepository
        .findById(session.getId().value())
        .ifPresent(
            entity -> {
              entity.setStatus(session.getStatus().getValue());
              entity.setLastAccessedAt(session.getLastAccessedAt());
              if (entity.getPlatformSession() != null) {
                entity.getPlatformSession().setStatus(session.getStatus().getValue());
                entity.getPlatformSession().setLastAccessedAt(session.getLastAccessedAt());
              }
              sessionJpaRepository.save(entity);
            });
  }

  @Override
  @Transactional(readOnly = true)
  public List<Session> findAllByPlatformUserId(UUID platformUserId) {
    return sessionJpaRepository.findAllByPlatformUserId(platformUserId).stream()
        .map(SessionPersistenceMapper::toDomain)
        .toList();
  }

  private ClientAppEntity resolveInternalClientApp() {
    return clientAppJpaRepository
        .findByClientId(INTERNAL_PLATFORM_CLIENT_ID)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Internal platform client app not found: " + INTERNAL_PLATFORM_CLIENT_ID));
  }

  private TenantUserEntity resolveTenantUser(Session session, UUID tenantId) {
    if (session.getClientAppId() == null) {
      return null;
    }
    return tenantUserJpaRepository
        .findByTenantIdAndPlatformUserId(tenantId, session.getPlatformUserId())
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "TenantUser not found for platformUser "
                        + session.getPlatformUserId()
                        + " in tenant "
                        + tenantId));
  }

  private SigningKeyEntity resolveSigningKey(String signingKeyId) {
    if (signingKeyId == null) {
      return null;
    }
    try {
      return signingKeyJpaRepository.getReferenceById(UUID.fromString(signingKeyId));
    } catch (IllegalArgumentException ignored) {
      return null;
    }
  }
}
