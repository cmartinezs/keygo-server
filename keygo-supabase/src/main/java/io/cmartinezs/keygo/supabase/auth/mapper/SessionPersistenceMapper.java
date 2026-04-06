package io.cmartinezs.keygo.supabase.auth.mapper;

import io.cmartinezs.keygo.domain.auth.model.Session;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import io.cmartinezs.keygo.domain.auth.model.SessionStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.supabase.auth.entity.SessionEntity;
import io.cmartinezs.keygo.supabase.auth.entity.SigningKeyEntity;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import lombok.experimental.UtilityClass;

/**
 * Mapper: convierte entre {@link Session} (dominio) y {@link SessionEntity} (JPA).
 *
 * <p>Modelo restructurado (RFC restructure-multitenant):
 * usa platformUser (nullable) y clientApp (nullable) en lugar de tenant/user.
 */
@UtilityClass
public class SessionPersistenceMapper {

  public static Session toDomain(SessionEntity entity) {
    String signingKeyId = entity.getSigningKey() != null
        ? entity.getSigningKey().getId().toString()
        : null;
    ClientAppId clientAppId = entity.getClientApp() != null
        ? new ClientAppId(entity.getClientApp().getId())
        : null;
    java.util.UUID platformUserId = entity.getPlatformUser() != null
        ? entity.getPlatformUser().getId()
        : null;

    return Session.reconstitute(
        SessionId.from(entity.getId()),
        platformUserId,
        clientAppId,
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
      PlatformUserEntity platformUserEntity,
      ClientAppEntity clientAppEntity,
      SigningKeyEntity signingKeyEntity) {
    return SessionEntity.builder()
        .id(session.getId().value())
        .platformUser(platformUserEntity)
        .clientApp(clientAppEntity)
        .status(session.getStatus().getValue())
        .expiresAt(session.getExpiresAt())
        .lastAccessedAt(session.getLastAccessedAt())
        .userAgent(session.getUserAgent())
        .ipAddress(session.getIpAddress())
        .signingKey(signingKeyEntity)
        .build();
  }
}
