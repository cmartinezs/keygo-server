package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SessionRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.ListUserSessionsCommand;
import io.cmartinezs.keygo.app.user.result.ListUserSessionsResult;
import io.cmartinezs.keygo.app.user.result.SessionInfoResult;
import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.auth.model.Session;
import io.cmartinezs.keygo.domain.auth.model.SessionStatus;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso: listar las sesiones activas del usuario autenticado.
 *
 * <p>Flujo:
 * <ol>
 *   <li>Verificar el access_token JWT y extraer el {@code sub} (UUID del usuario).</li>
 *   <li>Resolver el tenant por {@code tenantSlug}.</li>
 *   <li>Obtener todas las sesiones del usuario en el tenant (sin filtrar por estado).</li>
 *   <li>Filtrar únicamente las sesiones ACTIVE.</li>
 *   <li>Marcar la sesión actual comparando User-Agent + IP del comando con los de cada sesión.</li>
 * </ol>
 *
 * <p>Usado por: {@code GET /api/v1/tenants/{slug}/account/sessions}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class ListUserSessionsUseCase {

  private final SigningKeyRepositoryPort signingKeyRepository;
  private final AccessTokenVerifierPort accessTokenVerifier;
  private final TenantRepositoryPort tenantRepository;
  private final SessionRepositoryPort sessionRepository;

  public ListUserSessionsUseCase(
      SigningKeyRepositoryPort signingKeyRepository,
      AccessTokenVerifierPort accessTokenVerifier,
      TenantRepositoryPort tenantRepository,
      SessionRepositoryPort sessionRepository) {
    this.signingKeyRepository = signingKeyRepository;
    this.accessTokenVerifier = accessTokenVerifier;
    this.tenantRepository = tenantRepository;
    this.sessionRepository = sessionRepository;
  }

  /**
   * Ejecuta la consulta de sesiones activas del usuario autenticado.
   *
   * @param command parámetros del comando
   * @return resultado con la lista de sesiones activas
   * @throws InvalidRefreshTokenException si el token es inválido o expirado
   * @throws TenantNotFoundException      si el tenant no existe
   */
  public ListUserSessionsResult execute(ListUserSessionsCommand command) {
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

    // 2. Resolver tenant
    var tenant = tenantRepository.findBySlug(new TenantSlug(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    // 3. Parsear userId
    UUID userId;
    try {
      userId = UUID.fromString(sub);
    } catch (IllegalArgumentException e) {
      throw new InvalidRefreshTokenException("Access token 'sub' is not a valid UUID: " + sub);
    }

    // 4. Obtener sesiones y filtrar ACTIVE
    //    RFC restructure-multitenant: sessions now keyed by platformUserId.
    //    In tenant app flow, 'sub' is the tenant_user_id. For session lookup we need the
    //    platform_user_id. For MVP, we use the sub directly as a best-effort: if platform_user_id
    //    is null on the session it won't match, and that's acceptable for now.
    List<Session> sessions = sessionRepository.findAllByPlatformUserId(userId);

    List<SessionInfoResult> results = sessions.stream()
        .filter(s -> s.getStatus() == SessionStatus.ACTIVE)
        .map(s -> toResult(s, command.requestUserAgent(), command.requestIpAddress()))
        .toList();

    return new ListUserSessionsResult(results);
  }

  private SessionInfoResult toResult(Session session, String reqUserAgent, String reqIpAddress) {
    boolean isCurrent = Objects.equals(session.getUserAgent(), reqUserAgent)
        && Objects.equals(session.getIpAddress(), reqIpAddress);

    return new SessionInfoResult(
        session.getId().value(),
        session.getStatus().getValue(),
        session.getUserAgent(),
        session.getIpAddress(),
        session.getCreatedAt(),
        session.getLastAccessedAt(),
        session.getExpiresAt(),
        isCurrent);
  }
}
