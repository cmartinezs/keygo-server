package io.cmartinezs.keygo.supabase.auth.adapter;

import io.cmartinezs.keygo.app.auth.port.SessionRepositoryPort;
import io.cmartinezs.keygo.domain.auth.model.Session;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import io.cmartinezs.keygo.supabase.auth.entity.SigningKeyEntity;
import io.cmartinezs.keygo.supabase.auth.mapper.SessionPersistenceMapper;
import io.cmartinezs.keygo.supabase.auth.repository.SessionJpaRepository;
import io.cmartinezs.keygo.supabase.auth.repository.SigningKeyJpaRepository;
import io.cmartinezs.keygo.supabase.clientapp.repository.ClientAppJpaRepository;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import io.cmartinezs.keygo.supabase.user.repository.PlatformUserJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Adaptador: implementación de {@link SessionRepositoryPort} usando JPA.
 *
 * <p>Modelo restructurado (RFC restructure-multitenant):
 * sesiones tienen platformUser (nullable) y clientApp (nullable).
 */
@Component
public class SessionRepositoryAdapter implements SessionRepositoryPort {

  private final SessionJpaRepository sessionJpaRepository;
  private final PlatformUserJpaRepository platformUserJpaRepository;
  private final ClientAppJpaRepository clientAppJpaRepository;
  private final SigningKeyJpaRepository signingKeyJpaRepository;

  public SessionRepositoryAdapter(
      SessionJpaRepository sessionJpaRepository,
      PlatformUserJpaRepository platformUserJpaRepository,
      ClientAppJpaRepository clientAppJpaRepository,
      SigningKeyJpaRepository signingKeyJpaRepository) {
    this.sessionJpaRepository = sessionJpaRepository;
    this.platformUserJpaRepository = platformUserJpaRepository;
    this.clientAppJpaRepository = clientAppJpaRepository;
    this.signingKeyJpaRepository = signingKeyJpaRepository;
  }

  @Override
  public Session save(Session session) {
    // Resolver platformUser (nullable)
    PlatformUserEntity platformUserEntity = null;
    if (session.getPlatformUserId() != null) {
      platformUserEntity = platformUserJpaRepository.findById(session.getPlatformUserId())
          .orElse(null);
    }

    // Resolver clientApp (nullable — null = sesión de plataforma)
    var clientAppEntity = session.getClientAppId() != null
        ? clientAppJpaRepository.findById(session.getClientAppId().value())
            .orElseThrow(() -> new IllegalArgumentException("ClientApp not found: " + session.getClientAppId().value()))
        : null;

    // Resolución opcional del SigningKey para auditoría
    SigningKeyEntity signingKeyEntity = null;
    if (session.getSigningKeyId() != null) {
      try {
        UUID skId = UUID.fromString(session.getSigningKeyId());
        signingKeyEntity = signingKeyJpaRepository.getReferenceById(skId);
      } catch (IllegalArgumentException ignored) {
        // UUID inválido — no bloqueante, se persiste sin la referencia
      }
    }

    var entity = SessionPersistenceMapper.toEntity(
        session, platformUserEntity, clientAppEntity, signingKeyEntity);
    var saved = sessionJpaRepository.save(entity);
    return SessionPersistenceMapper.toDomain(saved);
  }

  @Override
  public Optional<Session> findById(SessionId sessionId) {
    return sessionJpaRepository.findById(sessionId.value())
        .map(SessionPersistenceMapper::toDomain);
  }

  @Override
  public void update(Session session) {
    sessionJpaRepository.findById(session.getId().value()).ifPresent(entity -> {
      entity.setStatus(session.getStatus().getValue());
      entity.setLastAccessedAt(session.getLastAccessedAt());
      sessionJpaRepository.save(entity);
    });
  }

  @Override
  public List<Session> findAllByPlatformUserId(UUID platformUserId) {
    return sessionJpaRepository
        .findAllByPlatformUserId(platformUserId)
        .stream()
        .map(SessionPersistenceMapper::toDomain)
        .toList();
  }
}
