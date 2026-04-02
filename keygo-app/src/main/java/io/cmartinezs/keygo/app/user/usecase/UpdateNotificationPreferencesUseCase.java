package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.UpdateNotificationPreferencesCommand;
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
 * Caso de uso: actualizar las preferencias de notificación del usuario autenticado (upsert).
 *
 * <p>Si no existe registro previo, se crea. Si ya existe, se actualiza con los valores recibidos.
 *
 * <p>Flujo:
 * <ol>
 *   <li>Verificar el access_token JWT y extraer el {@code sub} (UUID del usuario).</li>
 *   <li>Resolver el tenant por {@code tenantSlug}.</li>
 *   <li>Buscar preferencias existentes o crear nuevas a partir del comando.</li>
 *   <li>Persistir (upsert) y retornar el estado actualizado.</li>
 * </ol>
 *
 * <p>Usado por: {@code PATCH /api/v1/tenants/{slug}/account/notification-preferences}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class UpdateNotificationPreferencesUseCase {

  private final SigningKeyRepositoryPort signingKeyRepository;
  private final AccessTokenVerifierPort accessTokenVerifier;
  private final TenantRepositoryPort tenantRepository;
  private final NotificationPreferencesRepositoryPort preferencesRepository;

  public UpdateNotificationPreferencesUseCase(
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
   * Ejecuta el upsert de preferencias de notificación.
   *
   * @param command parámetros del comando con los 5 campos boolean
   * @return preferencias resultantes tras la persistencia
   * @throws InvalidRefreshTokenException si el token es inválido
   * @throws TenantNotFoundException      si el tenant no existe
   */
  public NotificationPreferencesResult execute(UpdateNotificationPreferencesCommand command) {
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

    // 4. Upsert: buscar existente o usar defaults como base, luego actualizar
    NotificationPreferences preferences = preferencesRepository
        .findByUserIdAndTenantId(userIdVO, tenant.getId())
        .orElseGet(() -> NotificationPreferences.defaults(userIdVO, tenant.getId()));

    preferences.update(
        command.securityAlertsEmail(),
        command.securityAlertsInApp(),
        command.billingAlertsEmail(),
        command.productUpdatesEmail(),
        command.weeklyDigest());

    NotificationPreferences saved = preferencesRepository.saveOrUpdate(preferences);

    return NotificationPreferencesResult.from(saved);
  }
}
