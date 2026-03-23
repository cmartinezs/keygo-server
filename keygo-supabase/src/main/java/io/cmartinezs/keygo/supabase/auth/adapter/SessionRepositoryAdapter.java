package io.cmartinezs.keygo.supabase.auth.adapter;

import io.cmartinezs.keygo.app.auth.port.SessionRepositoryPort;
import io.cmartinezs.keygo.domain.auth.model.Session;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import io.cmartinezs.keygo.supabase.auth.entity.SessionEntity;
import io.cmartinezs.keygo.supabase.auth.mapper.SessionPersistenceMapper;
import io.cmartinezs.keygo.supabase.auth.repository.SessionJpaRepository;
import io.cmartinezs.keygo.supabase.clientapp.repository.ClientAppJpaRepository;
import io.cmartinezs.keygo.supabase.tenant.repository.TenantJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Adaptador: implementación de {@link SessionRepositoryPort} usando JPA.
 */
@Component
public class SessionRepositoryAdapter implements SessionRepositoryPort {

  private final SessionJpaRepository sessionJpaRepository;
  private final TenantJpaRepository tenantJpaRepository;
  private final ClientAppJpaRepository clientAppJpaRepository;
  private final TenantUserJpaRepository tenantUserJpaRepository;

  public SessionRepositoryAdapter(
      SessionJpaRepository sessionJpaRepository,
      TenantJpaRepository tenantJpaRepository,
      ClientAppJpaRepository clientAppJpaRepository,
      TenantUserJpaRepository tenantUserJpaRepository) {
    this.sessionJpaRepository = sessionJpaRepository;
    this.tenantJpaRepository = tenantJpaRepository;
    this.clientAppJpaRepository = clientAppJpaRepository;
    this.tenantUserJpaRepository = tenantUserJpaRepository;
  }

  @Override
  public Session save(Session session) {
    var tenantEntity = tenantJpaRepository.findById(session.getTenantId().value())
        .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + session.getTenantId().value()));
    var clientAppEntity = clientAppJpaRepository.findById(session.getClientAppId().value())
        .orElseThrow(() -> new IllegalArgumentException("ClientApp not found: " + session.getClientAppId().value()));
    var userEntity = tenantUserJpaRepository.findById(session.getUserId().value())
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + session.getUserId().value()));

    SessionEntity entity = SessionPersistenceMapper.toEntity(session, tenantEntity, clientAppEntity, userEntity);
    SessionEntity saved = sessionJpaRepository.save(entity);
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
}

