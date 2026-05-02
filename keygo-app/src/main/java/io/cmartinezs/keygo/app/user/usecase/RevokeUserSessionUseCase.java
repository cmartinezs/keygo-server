package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.RefreshTokenRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.SessionRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.RevokeUserSessionCommand;
import io.cmartinezs.keygo.app.user.result.RevokeUserSessionResult;
import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import io.cmartinezs.keygo.domain.auth.model.SessionStatus;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;

import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso: revocar una sesión propia del usuario autenticado (self-service).
 *
 * <p>Flujo:
 * <ol>
 *   <li>Verificar el access_token JWT y extraer el {@code sub} (UUID del usuario).</li>
 *   <li>Resolver el tenant por {@code tenantSlug}.</li>
 *   <li>Buscar la sesión por ID. Si no existe → respuesta idempotente {@code alreadyClosed=true}.</li>
 *   <li>Verificar que la sesión pertenece al usuario autenticado.</li>
 *   <li>Si la sesión ya está TERMINATED o EXPIRED → respuesta idempotente {@code alreadyClosed=true}.</li>
 *   <li>Revocar refresh tokens y terminar la sesión.</li>
 * </ol>
 *
 * <p>Usado por: {@code DELETE /api/v1/tenants/{slug}/account/sessions/{sessionId}}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class RevokeUserSessionUseCase {

  private final SigningKeyRepositoryPort signingKeyRepository;
  private final AccessTokenVerifierPort accessTokenVerifier;
  private final TenantRepositoryPort tenantRepository;
  private final SessionRepositoryPort sessionRepository;
  private final RefreshTokenRepositoryPort refreshTokenRepository;

  public RevokeUserSessionUseCase(
      SigningKeyRepositoryPort signingKeyRepository,
      AccessTokenVerifierPort accessTokenVerifier,
      TenantRepositoryPort tenantRepository,
      SessionRepositoryPort sessionRepository,
      RefreshTokenRepositoryPort refreshTokenRepository) {
    this.signingKeyRepository = signingKeyRepository;
    this.accessTokenVerifier = accessTokenVerifier;
    this.tenantRepository = tenantRepository;
    this.sessionRepository = sessionRepository;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  /**
   * Ejecuta la revocación de la sesión.
   *
   * @param command parámetros del comando
   * @return resultado indicando si la sesión fue revocada o ya estaba cerrada
   * @throws InvalidRefreshTokenException si el token es inválido o expirado
   * @throws TenantNotFoundException      si el tenant no existe
   * @throws IllegalArgumentException     si el sessionId no es un UUID válido
   * @throws SecurityException            si la sesión no pertenece al usuario autenticado
   */
  public RevokeUserSessionResult execute(RevokeUserSessionCommand command) {
    // 1. Verificar token y extraer sub
    var publicKeys = signingKeyRepository.findPublishableKeys();
    if (publicKeys.isEmpty()) {
      throw new InvalidRefreshTokenException("No public signing keys available for token verification");
    }

    Map<String, Object> claims;
    try {
      claims = accessTokenVerifier.verify(command.bearerToken(), publicKeys);
    } catch (InvalidRefreshTokenException e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidRefreshTokenException("Invalid or expired access token: " + e.getMessage(), e);
    }

    String sub = (String) claims.get("sub");
    if (sub == null) {
      throw new InvalidRefreshTokenException("Access token missing 'sub' claim");
    }

    // 2. Resolver tenant (para validar contexto)
    tenantRepository.findBySlug(new TenantSlug(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    // 3. Parsear userId y sessionId
    UUID userId;
    try {
      userId = UUID.fromString(sub);
    } catch (IllegalArgumentException e) {
      throw new InvalidRefreshTokenException("Access token 'sub' is not a valid UUID: " + sub);
    }

    UUID sessionUuid;
    try {
      sessionUuid = UUID.fromString(command.sessionId());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("sessionId is not a valid UUID: " + command.sessionId());
    }

    var sessionId = SessionId.from(sessionUuid);

    // 4. Buscar sesión — si no existe, respuesta idempotente
    var sessionOpt = sessionRepository.findById(sessionId);
    if (sessionOpt.isEmpty()) {
      return new RevokeUserSessionResult(sessionUuid, true);
    }

    var session = sessionOpt.get();

    // 5. Verificar propiedad
    //    RFC restructure-multitenant: session has platformUserId instead of userId.
    //    For MVP, compare platformUserId with the JWT sub (which may be a tenant_user_id).
    //    This ownership check will be refined when platform auth flow is implemented.
    UUID sessionPlatformUserId = session.getPlatformUserId();
    if (sessionPlatformUserId != null && !sessionPlatformUserId.equals(userId)) {
      throw new SecurityException("Session does not belong to the authenticated user");
    }

    // 6. Si ya está cerrada, respuesta idempotente
    if (session.getStatus() != SessionStatus.ACTIVE) {
      return new RevokeUserSessionResult(sessionUuid, true);
    }

    // 7. Revocar refresh tokens y terminar sesión
    refreshTokenRepository.revokeAllForSession(sessionId);
    session.terminate();
    sessionRepository.update(session);

    return new RevokeUserSessionResult(sessionUuid, false);
  }
}
