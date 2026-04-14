package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.user.command.UpdatePlatformUserProfileCommand;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.UserProfileResult;
import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.UserId;

import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso: actualizar el perfil propio del usuario de plataforma autenticado (self-service).
 *
 * <p>Verifica el access_token JWT, extrae el {@code sub} (UUID del platform user),
 * localiza al usuario en {@code platform_users}, aplica la actualización parcial
 * (PATCH semántica — solo actualiza los campos no-nulos del comando) y retorna el
 * perfil actualizado.
 *
 * <p>Usado por: {@code PATCH /api/v1/platform/account/profile}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class UpdatePlatformUserProfileUseCase {

  private final SigningKeyRepositoryPort signingKeyRepository;
  private final AccessTokenVerifierPort accessTokenVerifier;
  private final PlatformUserRepositoryPort platformUserRepository;

  public UpdatePlatformUserProfileUseCase(
      SigningKeyRepositoryPort signingKeyRepository,
      AccessTokenVerifierPort accessTokenVerifier,
      PlatformUserRepositoryPort platformUserRepository) {
    this.signingKeyRepository = signingKeyRepository;
    this.accessTokenVerifier = accessTokenVerifier;
    this.platformUserRepository = platformUserRepository;
  }

  /**
   * Ejecuta la actualización del perfil propio del usuario de plataforma.
   *
   * @param command parámetros del comando (bearerToken + campos de perfil)
   * @return perfil actualizado del platform user
   */
  public UserProfileResult execute(UpdatePlatformUserProfileCommand command) {
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

    // 3. Extraer subject (UUID del platform user)
    String sub = (String) claims.get("sub");
    if (sub == null) {
      throw new InvalidRefreshTokenException("Access token missing 'sub' claim");
    }

    // 4. Parsear userId
    UUID userId;
    try {
      userId = UUID.fromString(sub);
    } catch (IllegalArgumentException e) {
      throw new InvalidRefreshTokenException("Access token 'sub' is not a valid UUID: " + sub);
    }

    // 5. Buscar platform user
    var user = platformUserRepository.findById(new UserId(userId))
        .orElseThrow(() -> new UserNotFoundException("id", sub));

    // 6. Actualizar perfil (solo campos no-nulos — PATCH semántica)
    user.updateProfile(
        command.firstName(),
        command.lastName(),
        command.phoneNumber(),
        command.locale(),
        command.zoneinfo(),
        command.profilePictureUrl());

    // 7. Persistir y retornar
    var updated = platformUserRepository.save(user);

    return new UserProfileResult(
        updated.getId().value().toString(),
        null,
        updated.getUsername() != null ? updated.getUsername().value() : null,
        updated.getEmail() != null ? updated.getEmail().value() : null,
        updated.getFirstName(),
        updated.getLastName(),
        updated.getStatus() != null ? updated.getStatus().name() : null,
        updated.getPhoneNumber(),
        updated.getLocale(),
        updated.getZoneinfo(),
        updated.getProfilePictureUrl(),
        null,
        null);
  }
}
