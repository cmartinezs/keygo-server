package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.RecoverPasswordCommand;
import io.cmartinezs.keygo.app.user.port.PasswordHasherPort;
import io.cmartinezs.keygo.app.user.port.PasswordRecoveryTokenRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.RecoverPasswordResult;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.PasswordRecoveryTokenAlreadyUsedException;
import io.cmartinezs.keygo.domain.user.exception.PasswordRecoveryTokenExpiredException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PasswordPolicy;
import io.cmartinezs.keygo.domain.user.model.PasswordRecoveryToken;

/**
 * Caso de uso: restablecer contraseña usando un token de recuperación (self-service).
 *
 * <p>Flujo:
 * <ol>
 *   <li>Resolver el tenant por {@code tenantSlug}.</li>
 *   <li>Buscar el token por su valor — 404 si no existe.</li>
 *   <li>Verificar que el token no haya expirado — 422 si expirado.</li>
 *   <li>Verificar que el token no haya sido usado — 422 si ya usado.</li>
 *   <li>Validar la nueva contraseña contra la política — 400 si inválida.</li>
 *   <li>Cargar el usuario por {@code userId} del token.</li>
 *   <li>Hashear y persistir la nueva contraseña; activar cuenta si estaba PENDING.</li>
 *   <li>Marcar el token como usado.</li>
 * </ol>
 *
 * <p>Usado por: {@code POST /api/v1/tenants/{slug}/account/recover-password}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class RecoverPasswordUseCase {

  private final TenantRepositoryPort tenantRepository;
  private final UserRepositoryPort userRepository;
  private final PasswordRecoveryTokenRepositoryPort tokenRepository;
  private final PasswordHasherPort passwordHasher;

  public RecoverPasswordUseCase(
      TenantRepositoryPort tenantRepository,
      UserRepositoryPort userRepository,
      PasswordRecoveryTokenRepositoryPort tokenRepository,
      PasswordHasherPort passwordHasher) {
    this.tenantRepository = tenantRepository;
    this.userRepository = userRepository;
    this.tokenRepository = tokenRepository;
    this.passwordHasher = passwordHasher;
  }

  /**
   * Ejecuta el restablecimiento de contraseña con token de recuperación.
   *
   * @param command parámetros del comando
   * @return resultado con {@code recovered = true} si se restableció exitosamente
   * @throws TenantNotFoundException                    si el tenant no existe
   * @throws UserNotFoundException                      si el token no existe (usamos mismo 404)
   * @throws PasswordRecoveryTokenExpiredException      si el token expiró
   * @throws PasswordRecoveryTokenAlreadyUsedException  si el token ya fue usado
   * @throws IllegalArgumentException                   si la nueva contraseña viola la política
   */
  public RecoverPasswordResult execute(RecoverPasswordCommand command) {
    // 1. Resolver tenant
    var tenant = tenantRepository.findBySlug(TenantSlug.of(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    // 2. Buscar token — 404 si no existe
    PasswordRecoveryToken recoveryToken = tokenRepository.findByToken(command.recoveryToken())
        .orElseThrow(() -> new UserNotFoundException("recovery_token", command.recoveryToken()));

    // 3. Verificar no expirado
    if (recoveryToken.isExpired()) {
      throw new PasswordRecoveryTokenExpiredException();
    }

    // 4. Verificar no usado
    if (recoveryToken.isUsed()) {
      throw new PasswordRecoveryTokenAlreadyUsedException();
    }

    // 5. Validar política de contraseña
    PasswordPolicy.validate(command.newPassword());

    // 6. Cargar usuario
    var user = userRepository.findByIdAndTenantId(recoveryToken.getUserId(), tenant.getId())
        .orElseThrow(() -> new UserNotFoundException("id", recoveryToken.getUserId().toString()));

    // 7. Actualizar contraseña + activar si estaba PENDING
    String newHash = passwordHasher.hash(command.newPassword());
    user.updatePassword(PasswordHash.of(newHash));
    if (user.isPending()) {
      user.activate();
    }
    userRepository.save(user);

    // 8. Marcar token como usado
    tokenRepository.markUsed(recoveryToken);

    return new RecoverPasswordResult(true);
  }
}
