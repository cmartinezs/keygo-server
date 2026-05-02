package io.cmartinezs.keygo.supabase.auth.mapper;

import io.cmartinezs.keygo.domain.auth.model.AuthorizationCode;
import io.cmartinezs.keygo.domain.auth.model.AuthorizationCodeId;
import io.cmartinezs.keygo.domain.auth.model.AuthorizationCodeStatus;
import io.cmartinezs.keygo.domain.auth.model.CodeChallenge;
import io.cmartinezs.keygo.domain.auth.model.ScopeSet;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.supabase.auth.entity.AuthorizationCodeEntity;
import io.cmartinezs.keygo.supabase.auth.exception.AuthorizationCodeMappingException;
import io.cmartinezs.keygo.supabase.auth.entity.PlatformSessionEntity;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthorizationCodePersistenceMapper {

  public static AuthorizationCode toDomain(AuthorizationCodeEntity entity, String plainCode) {
    CodeChallenge codeChallenge =
        "S256".equals(entity.getCodeChallengeMethod())
            ? CodeChallenge.s256(entity.getCodeChallenge())
            : CodeChallenge.plain(entity.getCodeChallenge());

    try {
      var constructor =
          AuthorizationCode.class.getDeclaredConstructor(
              AuthorizationCodeId.class,
              String.class,
              TenantId.class,
              ClientAppId.class,
              UserId.class,
              String.class,
              ScopeSet.class,
              CodeChallenge.class,
              AuthorizationCodeStatus.class,
              java.time.Instant.class,
              java.time.Instant.class);
      constructor.setAccessible(true);
      return constructor.newInstance(
          AuthorizationCodeId.from(entity.getId()),
          plainCode,
          new TenantId(entity.getTenantId()),
          new ClientAppId(entity.getClientApp().getId()),
          new UserId(entity.getTenantUser().getId()),
          entity.getRedirectUri(),
          ScopeSet.from(entity.getRequestedScopes()),
          codeChallenge,
          AuthorizationCodeStatus.fromValue(entity.getStatus()),
          entity.getExpiresAt(),
          entity.getUsedAt());
    } catch (Exception e) {
      throw new AuthorizationCodeMappingException(e);
    }
  }

  public static AuthorizationCodeEntity toEntity(
      AuthorizationCode authorizationCode,
      String codeHash,
      PlatformSessionEntity platformSessionEntity,
      PlatformUserEntity platformUserEntity,
      ClientAppEntity clientAppEntity,
      TenantUserEntity userEntity) {
    return AuthorizationCodeEntity.builder()
        .id(authorizationCode.getId().id())
        .codeHash(codeHash)
        .platformSession(platformSessionEntity)
        .platformUser(platformUserEntity)
        .tenantId(authorizationCode.getTenantId().value())
        .tenantUserId(userEntity.getId())
        .tenantUser(userEntity)
        .clientAppId(clientAppEntity.getId())
        .clientApp(clientAppEntity)
        .redirectUri(authorizationCode.getRedirectUri())
        .requestedScopes(authorizationCode.getScopes().asString())
        .codeChallenge(authorizationCode.getCodeChallenge().getChallenge())
        .codeChallengeMethod(authorizationCode.getCodeChallenge().getMethod())
        .status(authorizationCode.getStatus().getValue())
        .expiresAt(authorizationCode.getExpiresAt())
        .usedAt(authorizationCode.getUsedAt())
        .build();
  }
}
