package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.port.ClockPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.TokenClaimsFactoryPort;
import io.cmartinezs.keygo.app.auth.port.TokenSignerPort;
import io.cmartinezs.keygo.app.auth.result.IssueTokensResult;
import io.cmartinezs.keygo.domain.auth.exception.NoActiveSigningKeyException;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: emitir access_token e id_token JWT firmados con RS256.
 *
 * <p>Requiere que exista al menos una {@link SigningKey} con estado {@code ACTIVE}.
 * Primero intenta encontrar una clave tenant-específica; si no hay, usa la clave global.
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
   * @param tenantId            tenant propietario (para resolver la clave de firma correcta; nullable)
   * @param issuer              URL del emisor
   * @param subject             identificador del usuario (UUID como String)
   * @param audience            client_id de la app cliente
   * @param scope               scopes otorgados
   * @param nonce               nonce del flujo (puede ser null)
   * @param email               email del usuario (puede ser null)
   * @param name                nombre completo (puede ser null)
   * @param authorizationCodeId ID del código canjeado
   * @param roles               roles del usuario en la app
   * @return resultado con tokens y el ID de la clave usada
   * @throws NoActiveSigningKeyException si no hay clave activa
   */
  public IssueTokensResult execute(
      TenantId tenantId,
      String issuer,
      String subject,
      String audience,
      String scope,
      String nonce,
      String email,
      String name,
      String authorizationCodeId,
      List<String> roles) {

    // Obtener clave activa: tenant-scoped con fallback global
    SigningKey signingKey = (tenantId != null
        ? signingKeyRepository.findActiveKeyForTenant(tenantId)
        : signingKeyRepository.findActiveKey())
        .orElseThrow(NoActiveSigningKeyException::new);

    Instant now = clock.now();
    Instant expiresAt = now.plus(ACCESS_TOKEN_TTL);
    String accessJti = UUID.randomUUID().toString();
    String idJti = UUID.randomUUID().toString();

    var accessClaims = new LinkedHashMap<>(
        tokenClaimsFactory.buildAccessTokenClaims(
            issuer, subject, audience, scope, accessJti, now, expiresAt, roles));
    String tenantSlug = extractTenantSlugFromIssuer(issuer);
    if (tenantSlug != null) {
      accessClaims.put("tenant_slug", tenantSlug);
    }
    String accessToken = tokenSigner.signJwt(accessClaims, signingKey);

    var idClaims = tokenClaimsFactory.buildIdTokenClaims(
        issuer, subject, audience, idJti, now, expiresAt, nonce, email, name, accessToken, roles);
    String idToken = tokenSigner.signJwt(idClaims, signingKey);

    // El UUID de la clave firmante se devuelve para que la capa superior
    // lo registre en sesión y refresh token (auditoría)
    String signingKeyId = signingKey.getId().value();

    return new IssueTokensResult(
        accessToken, idToken, "Bearer", ACCESS_TOKEN_TTL.toSeconds(), scope,
        authorizationCodeId, signingKeyId);
  }

  private String extractTenantSlugFromIssuer(String issuer) {
    if (issuer == null || issuer.isBlank()) return null;
    String marker = "/api/v1/tenants/";
    int markerIndex = issuer.indexOf(marker);
    if (markerIndex < 0) return null;
    String tail = issuer.substring(markerIndex + marker.length());
    int slashIndex = tail.indexOf('/');
    return slashIndex >= 0 ? tail.substring(0, slashIndex) : tail;
  }
}
