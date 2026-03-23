package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.command.IssueClientCredentialsTokenCommand;
import io.cmartinezs.keygo.app.auth.port.ClockPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.TokenClaimsFactoryPort;
import io.cmartinezs.keygo.app.auth.port.TokenSignerPort;
import io.cmartinezs.keygo.app.auth.result.IssueClientCredentialsTokenResult;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.clientapp.port.ClientSecretEncoderPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.auth.exception.NoActiveSigningKeyException;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAuthenticationException;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedScope;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso: emitir un access_token JWT para el flujo OAuth2 client_credentials (M2M).
 *
 * <p>Flujo:
 * <ol>
 *   <li>Buscar y validar el tenant (activo y no suspendido)</li>
 *   <li>Buscar la app cliente por clientId y tenantId</li>
 *   <li>Verificar que la app es de tipo CONFIDENTIAL</li>
 *   <li>Verificar que el grant CLIENT_CREDENTIALS está permitido en la política</li>
 *   <li>Verificar que el client_secret es correcto</li>
 *   <li>Obtener la clave de firma activa</li>
 *   <li>Construir y firmar el access_token (sub = clientId)</li>
 *   <li>Devolver el resultado (sin id_token ni refresh_token)</li>
 * </ol>
 *
 * <p>El {@code sub} del token es el {@code client_id} (string), no un UUID de usuario.
 */
public class IssueClientCredentialsTokenUseCase {

  private static final Duration ACCESS_TOKEN_TTL = Duration.ofHours(1);

  private final TenantRepositoryPort tenantRepository;
  private final ClientAppRepositoryPort clientAppRepository;
  private final ClientSecretEncoderPort clientSecretEncoder;
  private final SigningKeyRepositoryPort signingKeyRepository;
  private final TokenSignerPort tokenSigner;
  private final TokenClaimsFactoryPort tokenClaimsFactory;
  private final ClockPort clock;
  private final String issuerBaseUrl;

  public IssueClientCredentialsTokenUseCase(
      TenantRepositoryPort tenantRepository,
      ClientAppRepositoryPort clientAppRepository,
      ClientSecretEncoderPort clientSecretEncoder,
      SigningKeyRepositoryPort signingKeyRepository,
      TokenSignerPort tokenSigner,
      TokenClaimsFactoryPort tokenClaimsFactory,
      ClockPort clock,
      String issuerBaseUrl) {
    this.tenantRepository = tenantRepository;
    this.clientAppRepository = clientAppRepository;
    this.clientSecretEncoder = clientSecretEncoder;
    this.signingKeyRepository = signingKeyRepository;
    this.tokenSigner = tokenSigner;
    this.tokenClaimsFactory = tokenClaimsFactory;
    this.clock = clock;
    this.issuerBaseUrl = issuerBaseUrl;
  }

  /**
   * Ejecuta el flujo client_credentials y devuelve el access_token firmado.
   *
   * @param command datos de la solicitud
   * @return resultado con access_token y metadata
   * @throws TenantNotFoundException        si el tenant no existe
   * @throws TenantSuspendedException       si el tenant está suspendido
   * @throws ClientAppNotFoundException     si la app no existe en el tenant
   * @throws ClientAuthenticationException  si la app es PUBLIC o el secret es incorrecto
   * @throws NoActiveSigningKeyException    si no hay clave de firma activa
   */
  public IssueClientCredentialsTokenResult execute(IssueClientCredentialsTokenCommand command) {

    // 1. Validar tenant
    Tenant tenant = tenantRepository
        .findBySlug(new TenantSlug(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    if (tenant.isSuspended()) {
      throw new TenantSuspendedException(command.tenantSlug());
    }

    // 2. Buscar la app cliente
    ClientApp clientApp = clientAppRepository
        .findByClientIdAndTenantId(new ClientId(command.clientId()), tenant.getId())
        .orElseThrow(() -> new ClientAppNotFoundException(command.clientId()));

    // 3. Verificar que la app es CONFIDENTIAL
    if (ClientType.PUBLIC.equals(clientApp.getType())) {
      throw new ClientAuthenticationException(
          "PUBLIC clients cannot use the client_credentials grant");
    }

    // 4. Verificar que el grant CLIENT_CREDENTIALS está permitido
    clientApp.validateGrant(AllowedGrant.CLIENT_CREDENTIALS);

    // 5. Verificar el client_secret
    if (!clientSecretEncoder.matches(command.rawClientSecret(), clientApp.getHashedSecret())) {
      throw new ClientAuthenticationException(
          "Invalid client_secret for client: " + command.clientId());
    }

    // 6. Obtener clave activa
    SigningKey signingKey = signingKeyRepository
        .findActiveKey()
        .orElseThrow(() -> new NoActiveSigningKeyException("No active signing key found"));

    // 7. Resolver scopes
    String scope = resolveScope(command.scope(), clientApp);

    // 8. Construir y firmar access_token (sub = clientId)
    Instant now = clock.now();
    Instant expiresAt = now.plus(ACCESS_TOKEN_TTL);
    String jti = UUID.randomUUID().toString();
    String issuer = issuerBaseUrl + "/api/v1/tenants/" + command.tenantSlug();

    var claims = tokenClaimsFactory.buildAccessTokenClaims(
        issuer, command.clientId(), command.clientId(), scope, jti, now, expiresAt);
    String accessToken = tokenSigner.signJwt(claims, signingKey);

    return new IssueClientCredentialsTokenResult(accessToken, "Bearer", ACCESS_TOKEN_TTL.toSeconds(), scope);
  }

  /**
   * Resuelve los scopes efectivos: usa los solicitados si se proporcionan y son un subconjunto
   * de los permitidos; de lo contrario, usa todos los scopes permitidos por la política.
   */
  private String resolveScope(String requestedScope, ClientApp clientApp) {
    var allowedScopes = clientApp.getAccessPolicy().scopes();

    if (requestedScope == null || requestedScope.isBlank()) {
      // Retornar todos los scopes permitidos
      return allowedScopes.stream()
          .map(AllowedScope::value)
          .sorted()
          .collect(Collectors.joining(" "));
    }

    // Filtrar los solicitados que estén dentro de los permitidos
    String[] requested = requestedScope.split("\\s+");
    var allowedValues = allowedScopes.stream()
        .map(AllowedScope::value)
        .collect(Collectors.toSet());

    String effective = java.util.Arrays.stream(requested)
        .filter(allowedValues::contains)
        .sorted()
        .collect(Collectors.joining(" "));

    if (effective.isBlank()) {
      // Ningún scope solicitado está permitido → usar todos los permitidos
      return allowedScopes.stream()
          .map(AllowedScope::value)
          .sorted()
          .collect(Collectors.joining(" "));
    }

    return effective;
  }
}

