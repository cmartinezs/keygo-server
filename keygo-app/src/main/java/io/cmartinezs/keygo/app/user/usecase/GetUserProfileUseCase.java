package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.GetUserProfileCommand;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.UserProfileResult;
import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.UserId;

import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso: obtener perfil completo del usuario autenticado (self-service).
 *
 * <p>Verifica el access_token JWT, extrae el {@code sub} (UUID del usuario),
 * localiza al usuario en el tenant y retorna su perfil completo (todos los
 * campos de perfil OIDC extendido, sin filtrado por scope).
 *
 * <p>Usado por: {@code GET /api/v1/tenants/{slug}/account/profile}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class GetUserProfileUseCase {

  private final SigningKeyRepositoryPort signingKeyRepository;
  private final AccessTokenVerifierPort accessTokenVerifier;
  private final UserRepositoryPort userRepository;
  private final TenantRepositoryPort tenantRepository;

  public GetUserProfileUseCase(
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
   * Ejecuta la obtención del perfil propio del usuario.
   *
   * @param command parámetros del comando (tenantSlug + bearerToken)
   * @return perfil completo del usuario autenticado
   */
  public UserProfileResult execute(GetUserProfileCommand command) {
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

    // 7. Construir resultado con perfil completo
    return new UserProfileResult(
        user.getId().value().toString(),
        user.getTenantId().value().toString(),
        user.getUsername() != null ? user.getUsername().value() : null,
        user.getEmail() != null ? user.getEmail().value() : null,
        user.getFirstName(),
        user.getLastName(),
        user.getStatus() != null ? user.getStatus().name() : null,
        user.getPhoneNumber(),
        user.getLocale(),
        user.getZoneinfo(),
        user.getProfilePictureUrl(),
        user.getBirthdate(),
        user.getWebsite());
  }
}

