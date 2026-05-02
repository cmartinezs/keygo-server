package io.cmartinezs.keygo.supabase.auth.mapper;

import io.cmartinezs.keygo.domain.auth.model.Session;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import io.cmartinezs.keygo.domain.auth.model.SessionStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.supabase.auth.entity.PlatformSessionEntity;
import io.cmartinezs.keygo.supabase.auth.entity.SessionEntity;
import io.cmartinezs.keygo.supabase.auth.entity.SigningKeyEntity;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SessionPersistenceMapper {

  public static Session toDomain(SessionEntity entity) {
    String signingKeyId =
        entity.getSigningKey() != null ? entity.getSigningKey().getId().toString() : null;
    ClientAppId clientAppId =
        entity.getClientApp() != null ? new ClientAppId(entity.getClientApp().getId()) : null;

    return Session.reconstitute(
        SessionId.from(entity.getId()),
        entity.getPlatformUser().getId(),
        isInternalPlatformSession(entity) ? null : clientAppId,
        SessionStatus.fromValue(entity.getStatus()),
        entity.getExpiresAt(),
        entity.getLastAccessedAt(),
        entity.getPlatformSession() != null ? entity.getPlatformSession().getUserAgent() : null,
        entity.getIssuedIpAddress() != null
            ? entity.getIssuedIpAddress()
            : entity.getPlatformSession() != null ? entity.getPlatformSession().getIpAddress() : null,
        entity.getCreatedAt(),
        signingKeyId);
  }

  public static SessionEntity toEntity(
      Session session,
      UUID tenantId,
      TenantUserEntity tenantUserEntity,
      PlatformSessionEntity platformSessionEntity,
      PlatformUserEntity platformUserEntity,
      ClientAppEntity clientAppEntity,
      SigningKeyEntity signingKeyEntity) {
    return SessionEntity.builder()
        .id(session.getId().value())
        .platformSession(platformSessionEntity)
        .platformUser(platformUserEntity)
        .tenantId(tenantId)
        .tenantUserId(tenantUserEntity != null ? tenantUserEntity.getId() : null)
        .tenantUser(tenantUserEntity)
        .clientAppId(clientAppEntity.getId())
        .clientApp(clientAppEntity)
        .signingKey(signingKeyEntity)
        .status(session.getStatus().getValue())
        .expiresAt(session.getExpiresAt())
        .lastAccessedAt(session.getLastAccessedAt())
        .issuedIpAddress(session.getIpAddress())
        .startedAt(session.getCreatedAt())
        .build();
  }

  private static boolean isInternalPlatformSession(SessionEntity entity) {
    return entity.getTenantUser() == null
        && entity.getClientApp() != null
        && entity.getClientApp().isInternal();
  }
}
