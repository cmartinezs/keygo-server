package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.user.command.GetPlatformUserProfileCommand;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.UserProfileResult;
import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.UserId;

import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso: obtener perfil completo del usuario de plataforma autenticado (self-service).
 *
 * <p>Verifica el access_token JWT, extrae el {@code sub} (UUID del platform user),
 * localiza al usuario en {@code platform_users} y retorna su perfil completo.
 * A diferencia de {@link GetUserProfileUseCase}, no resuelve ningún tenant.
 *
 * <p>Usado por: {@code GET /api/v1/platform/account/profile}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class GetPlatformUserProfileUseCase {

  private final SigningKeyRepositoryPort signingKeyRepository;
  private final AccessTokenVerifierPort accessTokenVerifier;
  private final PlatformUserRepositoryPort platformUserRepository;

  public GetPlatformUserProfileUseCase(
      SigningKeyRepositoryPort signingKeyRepository,
      AccessTokenVerifierPort accessTokenVerifier,
      PlatformUserRepositoryPort platformUserRepository) {
    this.signingKeyRepository = signingKeyRepository;
    this.accessTokenVerifier = accessTokenVerifier;
    this.platformUserRepository = platformUserRepository;
  }

  /**
   * Ejecuta la obtención del perfil propio del usuario de plataforma.
   *
   * @param command parámetros del comando (bearerToken)
   * @return perfil completo del platform user autenticado
   */
  public UserProfileResult execute(GetPlatformUserProfileCommand command) {
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

    // 6. Construir resultado (tenantId, birthdate y website son null en PlatformUser)
    return new UserProfileResult(
        user.getId().value().toString(),
        null,
        user.getUsername() != null ? user.getUsername().value() : null,
        user.getEmail() != null ? user.getEmail().value() : null,
        user.getFirstName(),
        user.getLastName(),
        user.getStatus() != null ? user.getStatus().name() : null,
        user.getPhoneNumber(),
        user.getLocale(),
        user.getZoneinfo(),
        user.getProfilePictureUrl(),
        null,
        null);
  }
}
