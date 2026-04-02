package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.ChangePasswordCommand;
import io.cmartinezs.keygo.app.user.exception.IncorrectCurrentPasswordException;
import io.cmartinezs.keygo.app.user.port.PasswordHasherPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.ChangePasswordResult;
import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PasswordPolicy;
import io.cmartinezs.keygo.domain.user.model.UserId;

import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso: cambiar contraseña propia del usuario autenticado (self-service).
 *
 * <p>Flujo:
 * <ol>
 *   <li>Verificar el access_token JWT y extraer el {@code sub} (UUID del usuario).</li>
 *   <li>Resolver el tenant por {@code tenantSlug}.</li>
 *   <li>Cargar el usuario del repositorio.</li>
 *   <li>Verificar que {@code currentPassword} coincide con el hash almacenado.</li>
 *   <li>Validar {@code newPassword} contra la política de seguridad.</li>
 *   <li>Validar que {@code newPassword} difiere de {@code currentPassword}.</li>
 *   <li>Hashear y persistir la nueva contraseña.</li>
 * </ol>
 *
 * <p>Usado por: {@code POST /api/v1/tenants/{slug}/account/change-password}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class ChangePasswordUseCase {

  private final SigningKeyRepositoryPort signingKeyRepository;
  private final AccessTokenVerifierPort accessTokenVerifier;
  private final TenantRepositoryPort tenantRepository;
  private final UserRepositoryPort userRepository;
  private final PasswordHasherPort passwordHasher;

  public ChangePasswordUseCase(
      SigningKeyRepositoryPort signingKeyRepository,
      AccessTokenVerifierPort accessTokenVerifier,
      TenantRepositoryPort tenantRepository,
      UserRepositoryPort userRepository,
      PasswordHasherPort passwordHasher) {
    this.signingKeyRepository = signingKeyRepository;
    this.accessTokenVerifier = accessTokenVerifier;
    this.tenantRepository = tenantRepository;
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
  }

  /**
   * Ejecuta el cambio de contraseña self-service.
   *
   * @param command parámetros del comando
   * @return resultado con {@code changed = true} si se cambió exitosamente
   * @throws InvalidRefreshTokenException        si el token es inválido o expirado
   * @throws TenantNotFoundException             si el tenant no existe
   * @throws UserNotFoundException               si el usuario no existe en el tenant
   * @throws IncorrectCurrentPasswordException   si la contraseña actual es incorrecta
   * @throws IllegalArgumentException            si la nueva contraseña viola la política
   */
  public ChangePasswordResult execute(ChangePasswordCommand command) {
    // 1. Obtener claves públicas y verificar token
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

    // 4. Cargar usuario
    var user = userRepository.findByIdAndTenantId(new UserId(userId), tenant.getId())
        .orElseThrow(() -> new UserNotFoundException("id", sub));

    // 5. Verificar contraseña actual
    if (!passwordHasher.matches(command.currentPassword(), user.getPasswordHash().value())) {
      throw new IncorrectCurrentPasswordException();
    }

    // 6. Validar política de la nueva contraseña
    PasswordPolicy.validate(command.newPassword());

    // 7. Validar que la nueva contraseña difiere de la actual
    if (passwordHasher.matches(command.newPassword(), user.getPasswordHash().value())) {
      throw new IllegalArgumentException(
          "new_password: la nueva contraseña debe ser diferente a la actual");
    }

    // 8. Hashear y persistir
    String newHash = passwordHasher.hash(command.newPassword());
    user.updatePassword(PasswordHash.of(newHash));
    userRepository.save(user);

    return new ChangePasswordResult(true);
  }
}
