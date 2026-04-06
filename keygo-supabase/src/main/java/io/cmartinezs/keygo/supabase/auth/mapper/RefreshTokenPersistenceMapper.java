package io.cmartinezs.keygo.supabase.auth.mapper;

import io.cmartinezs.keygo.domain.auth.model.RefreshToken;
import io.cmartinezs.keygo.domain.auth.model.RefreshTokenId;
import io.cmartinezs.keygo.domain.auth.model.RefreshTokenStatus;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.supabase.auth.entity.RefreshTokenEntity;
import io.cmartinezs.keygo.supabase.auth.entity.SessionEntity;
import io.cmartinezs.keygo.supabase.auth.entity.SigningKeyEntity;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import lombok.experimental.UtilityClass;

/**
 * Mapper: convierte entre {@link RefreshToken} (dominio) y {@link RefreshTokenEntity} (JPA).
 */
@UtilityClass
public class RefreshTokenPersistenceMapper {

  public static RefreshToken toDomain(RefreshTokenEntity entity) {
    RefreshTokenId replacedById = entity.getReplacedBy() != null
        ? RefreshTokenId.from(entity.getReplacedBy().getId())
        : null;
    String signingKeyId = entity.getSigningKey() != null
        ? entity.getSigningKey().getId().toString()
        : null;

    return RefreshToken.reconstitute(
        RefreshTokenId.from(entity.getId()),
        entity.getTokenHash(),
        new TenantId(entity.getTenant().getId()),
        new ClientAppId(entity.getClientApp().getId()),
        new UserId(entity.getUser().getId()),
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
      TenantEntity tenantEntity,
      ClientAppEntity clientAppEntity,
      TenantUserEntity userEntity,
      SessionEntity sessionEntity,
      RefreshTokenEntity replacedByEntity,
      SigningKeyEntity signingKeyEntity) {
    return RefreshTokenEntity.builder()
        .id(refreshToken.getId().value())
        .tokenHash(refreshToken.getTokenHash())
        .session(sessionEntity)
        .tenant(tenantEntity)
        .clientApp(clientAppEntity)
        .user(userEntity)
        .requestedScopes(refreshToken.getScopes())
        .status(refreshToken.getStatus().getValue())
        .expiresAt(refreshToken.getExpiresAt())
        .usedAt(refreshToken.getUsedAt())
        .replacedBy(replacedByEntity)
        .signingKey(signingKeyEntity)
        .build();
  }
}
