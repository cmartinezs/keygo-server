package io.cmartinezs.keygo.app.platform.usecase;

import io.cmartinezs.keygo.app.auth.exception.HashingUnavailableException;
import io.cmartinezs.keygo.app.auth.port.ClockPort;
import io.cmartinezs.keygo.app.auth.port.RefreshTokenRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.SessionRepositoryPort;
import io.cmartinezs.keygo.app.auth.result.IssueTokensResult;
import io.cmartinezs.keygo.app.auth.usecase.IssueTokensUseCase;
import io.cmartinezs.keygo.app.membership.port.PlatformUserRoleRepositoryPort;
import io.cmartinezs.keygo.app.platform.result.IssuePlatformTokensResult;
import io.cmartinezs.keygo.domain.auth.model.RefreshToken;
import io.cmartinezs.keygo.domain.auth.model.Session;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
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
 * Caso de uso: emitir tokens de plataforma tras autenticación directa.
 *
 * <p>Flujo:
 * <ol>
 *   <li>Obtener roles de plataforma del usuario
 *   <li>Emitir access_token via {@link IssueTokensUseCase} (tenantId=null → clave global)
 *   <li>Abrir sesión de plataforma (clientAppId=null)
 *   <li>Generar y persistir refresh token
 *   <li>Devolver tokens al caller
 * </ol>
 */
public class IssuePlatformTokensUseCase {

  private static final Duration SESSION_TTL = Duration.ofDays(30);
  private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);
  private static final String PLATFORM_AUDIENCE = "keygo-platform";
  private static final String PLATFORM_SCOPE = "openid profile platform";

  private final PlatformUserRoleRepositoryPort platformUserRoleRepository;
  private final IssueTokensUseCase issueTokensUseCase;
  private final SessionRepositoryPort sessionRepository;
  private final RefreshTokenRepositoryPort refreshTokenRepository;
  private final ClockPort clock;
  private final SecureRandom secureRandom = new SecureRandom();

  public IssuePlatformTokensUseCase(
      PlatformUserRoleRepositoryPort platformUserRoleRepository,
      IssueTokensUseCase issueTokensUseCase,
      SessionRepositoryPort sessionRepository,
      RefreshTokenRepositoryPort refreshTokenRepository,
      ClockPort clock) {
    this.platformUserRoleRepository = platformUserRoleRepository;
    this.issueTokensUseCase = issueTokensUseCase;
    this.sessionRepository = sessionRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.clock = clock;
  }

  /**
   * Emite tokens de plataforma para un usuario autenticado.
   *
   * @param platformUser usuario de plataforma autenticado
   * @param issuer       URL del emisor (e.g. http://host/keygo-server/api/v1/platform)
   * @param userAgent    user-agent del cliente (nullable)
   * @param ipAddress    dirección IP del cliente (nullable)
   * @return resultado con access_token, refresh_token plano, tipo y expiración
   */
  public IssuePlatformTokensResult execute(
      PlatformUser platformUser,
      String issuer,
      String userAgent,
      String ipAddress) {

    UUID userId = platformUser.getId().value();

    // 1. Obtener roles de plataforma
    List<String> roleCodes = platformUserRoleRepository.findRoleCodesByPlatformUserId(userId);

    // 2. Emitir access_token + id_token (tenantId=null → clave global)
    String sub = userId.toString();
    String email = platformUser.getEmail() != null ? platformUser.getEmail().value() : null;
    String name = buildName(platformUser.getFirstName(), platformUser.getLastName());

    IssueTokensResult tokensResult = issueTokensUseCase.execute(
        null, issuer, sub, PLATFORM_AUDIENCE, PLATFORM_SCOPE,
        null, email, name, null, roleCodes);

    Instant now = clock.now();
    String signingKeyId = tokensResult.signingKeyId();

    // 3. Abrir sesión de plataforma (clientAppId=null)
    Session session = Session.open(
        userId, null, now.plus(SESSION_TTL), now, userAgent, ipAddress, signingKeyId);
    Session savedSession = sessionRepository.save(session);

    // 4. Generar refresh token
    String rawRefreshToken = generateSecureToken();
    String tokenHash = sha256Hex(rawRefreshToken);
    Instant refreshExpiresAt = now.plus(REFRESH_TOKEN_TTL);

    RefreshToken refreshToken = RefreshToken.issue(
        tokenHash, null, null, savedSession.getId(),
        PLATFORM_SCOPE, refreshExpiresAt, now, signingKeyId);
    refreshTokenRepository.save(refreshToken);

    // 5. Retornar resultado
    return new IssuePlatformTokensResult(
        tokensResult.accessToken(),
        rawRefreshToken,
        tokensResult.tokenType(),
        tokensResult.expiresIn(),
        signingKeyId);
  }

  private String buildName(String firstName, String lastName) {
    if (firstName == null && lastName == null) return null;
    if (firstName == null) return lastName;
    if (lastName == null) return firstName;
    return firstName + " " + lastName;
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
