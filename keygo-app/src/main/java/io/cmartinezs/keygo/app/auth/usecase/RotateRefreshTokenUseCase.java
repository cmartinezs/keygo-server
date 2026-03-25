package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.command.RotateRefreshTokenCommand;
import io.cmartinezs.keygo.app.auth.port.ClockPort;
import io.cmartinezs.keygo.app.auth.port.RefreshTokenRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.SessionRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.TokenClaimsFactoryPort;
import io.cmartinezs.keygo.app.auth.port.TokenSignerPort;
import io.cmartinezs.keygo.app.auth.result.RotateRefreshTokenResult;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.auth.exception.NoActiveSigningKeyException;
import io.cmartinezs.keygo.domain.auth.exception.RefreshTokenExpiredException;
import io.cmartinezs.keygo.domain.auth.model.RefreshToken;
import io.cmartinezs.keygo.domain.auth.model.RefreshTokenStatus;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: rotar un refresh token para emitir nuevos tokens.
 *
 * <p>Flujo:
 * <ol>
 *   <li>Buscar el RT por hash SHA-256 del token plano recibido
 *   <li>Validar que está ACTIVE, no expirado y pertenece al cliente/tenant
 *   <li>Emitir nuevo access_token + id_token firmados con RS256
 *   <li>Generar nuevo refresh token plano, calcular su hash SHA-256, persistir
 *   <li>Marcar el RT anterior como USED y actualizarlo
 *   <li>Actualizar la sesión con el último acceso
 *   <li>Devolver nuevos tokens al caller (el token plano SOLO en este momento)
 * </ol>
 */
public class RotateRefreshTokenUseCase {

  private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);
  private static final Duration ACCESS_TOKEN_TTL = Duration.ofHours(1);

  private final RefreshTokenRepositoryPort refreshTokenRepository;
  private final SessionRepositoryPort sessionRepository;
  private final SigningKeyRepositoryPort signingKeyRepository;
  private final TokenSignerPort tokenSigner;
  private final TokenClaimsFactoryPort tokenClaimsFactory;
  private final TenantRepositoryPort tenantRepository;
  private final ClientAppRepositoryPort clientAppRepository;
  private final UserRepositoryPort userRepository;
  private final MembershipRepositoryPort membershipRepository;
  private final ClockPort clock;
  private final String issuerBaseUrl;
  private final SecureRandom secureRandom = new SecureRandom();

  public RotateRefreshTokenUseCase(
      RefreshTokenRepositoryPort refreshTokenRepository,
      SessionRepositoryPort sessionRepository,
      SigningKeyRepositoryPort signingKeyRepository,
      TokenSignerPort tokenSigner,
      TokenClaimsFactoryPort tokenClaimsFactory,
      TenantRepositoryPort tenantRepository,
      ClientAppRepositoryPort clientAppRepository,
      UserRepositoryPort userRepository,
      MembershipRepositoryPort membershipRepository,
      ClockPort clock,
      String issuerBaseUrl) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.sessionRepository = sessionRepository;
    this.signingKeyRepository = signingKeyRepository;
    this.tokenSigner = tokenSigner;
    this.tokenClaimsFactory = tokenClaimsFactory;
    this.tenantRepository = tenantRepository;
    this.clientAppRepository = clientAppRepository;
    this.userRepository = userRepository;
    this.membershipRepository = membershipRepository;
    this.clock = clock;
    this.issuerBaseUrl = issuerBaseUrl;
  }

  /**
   * Ejecuta la rotación del refresh token.
   *
   * @param command parámetros del comando
   * @return nuevos tokens emitidos (access_token, id_token, refresh_token plano)
   * @throws InvalidRefreshTokenException si el token es inválido, fue usado o no pertenece al cliente
   * @throws RefreshTokenExpiredException si el token expiró
   * @throws NoActiveSigningKeyException  si no hay clave activa para firmar
   */
  public RotateRefreshTokenResult execute(RotateRefreshTokenCommand command) {
    // 1. Buscar RT por hash SHA-256 del token plano
    String tokenHash = sha256Hex(command.rawRefreshToken());
    RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
        .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found or invalid"));

    // 2. Validar estado
    if (refreshToken.getStatus() == RefreshTokenStatus.USED) {
      throw new InvalidRefreshTokenException("Refresh token has already been used (possible replay attack)");
    }
    if (refreshToken.getStatus() == RefreshTokenStatus.REVOKED) {
      throw new InvalidRefreshTokenException("Refresh token has been revoked");
    }
    if (refreshToken.getStatus() == RefreshTokenStatus.EXPIRED || refreshToken.isExpired()) {
      throw new RefreshTokenExpiredException("Refresh token has expired");
    }

    // 3. Validar pertenencia al tenant y cliente
    Tenant tenant = tenantRepository.findBySlug(new TenantSlug(command.tenantSlug()))
        .orElseThrow(() -> new InvalidRefreshTokenException("Tenant not found: " + command.tenantSlug()));

    TenantId tenantId = tenant.getId();
    if (!refreshToken.getTenantId().equals(tenantId)) {
      throw new InvalidRefreshTokenException("Refresh token does not belong to this tenant");
    }

    ClientApp clientApp = clientAppRepository.findByClientIdAndTenantId(
            new ClientId(command.clientId()), tenantId)
        .orElseThrow(() -> new ClientAppNotFoundException("Client app not found: " + command.clientId()));

    if (!refreshToken.getClientAppId().equals(clientApp.getId())) {
      throw new InvalidRefreshTokenException("Refresh token was not issued to this client app");
    }

    // 4. Obtener usuario para claims del id_token
    Instant now = clock.now();
    UserId userId = refreshToken.getUserId();
    var user = userRepository.findByIdAndTenantId(userId, tenantId).orElse(null);
    String email = user != null && user.getEmail() != null ? user.getEmail().value() : null;
    String name = user != null
        ? buildName(user.getFirstName(), user.getLastName())
        : null;

    // 4b. Obtener roles del usuario en la app para incluirlos en el JWT
    List<String> roles = membershipRepository.findRoleCodesByUserAndClientApp(
        userId.value(), clientApp.getId().value());

    // 5. Firmar nuevos tokens
    SigningKey signingKey = signingKeyRepository.findActiveKey()
        .orElseThrow(() -> new NoActiveSigningKeyException("No active signing key found"));

    Instant expiresAt = now.plus(ACCESS_TOKEN_TTL);
    String accessJti = UUID.randomUUID().toString();
    String idJti = UUID.randomUUID().toString();
    String scope = command.scope() != null ? command.scope() : refreshToken.getScopes();
    String issuer = buildIssuer(command.tenantSlug());

    var accessClaims = new LinkedHashMap<>(tokenClaimsFactory.buildAccessTokenClaims(
        issuer, userId.value().toString(), command.clientId(), scope, accessJti, now, expiresAt, roles));
    accessClaims.put("tenant_slug", command.tenantSlug());
    String accessToken = tokenSigner.signJwt(accessClaims, signingKey);

    var idClaims = tokenClaimsFactory.buildIdTokenClaims(
        issuer, userId.value().toString(), command.clientId(), idJti, now, expiresAt,
        null, email, name, accessToken, roles);
    String idToken = tokenSigner.signJwt(idClaims, signingKey);

    // 6. Generar nuevo refresh token plano + hash + persistir
    String newRawToken = generateSecureToken();
    String newTokenHash = sha256Hex(newRawToken);
    Instant newRtExpiresAt = now.plus(REFRESH_TOKEN_TTL);

    RefreshToken newRefreshToken = RefreshToken.issue(
        newTokenHash,
        tenantId,
        refreshToken.getClientAppId(),
        userId,
        refreshToken.getSessionId(),
        scope,
        newRtExpiresAt,
        now);

    RefreshToken savedNewToken = refreshTokenRepository.save(newRefreshToken);

    // 7. Marcar el token anterior como USED
    refreshToken.markAsUsed(now, savedNewToken.getId());
    refreshTokenRepository.update(refreshToken);

    // 8. Actualizar la sesión con último acceso
    sessionRepository.findById(refreshToken.getSessionId()).ifPresent(session -> {
      session.updateLastAccessed(now);
      sessionRepository.update(session);
    });

    return new RotateRefreshTokenResult(
        accessToken,
        idToken,
        newRawToken,
        "Bearer",
        ACCESS_TOKEN_TTL.toSeconds(),
        scope);
  }

  /** Genera un token plano aleatorio de 256 bits en Base64URL. */
  private String generateSecureToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  /** Hash SHA-256 del token plano (hex) — determinista, apto para búsqueda en DB. */
  static String sha256Hex(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      var sb = new StringBuilder(hash.length * 2);
      for (byte b : hash) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private String buildName(String firstName, String lastName) {
    if (firstName == null && lastName == null) return null;
    if (firstName == null) return lastName;
    if (lastName == null) return firstName;
    return firstName + " " + lastName;
  }

  private String buildIssuer(String tenantSlug) {
    return issuerBaseUrl + "/api/v1/tenants/" + tenantSlug;
  }
}

