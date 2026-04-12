package io.cmartinezs.keygo.supabase.auth.mapper;

import io.cmartinezs.keygo.domain.auth.model.RefreshToken;
import io.cmartinezs.keygo.domain.auth.model.RefreshTokenId;
import io.cmartinezs.keygo.domain.auth.model.RefreshTokenStatus;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.supabase.auth.entity.RefreshTokenEntity;
import io.cmartinezs.keygo.supabase.auth.entity.SessionEntity;
import io.cmartinezs.keygo.supabase.auth.entity.SigningKeyEntity;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RefreshTokenPersistenceMapper {

  public static RefreshToken toDomain(RefreshTokenEntity entity) {
    RefreshTokenId replacedById =
        entity.getReplacedBy() != null ? RefreshTokenId.from(entity.getReplacedBy().getId()) : null;
    String signingKeyId =
        entity.getSigningKey() != null ? entity.getSigningKey().getId().toString() : null;
    ClientAppId clientAppId =
        entity.getClientApp() != null ? new ClientAppId(entity.getClientApp().getId()) : null;
    UUID tenantUserId = entity.getTenantUser() != null ? entity.getTenantUser().getId() : null;

    return RefreshToken.reconstitute(
        RefreshTokenId.from(entity.getId()),
        entity.getTokenHash(),
        isInternalPlatformToken(entity) ? null : clientAppId,
        tenantUserId,
        SessionId.from(entity.getSession().getId()),
        entity.getRequestedScopes(),
        RefreshTokenStatus.fromValue(entity.getStatus()),
        entity.getExpiresAt(),
        entity.getCreatedAt(),
        entity.getUsedAt(),
        replacedById,
        signingKeyId);
  }

  public static RefreshTokenEntity toEntity(
      RefreshToken refreshToken,
      ClientAppEntity clientAppEntity,
      TenantUserEntity tenantUserEntity,
      PlatformUserEntity platformUserEntity,
      UUID tenantId,
      SessionEntity sessionEntity,
      RefreshTokenEntity replacedByEntity,
      SigningKeyEntity signingKeyEntity) {
    return RefreshTokenEntity.builder()
        .id(refreshToken.getId().value())
        .tokenHash(refreshToken.getTokenHash())
        .session(sessionEntity)
        .platformUser(platformUserEntity)
        .tenantId(tenantId)
        .tenantUserId(tenantUserEntity != null ? tenantUserEntity.getId() : null)
        .tenantUser(tenantUserEntity)
        .clientAppId(clientAppEntity.getId())
        .clientApp(clientAppEntity)
        .signingKey(signingKeyEntity)
        .requestedScopes(refreshToken.getScopes())
        .status(refreshToken.getStatus().getValue())
        .expiresAt(refreshToken.getExpiresAt())
        .usedAt(refreshToken.getUsedAt())
        .replacedBy(replacedByEntity)
        .build();
  }

  private static boolean isInternalPlatformToken(RefreshTokenEntity entity) {
    return entity.getTenantUser() == null
        && entity.getClientApp() != null
        && entity.getClientApp().isInternal();
  }
}
