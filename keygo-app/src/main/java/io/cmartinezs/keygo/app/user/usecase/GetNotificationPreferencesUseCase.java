package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.GetNotificationPreferencesCommand;
import io.cmartinezs.keygo.app.user.port.NotificationPreferencesRepositoryPort;
import io.cmartinezs.keygo.app.user.result.NotificationPreferencesResult;
import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.model.NotificationPreferences;
import io.cmartinezs.keygo.domain.user.model.UserId;

import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso: obtener las preferencias de notificación del usuario autenticado.
 *
 * <p>Si el usuario nunca configuró preferencias, se retornan los valores por defecto
 * sin crear ningún registro en base de datos.
 *
 * <p>Flujo:
 * <ol>
 *   <li>Verificar el access_token JWT y extraer el {@code sub} (UUID del usuario).</li>
 *   <li>Resolver el tenant por {@code tenantSlug}.</li>
 *   <li>Buscar preferencias — si no existen, retornar defaults.</li>
 * </ol>
 *
 * <p>Usado por: {@code GET /api/v1/tenants/{slug}/account/notification-preferences}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class GetNotificationPreferencesUseCase {

  private final SigningKeyRepositoryPort signingKeyRepository;
  private final AccessTokenVerifierPort accessTokenVerifier;
  private final TenantRepositoryPort tenantRepository;
  private final NotificationPreferencesRepositoryPort preferencesRepository;

  public GetNotificationPreferencesUseCase(
      SigningKeyRepositoryPort signingKeyRepository,
      AccessTokenVerifierPort accessTokenVerifier,
      TenantRepositoryPort tenantRepository,
      NotificationPreferencesRepositoryPort preferencesRepository) {
    this.signingKeyRepository = signingKeyRepository;
    this.accessTokenVerifier = accessTokenVerifier;
    this.tenantRepository = tenantRepository;
    this.preferencesRepository = preferencesRepository;
  }

  /**
   * Ejecuta la obtención de preferencias de notificación.
   *
   * @param command parámetros del comando
   * @return preferencias actuales o defaults si no hay registro
   * @throws InvalidRefreshTokenException si el token es inválido
   * @throws TenantNotFoundException      si el tenant no existe
   */
  public NotificationPreferencesResult execute(GetNotificationPreferencesCommand command) {
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

    var userIdVO = new UserId(userId);

    // 4. Buscar preferencias o retornar defaults
    NotificationPreferences preferences = preferencesRepository
        .findByUserIdAndTenantId(userIdVO, tenant.getId())
        .orElseGet(() -> NotificationPreferences.defaults(userIdVO, tenant.getId()));

    return NotificationPreferencesResult.from(preferences);
  }
}
