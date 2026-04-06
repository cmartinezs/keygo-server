package io.cmartinezs.keygo.supabase.auth.mapper;

import io.cmartinezs.keygo.domain.auth.model.Session;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import io.cmartinezs.keygo.domain.auth.model.SessionStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.supabase.auth.entity.SessionEntity;
import io.cmartinezs.keygo.supabase.auth.entity.SigningKeyEntity;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import lombok.experimental.UtilityClass;

/**
 * Mapper: convierte entre {@link Session} (dominio) y {@link SessionEntity} (JPA).
 */
@UtilityClass
public class SessionPersistenceMapper {

  public static Session toDomain(SessionEntity entity) {
    String signingKeyId = entity.getSigningKey() != null
        ? entity.getSigningKey().getId().toString()
        : null;
    return Session.reconstitute(
        SessionId.from(entity.getId()),
        new TenantId(entity.getTenant().getId()),
        new ClientAppId(entity.getClientApp().getId()),
        new UserId(entity.getUser().getId()),
        SessionStatus.fromValue(entity.getStatus()),
        entity.getExpiresAt(),
        entity.getLastAccessedAt(),
        entity.getUserAgent(),
        entity.getIpAddress(),
        entity.getCreatedAt(),
        signingKeyId);
  }

  public static SessionEntity toEntity(
      Session session,
      TenantEntity tenantEntity,
      ClientAppEntity clientAppEntity,
      TenantUserEntity userEntity,
      SigningKeyEntity signingKeyEntity) {
    return SessionEntity.builder()
        .id(session.getId().value())
        .tenant(tenantEntity)
        .clientApp(clientAppEntity)
        .user(userEntity)
        .status(session.getStatus().getValue())
        .expiresAt(session.getExpiresAt())
        .lastAccessedAt(session.getLastAccessedAt())
        .userAgent(session.getUserAgent())
        .ipAddress(session.getIpAddress())
        .signingKey(signingKeyEntity)
        .build();
  }
}
