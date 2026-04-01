package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.port.ClockPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.TokenClaimsFactoryPort;
import io.cmartinezs.keygo.app.auth.port.TokenSignerPort;
import io.cmartinezs.keygo.app.auth.result.IssueTokensResult;
import io.cmartinezs.keygo.domain.auth.exception.NoActiveSigningKeyException;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: emitir access_token e id_token JWT firmados con RS256.
 *
 * <p>Requiere que exista al menos una {@link SigningKey} con estado {@code ACTIVE}. Si no la hay,
 * lanza {@link NoActiveSigningKeyException}.
 *
 * <p>Tiempo de vida del access_token: 1 hora.
 * Tiempo de vida del id_token: 1 hora (mismo que access_token).
 */
public class IssueTokensUseCase {

  private static final Duration ACCESS_TOKEN_TTL = Duration.ofHours(1);

  private final SigningKeyRepositoryPort signingKeyRepository;
  private final TokenSignerPort tokenSigner;
  private final TokenClaimsFactoryPort tokenClaimsFactory;
  private final ClockPort clock;

  public IssueTokensUseCase(
      SigningKeyRepositoryPort signingKeyRepository,
      TokenSignerPort tokenSigner,
      TokenClaimsFactoryPort tokenClaimsFactory,
      ClockPort clock) {
    this.signingKeyRepository = signingKeyRepository;
    this.tokenSigner = tokenSigner;
    this.tokenClaimsFactory = tokenClaimsFactory;
    this.clock = clock;
  }

  /**
   * Emite un par access_token + id_token para el usuario autenticado.
   *
   * @param issuer              URL del emisor (incluye context-path y slug del tenant)
   * @param subject             identificador del usuario (UUID como String)
   * @param audience            client_id de la app cliente
   * @param scope               scopes otorgados, separados por espacio
   * @param nonce               nonce del flujo de autorización (puede ser null)
   * @param email               email del usuario (puede ser null)
   * @param name                nombre completo del usuario (puede ser null)
   * @param authorizationCodeId ID del código canjeado (para auditoría)
   * @param roles               lista de roles del usuario en la app (puede ser null o vacío)
   * @return resultado con access_token, id_token y metadata
   * @throws NoActiveSigningKeyException si no hay clave activa
   */
  public IssueTokensResult execute(
      String issuer,
      String subject,
      String audience,
      String scope,
      String nonce,
      String email,
      String name,
      String authorizationCodeId,
      List<String> roles) {

    // Given: obtener clave activa
    SigningKey signingKey =
        signingKeyRepository
            .findActiveKey()
            .orElseThrow(NoActiveSigningKeyException::new);

    // When: calcular tiempos
    Instant now = clock.now();
    Instant expiresAt = now.plus(ACCESS_TOKEN_TTL);
    String accessJti = UUID.randomUUID().toString();
    String idJti = UUID.randomUUID().toString();

    // Then: construir y firmar access_token
    var accessClaims = new LinkedHashMap<>(
        tokenClaimsFactory.buildAccessTokenClaims(
            issuer, subject, audience, scope, accessJti, now, expiresAt, roles));
    String tenantSlug = extractTenantSlugFromIssuer(issuer);
    if (tenantSlug != null) {
      accessClaims.put("tenant_slug", tenantSlug);
    }
    String accessToken = tokenSigner.signJwt(accessClaims, signingKey);

    // Then: construir y firmar id_token (incluye at_hash del access_token)
    var idClaims =
        tokenClaimsFactory.buildIdTokenClaims(
            issuer, subject, audience, idJti, now, expiresAt, nonce, email, name, accessToken, roles);
    String idToken = tokenSigner.signJwt(idClaims, signingKey);

    return new IssueTokensResult(
        accessToken, idToken, "Bearer", ACCESS_TOKEN_TTL.toSeconds(), scope, authorizationCodeId);
  }

  private String extractTenantSlugFromIssuer(String issuer) {
    if (issuer == null || issuer.isBlank()) {
      return null;
    }
    String marker = "/api/v1/tenants/";
    int markerIndex = issuer.indexOf(marker);
    if (markerIndex < 0) {
      return null;
    }
    String tail = issuer.substring(markerIndex + marker.length());
    int slashIndex = tail.indexOf('/');
    return slashIndex >= 0 ? tail.substring(0, slashIndex) : tail;
  }
}

