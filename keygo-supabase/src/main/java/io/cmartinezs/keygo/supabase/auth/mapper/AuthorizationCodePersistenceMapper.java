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
import lombok.experimental.UtilityClass;

/**
 * Mapper: convierte entre AuthorizationCode (dominio) y AuthorizationCodeEntity (JPA).
 */
@UtilityClass
public class AuthorizationCodePersistenceMapper {

  /**
   * Convierte una entidad JPA a un agregado de dominio.
   *
   * @param entity entidad JPA
   * @return agregado de dominio
   */
  public static AuthorizationCode toDomain(AuthorizationCodeEntity entity) {
    // Reconstruir el code challenge
    CodeChallenge codeChallenge;
    if ("S256".equals(entity.getCodeChallengeMethod())) {
      codeChallenge = CodeChallenge.s256(entity.getCodeChallenge());
    } else {
      codeChallenge = CodeChallenge.plain(entity.getCodeChallenge());
    }

    // Agrupar parámetros en un record para cumplir límite de SonarQube (max 7 params)
    var params = new AuthorizationCodeParams(
        AuthorizationCodeId.from(entity.getId()),
        entity.getCode(),
        new TenantId(entity.getTenant().getId()),
        new ClientAppId(entity.getClientApp().getId()),
        new UserId(entity.getUser().getId()),
        entity.getRedirectUri(),
        ScopeSet.from(entity.getRequestedScopes()),
        codeChallenge,
        AuthorizationCodeStatus.fromValue(entity.getStatus()),
        entity.getExpiresAt(),
        entity.getUsedAt());

    return reconstructAuthorizationCode(params);
  }

  /**
   * Convierte un agregado de dominio a una entidad JPA.
   *
   * @param authorizationCode agregado de dominio
   * @param clientAppEntity entidad del cliente app
   * @param tenantEntity entidad del tenant
   * @param userEntity entidad del usuario
   * @return entidad JPA
   */
  public static AuthorizationCodeEntity toEntity(
      AuthorizationCode authorizationCode,
      io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity clientAppEntity,
      io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity tenantEntity,
      io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity userEntity) {
    return AuthorizationCodeEntity.builder()
        .id(authorizationCode.getId().id())
        .code(authorizationCode.getCode())
        .clientApp(clientAppEntity)
        .tenant(tenantEntity)
        .user(userEntity)
        .codeChallenge(authorizationCode.getCodeChallenge().getChallenge())
        .codeChallengeMethod(authorizationCode.getCodeChallenge().getMethod())
        .requestedScopes(authorizationCode.getScopes().asString())
        .redirectUri(authorizationCode.getRedirectUri())
        .status(authorizationCode.getStatus().getValue())
        .expiresAt(authorizationCode.getExpiresAt())
        .usedAt(authorizationCode.getUsedAt())
        .build();
  }

  /**
   * Reconstruye un AuthorizationCode usando reflexión para evitar exposición del constructor.
   *
   * <p>En una implementación real, esto debería usar un factory method en el dominio.
   */
  private static AuthorizationCode reconstructAuthorizationCode(AuthorizationCodeParams params) {
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
          params.id,
          params.code,
          params.tenantId,
          params.clientAppId,
          params.userId,
          params.redirectUri,
          params.scopes,
          params.codeChallenge,
          params.status,
          params.expiresAt,
          params.usedAt);
    } catch (Exception e) {
      throw new RuntimeException("Failed to reconstruct AuthorizationCode", e);
    }
  }

  /**
   * Record privado que encapsula los parámetros de AuthorizationCode.
   * Reduce los parámetros del método a 1, cumpliendo con el límite de SonarQube.
   */
  private record AuthorizationCodeParams(
      AuthorizationCodeId id,
      String code,
      TenantId tenantId,
      ClientAppId clientAppId,
      UserId userId,
      String redirectUri,
      ScopeSet scopes,
      CodeChallenge codeChallenge,
      AuthorizationCodeStatus status,
      java.time.Instant expiresAt,
      java.time.Instant usedAt) {}
}

