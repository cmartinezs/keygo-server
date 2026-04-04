package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.ResetPasswordCommand;
import io.cmartinezs.keygo.app.user.exception.IncorrectCurrentPasswordException;
import io.cmartinezs.keygo.app.user.exception.UserNotInResetPasswordStatusException;
import io.cmartinezs.keygo.app.user.port.PasswordHasherPort;
import io.cmartinezs.keygo.app.user.port.PasswordResetCodeRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.ResetPasswordResult;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.InvalidPasswordResetCodeException;
import io.cmartinezs.keygo.domain.user.exception.PasswordResetCodeExpiredException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PasswordPolicy;

/**
 * Caso de uso: restablecer contraseña para usuarios en estado {@code RESET_PASSWORD} (self-service).
 *
 * <p>Flujo:
 * <ol>
 *   <li>Resolver el tenant por {@code tenantSlug}.</li>
 *   <li>Buscar el usuario por {@code tenantId} + {@code email}.</li>
 *   <li>Verificar que el usuario está en estado {@code RESET_PASSWORD}.</li>
 *   <li>Verificar que la {@code temporaryPassword} coincide con el hash almacenado.</li>
 *   <li>Validar que {@code newPassword} coincide con {@code confirmNewPassword}.</li>
 *   <li>Validar {@code newPassword} contra la política de seguridad.</li>
 *   <li>Verificar el código de 6 dígitos recibido por email (no expirado, no usado, correcto).</li>
 *   <li>Hashear y persistir la nueva contraseña.</li>
 *   <li>Activar la cuenta ({@code status → ACTIVE}).</li>
 *   <li>Marcar el código como usado.</li>
 * </ol>
 *
 * <p>Usado por: {@code POST /api/v1/tenants/{slug}/account/reset-password}
 *
 * @author cmartinezs
 * @version 2.0
 */
public class ResetPasswordUseCase {

  private final TenantRepositoryPort tenantRepository;
  private final UserRepositoryPort userRepository;
  private final PasswordHasherPort passwordHasher;
  private final PasswordResetCodeRepositoryPort codeRepository;

  public ResetPasswordUseCase(
      TenantRepositoryPort tenantRepository,
      UserRepositoryPort userRepository,
      PasswordHasherPort passwordHasher,
      PasswordResetCodeRepositoryPort codeRepository) {
    this.tenantRepository = tenantRepository;
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
    this.codeRepository = codeRepository;
  }

  /**
   * Ejecuta el restablecimiento de contraseña con contraseña temporal y código de verificación.
   *
   * @param command parámetros del comando
   * @return resultado con {@code reset = true} si se restableció exitosamente
   * @throws TenantNotFoundException                  si el tenant no existe
   * @throws UserNotFoundException                    si el usuario no existe en el tenant
   * @throws UserNotInResetPasswordStatusException    si el usuario no está en estado RESET_PASSWORD
   * @throws IncorrectCurrentPasswordException        si la contraseña temporal es incorrecta
   * @throws IllegalArgumentException                 si las contraseñas no coinciden o violan la política
   * @throws InvalidPasswordResetCodeException        si el código de verificación es inválido o no existe
   * @throws PasswordResetCodeExpiredException        si el código de verificación ha expirado
   */
  public ResetPasswordResult execute(ResetPasswordCommand command) {
    // 1. Resolver tenant
    var tenant = tenantRepository.findBySlug(TenantSlug.of(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    // 2. Buscar usuario por email
    var user = userRepository.findByTenantIdAndEmail(tenant.getId(), EmailAddress.of(command.email()))
        .orElseThrow(() -> new UserNotFoundException("email", command.email()));

    // 3. Verificar que el usuario está en estado RESET_PASSWORD
    if (!user.isResetPassword()) {
      throw new UserNotInResetPasswordStatusException(command.email());
    }

    // 4. Verificar que la contraseña temporal coincide
    if (!passwordHasher.matches(command.temporaryPassword(), user.getPasswordHash().value())) {
      throw new IncorrectCurrentPasswordException();
    }

    // 5. Validar que newPassword coincide con confirmNewPassword
    if (!command.newPassword().equals(command.confirmNewPassword())) {
      throw new IllegalArgumentException(
          "new_password: la nueva contraseña y su confirmación no coinciden");
    }

    // 6. Validar política de la nueva contraseña
    PasswordPolicy.validate(command.newPassword());

    // 7. Verificar el código de 6 dígitos
    var resetCode = codeRepository.findByUserId(user.getId())
        .orElseThrow(InvalidPasswordResetCodeException::new);

    if (resetCode.isUsed()) {
      throw new InvalidPasswordResetCodeException();
    }
    if (resetCode.isExpired()) {
      throw new PasswordResetCodeExpiredException();
    }
    if (!resetCode.getCode().equals(command.verificationCode())) {
      throw new InvalidPasswordResetCodeException();
    }

    // 8. Hashear y persistir la nueva contraseña + activar cuenta
    String newHash = passwordHasher.hash(command.newPassword());
    user.updatePassword(PasswordHash.of(newHash));
    user.activate();
    userRepository.save(user);

    // 9. Marcar el código como usado
    codeRepository.markUsed(resetCode);

    return new ResetPasswordResult(true);
  }
}
