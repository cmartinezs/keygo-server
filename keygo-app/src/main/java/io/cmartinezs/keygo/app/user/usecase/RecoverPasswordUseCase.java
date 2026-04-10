package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.RecoverPasswordCommand;
import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.RecoverPasswordResult;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.VerificationCodeAlreadyUsedException;
import io.cmartinezs.keygo.domain.user.exception.VerificationCodeExpiredException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.VerificationCode;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;
import io.cmartinezs.keygo.domain.user.model.PasswordValidationHelper;

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
  private final VerificationCodeRepositoryPort codeRepository;
  private final CredentialEncoderPort credentialEncoder;

  public RecoverPasswordUseCase(
      TenantRepositoryPort tenantRepository,
      UserRepositoryPort userRepository,
      VerificationCodeRepositoryPort codeRepository,
      CredentialEncoderPort credentialEncoder) {
    this.tenantRepository = tenantRepository;
    this.userRepository = userRepository;
    this.codeRepository = codeRepository;
    this.credentialEncoder = credentialEncoder;
  }

  /**
   * Ejecuta el restablecimiento de contraseña con token de recuperación.
   *
   * @param command parámetros del comando
   * @return resultado con {@code recovered = true} si se restableció exitosamente
   * @throws TenantNotFoundException                    si el tenant no existe
   * @throws UserNotFoundException                      si el token no existe (usamos mismo 404)
   * @throws VerificationCodeExpiredException           si el código expiró
   * @throws VerificationCodeAlreadyUsedException      si el código ya fue usado
   * @throws IllegalArgumentException                   si la nueva contraseña viola la política
   */
  public RecoverPasswordResult execute(RecoverPasswordCommand command) {
    // 1. Resolver tenant
    var tenant = tenantRepository.findBySlug(TenantSlug.of(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    // 2. Buscar token — 404 si no existe
    VerificationCode recoveryCode = codeRepository.findByCodeAndPurpose(
            command.recoveryToken(), VerificationPurpose.PASSWORD_RECOVERY)
        .orElseThrow(() -> new UserNotFoundException("recovery_token", command.recoveryToken()));

    // 3. Verificar no expirado
    if (recoveryCode.isExpired()) {
      throw new VerificationCodeExpiredException(VerificationPurpose.PASSWORD_RECOVERY);
    }

    // 4. Verificar no usado
    if (recoveryCode.isUsed()) {
      throw new VerificationCodeAlreadyUsedException(VerificationPurpose.PASSWORD_RECOVERY);
    }

    // 5. Validar política de contraseña
      PasswordValidationHelper.validate(command.newPassword(), false);

    // 6. Cargar usuario
    var user = userRepository.findByTenantIdAndPlatformUserId(tenant.getId(), recoveryCode.getUserId())
        .orElseThrow(() -> new UserNotFoundException("id", recoveryCode.getUserId().toString()));

    // 7. Actualizar contraseña + activar si estaba PENDING
    String newHash = credentialEncoder.encode(command.newPassword());
    user.updatePassword(PasswordHash.of(newHash));
    if (user.isPending()) {
      user.activate();
    }
    userRepository.save(user);

    // 8. Marcar token como usado
    codeRepository.markUsed(recoveryCode);

    return new RecoverPasswordResult(true);
  }
}
