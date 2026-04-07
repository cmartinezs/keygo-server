package io.cmartinezs.keygo.app.platform.usecase;

import io.cmartinezs.keygo.app.auth.exception.HashingUnavailableException;
import io.cmartinezs.keygo.app.auth.port.ClockPort;
import io.cmartinezs.keygo.app.auth.port.RefreshTokenRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.SessionRepositoryPort;
import io.cmartinezs.keygo.app.auth.result.IssueTokensResult;
import io.cmartinezs.keygo.app.auth.usecase.IssueTokensUseCase;
import io.cmartinezs.keygo.app.membership.port.PlatformUserRoleRepositoryPort;
import io.cmartinezs.keygo.app.platform.command.RotatePlatformRefreshTokenCommand;
import io.cmartinezs.keygo.app.platform.result.IssuePlatformTokensResult;
import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.auth.exception.RefreshTokenExpiredException;
import io.cmartinezs.keygo.domain.auth.model.RefreshToken;
import io.cmartinezs.keygo.domain.auth.model.RefreshTokenStatus;
import io.cmartinezs.keygo.domain.auth.model.Session;
import io.cmartinezs.keygo.domain.auth.model.SessionStatus;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: rotar un refresh token de plataforma para emitir nuevos tokens.
 *
 * <p>Flujo simplificado del {@code RotateRefreshTokenUseCase} para sesiones de plataforma:
 * <ol>
 *   <li>Buscar el RT por hash SHA-256
 *   <li>Validar estado (ACTIVE, no expirado, no usado/revocado)
 *   <li>Verificar que la sesión es de plataforma (clientAppId=null) y está ACTIVE
 *   <li>Obtener roles de plataforma del usuario
 *   <li>Emitir nuevos tokens via {@link IssueTokensUseCase}
 *   <li>Generar nuevo refresh token, marcar el anterior como USED
 *   <li>Actualizar sesión con último acceso
 * </ol>
 */
public class RotatePlatformRefreshTokenUseCase {

  private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);
  private static final String PLATFORM_AUDIENCE = "keygo-platform";
  private static final String PLATFORM_SCOPE = "openid profile platform";

  private final RefreshTokenRepositoryPort refreshTokenRepository;
  private final SessionRepositoryPort sessionRepository;
  private final PlatformUserRoleRepositoryPort platformUserRoleRepository;
  private final IssueTokensUseCase issueTokensUseCase;
  private final ClockPort clock;
  private final String issuerBaseUrl;
  private final SecureRandom secureRandom = new SecureRandom();

  public RotatePlatformRefreshTokenUseCase(
      RefreshTokenRepositoryPort refreshTokenRepository,
      SessionRepositoryPort sessionRepository,
      PlatformUserRoleRepositoryPort platformUserRoleRepository,
      IssueTokensUseCase issueTokensUseCase,
      ClockPort clock,
      String issuerBaseUrl) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.sessionRepository = sessionRepository;
    this.platformUserRoleRepository = platformUserRoleRepository;
    this.issueTokensUseCase = issueTokensUseCase;
    this.clock = clock;
    this.issuerBaseUrl = issuerBaseUrl;
  }

  /**
   * Ejecuta la rotación del refresh token de plataforma.
   *
   * @param command parámetros del comando (token plano)
   * @return nuevos tokens emitidos
   * @throws InvalidRefreshTokenException si el token es inválido o no es de plataforma
   * @throws RefreshTokenExpiredException si el token expiró
   */
  public IssuePlatformTokensResult execute(RotatePlatformRefreshTokenCommand command) {
    // 1. Buscar RT por hash SHA-256
    String tokenHash = sha256Hex(command.refreshToken());
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
      throw new RefreshTokenExpiredException();
    }

    // 3. Verificar sesión: debe ser de plataforma (clientAppId=null) y ACTIVE
    Session session = sessionRepository.findById(refreshToken.getSessionId())
        .orElseThrow(() -> new InvalidRefreshTokenException("Session not found for refresh token"));

    if (!session.isPlatformSession()) {
      throw new InvalidRefreshTokenException("Refresh token does not belong to a platform session");
    }
    if (session.getStatus() != SessionStatus.ACTIVE) {
      throw new InvalidRefreshTokenException("Session is not active");
    }

    UUID platformUserId = session.getPlatformUserId();
    if (platformUserId == null) {
      throw new InvalidRefreshTokenException("Platform session has no associated user");
    }

    Instant now = clock.now();

    // 4. Obtener roles de plataforma
    List<String> roleCodes = platformUserRoleRepository.findRoleCodesByPlatformUserId(platformUserId);

    // 5. Emitir nuevos tokens (tenantId=null → clave global)
    String issuer = issuerBaseUrl + "/api/v1/platform";
    String sub = platformUserId.toString();

    IssueTokensResult tokensResult = issueTokensUseCase.execute(
        null, issuer, sub, PLATFORM_AUDIENCE, PLATFORM_SCOPE,
        null, null, null, null, roleCodes);

    String signingKeyId = tokensResult.signingKeyId();

    // 6. Generar nuevo refresh token
    String newRawToken = generateSecureToken();
    String newTokenHash = sha256Hex(newRawToken);
    Instant newRtExpiresAt = now.plus(REFRESH_TOKEN_TTL);

    RefreshToken newRefreshToken = RefreshToken.issue(
        newTokenHash, null, null, refreshToken.getSessionId(),
        PLATFORM_SCOPE, newRtExpiresAt, now, signingKeyId);
    RefreshToken savedNewToken = refreshTokenRepository.save(newRefreshToken);

    // 7. Marcar el token anterior como USED
    refreshToken.markAsUsed(now, savedNewToken.getId());
    refreshTokenRepository.update(refreshToken);

    // 8. Actualizar sesión con último acceso
    session.updateLastAccessed(now);
    sessionRepository.update(session);

    return new IssuePlatformTokensResult(
        tokensResult.accessToken(),
        tokensResult.idToken(),
        newRawToken,
        tokensResult.tokenType(),
        tokensResult.expiresIn(),
        signingKeyId);
  }

  private String generateSecureToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

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
      throw new HashingUnavailableException("SHA-256", e);
    }
  }
}
