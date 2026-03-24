package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.UpdateUserProfileCommand;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.UserProfileResult;
import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.UserId;

import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso: actualizar el perfil propio del usuario autenticado (self-service).
 *
 * <p>Verifica el access_token JWT, extrae el {@code sub} (UUID del usuario),
 * localiza al usuario en el tenant, aplica la actualización parcial (PATCH
 * semántica — solo actualiza los campos no-nulos del comando) y retorna el
 * perfil actualizado.
 *
 * <p>Usado por: {@code PATCH /api/v1/tenants/{slug}/account/profile}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class UpdateUserProfileUseCase {

  private final SigningKeyRepositoryPort signingKeyRepository;
  private final AccessTokenVerifierPort accessTokenVerifier;
  private final UserRepositoryPort userRepository;
  private final TenantRepositoryPort tenantRepository;

  public UpdateUserProfileUseCase(
      SigningKeyRepositoryPort signingKeyRepository,
      AccessTokenVerifierPort accessTokenVerifier,
      UserRepositoryPort userRepository,
      TenantRepositoryPort tenantRepository) {
    this.signingKeyRepository = signingKeyRepository;
    this.accessTokenVerifier = accessTokenVerifier;
    this.userRepository = userRepository;
    this.tenantRepository = tenantRepository;
  }

  /**
   * Ejecuta la actualización del perfil propio del usuario.
   *
   * @param command parámetros del comando (tenantSlug + bearerToken + campos de perfil)
   * @return perfil actualizado del usuario
   */
  public UserProfileResult execute(UpdateUserProfileCommand command) {
    // 1. Obtener claves públicas para verificar el token
    var publicKeys = signingKeyRepository.findPublishableKeys();
    if (publicKeys.isEmpty()) {
      throw new InvalidRefreshTokenException("No public signing keys available for token verification");
    }

    // 2. Verificar token y extraer claims
    Map<String, Object> claims;
    try {
      claims = accessTokenVerifier.verify(command.bearerToken(), publicKeys);
    } catch (InvalidRefreshTokenException e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidRefreshTokenException("Invalid or expired access token: " + e.getMessage(), e);
    }

    // 3. Extraer subject (UUID del usuario)
    String sub = (String) claims.get("sub");
    if (sub == null) {
      throw new InvalidRefreshTokenException("Access token missing 'sub' claim");
    }

    // 4. Resolver tenant
    var tenant = tenantRepository.findBySlug(new TenantSlug(command.tenantSlug()))
        .orElseThrow(() -> new InvalidRefreshTokenException("Tenant not found: " + command.tenantSlug()));

    // 5. Parsear userId
    UUID userId;
    try {
      userId = UUID.fromString(sub);
    } catch (IllegalArgumentException e) {
      throw new InvalidRefreshTokenException("Access token 'sub' is not a valid UUID: " + sub);
    }

    // 6. Buscar usuario
    var user = userRepository.findByIdAndTenantId(new UserId(userId), tenant.getId())
        .orElseThrow(() -> new UserNotFoundException("User not found: " + sub));

    // 7. Actualizar perfil (solo campos no-nulos — PATCH semántica)
    user.updateProfile(
        command.firstName(),
        command.lastName(),
        command.phoneNumber(),
        command.locale(),
        command.zoneinfo(),
        command.profilePictureUrl(),
        command.birthdate(),
        command.website());

    // 8. Persistir y retornar
    var updated = userRepository.save(user);

    return new UserProfileResult(
        updated.getId().value().toString(),
        updated.getTenantId().value().toString(),
        updated.getUsername() != null ? updated.getUsername().value() : null,
        updated.getEmail() != null ? updated.getEmail().value() : null,
        updated.getFirstName(),
        updated.getLastName(),
        updated.getStatus() != null ? updated.getStatus().name() : null,
        updated.getPhoneNumber(),
        updated.getLocale(),
        updated.getZoneinfo(),
        updated.getProfilePictureUrl(),
        updated.getBirthdate(),
        updated.getWebsite());
  }
}

