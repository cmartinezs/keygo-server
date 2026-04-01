package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.command.GetUserInfoCommand;
import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.auth.result.UserInfoResult;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso: obtener información del usuario autenticado (OIDC §5.3 userinfo).
 *
 * <p>Verifica el access_token JWT, extrae el subject (sub), localiza al usuario
 * en el tenant y retorna sus claims de identidad.
 */
public class GetUserInfoUseCase {

  private final SigningKeyRepositoryPort signingKeyRepository;
  private final AccessTokenVerifierPort accessTokenVerifier;
  private final UserRepositoryPort userRepository;
  private final TenantRepositoryPort tenantRepository;

  public GetUserInfoUseCase(
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
   * Ejecuta la obtención de userinfo.
   *
   * @param command parámetros del comando (tenantSlug + bearerToken)
   * @return claims de identidad del usuario
   * @throws InvalidRefreshTokenException si el token es inválido o expirado
   * @throws UserNotFoundException        si el usuario referenciado no existe
   */
  public UserInfoResult execute(GetUserInfoCommand command) {
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

    // 5. Buscar usuario
    UUID userId;
    try {
      userId = UUID.fromString(sub);
    } catch (IllegalArgumentException e) {
      throw new InvalidRefreshTokenException("Access token 'sub' is not a valid UUID: " + sub);
    }

    var user = userRepository.findByIdAndTenantId(new UserId(userId), tenant.getId())
        .orElseThrow(() -> new UserNotFoundException("id", sub));

    // 6. Construir resultado
    String fullName = buildName(user.getFirstName(), user.getLastName());
    return new UserInfoResult(
        sub,
        user.getEmail() != null ? user.getEmail().value() : null,
        fullName,
        user.getUsername() != null ? user.getUsername().value() : null,
        user.getFirstName(),
        user.getLastName(),
        user.getProfilePictureUrl(),
        user.getLocale(),
        user.getZoneinfo(),
        user.getBirthdate(),
        user.getWebsite(),
        user.getPhoneNumber());
  }

  private String buildName(String firstName, String lastName) {
    if (firstName == null && lastName == null) return null;
    if (firstName == null) return lastName;
    if (lastName == null) return firstName;
    return firstName + " " + lastName;
  }
}

