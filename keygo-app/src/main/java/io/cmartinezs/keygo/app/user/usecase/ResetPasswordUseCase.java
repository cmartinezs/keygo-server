package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.ResetPasswordCommand;
import io.cmartinezs.keygo.app.user.exception.IncorrectCurrentPasswordException;
import io.cmartinezs.keygo.app.user.exception.PasswordMismatchException;
import io.cmartinezs.keygo.app.user.exception.UserNotInResetPasswordStatusException;
import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.ResetPasswordResult;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.VerificationCodeExpiredException;
import io.cmartinezs.keygo.domain.user.exception.VerificationCodeInvalidException;
import io.cmartinezs.keygo.domain.user.exception.PasswordResetRequestNotFoundException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PasswordValidationHelper;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;

import java.util.UUID;

/**
 * Caso de uso: restablecer contraseña para usuarios en estado {@code RESET_PASSWORD} (self-service).
 *
 * <p>Flujo:
 * <ol>
 *   <li>Resolver el tenant por {@code tenantSlug}.</li>
 *   <li>Buscar la solicitud de reset por {@code requestId} (UUID de {@code password_reset_codes}).</li>
 *   <li>Verificar que el código no está usado ni expirado y coincide con el proporcionado.</li>
 *   <li>Resolver el usuario asociado al código dentro del scope del tenant.</li>
 *   <li>Verificar que el usuario está en estado {@code RESET_PASSWORD}.</li>
 *   <li>Verificar que la {@code temporaryPassword} coincide con el hash almacenado.</li>
 *   <li>Validar que {@code newPassword} coincide con {@code confirmNewPassword}.</li>
 *   <li>Validar {@code newPassword} contra la política de seguridad.</li>
 *   <li>Hashear y persistir la nueva contraseña.</li>
 *   <li>Activar la cuenta ({@code status → ACTIVE}).</li>
 *   <li>Marcar el código como usado.</li>
 * </ol>
 *
 * <p>Usado por: {@code POST /api/v1/tenants/{slug}/account/reset-password}
 *
 * @author cmartinezs
 * @version 3.0
 */
public class ResetPasswordUseCase {

  private final TenantRepositoryPort tenantRepository;
  private final UserRepositoryPort userRepository;
  private final CredentialEncoderPort credentialEncoder;
  private final VerificationCodeRepositoryPort codeRepository;

  public ResetPasswordUseCase(
      TenantRepositoryPort tenantRepository,
      UserRepositoryPort userRepository,
      CredentialEncoderPort credentialEncoder,
      VerificationCodeRepositoryPort codeRepository) {
    this.tenantRepository = tenantRepository;
    this.userRepository = userRepository;
    this.credentialEncoder = credentialEncoder;
    this.codeRepository = codeRepository;
  }

  /**
   * Ejecuta el restablecimiento de contraseña con contraseña temporal y código de verificación.
   *
   * @param command parámetros del comando
   * @return resultado con {@code reset = true} si se restableció exitosamente
   * @throws TenantNotFoundException                  si el tenant no existe
   * @throws PasswordResetRequestNotFoundException    si el requestId no existe
   * @throws VerificationCodeInvalidException        si el código de verificación es inválido o ya fue usado
   * @throws VerificationCodeExpiredException        si el código de verificación ha expirado
   * @throws UserNotFoundException                    si el usuario del código no pertenece al tenant
   * @throws UserNotInResetPasswordStatusException    si el usuario no está en estado RESET_PASSWORD
   * @throws IncorrectCurrentPasswordException        si la contraseña temporal es incorrecta
   * @throws IllegalArgumentException                 si las contraseñas no coinciden o violan la política
   */
  public ResetPasswordResult execute(ResetPasswordCommand command) {
    // 1. Resolver tenant
    var tenant = tenantRepository.findBySlug(TenantSlug.of(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    // 2. Buscar la solicitud de reset por requestId
    UUID requestId = parseRequestId(command.requestId());
    var resetCode = codeRepository.findById(requestId)
        .orElseThrow(() -> new PasswordResetRequestNotFoundException(command.requestId()));

    // 3. Verificar el código de verificación (primero: evita revelar info del usuario si el código es inválido)
    if (resetCode.isUsed()) {
      throw new VerificationCodeInvalidException(VerificationPurpose.PASSWORD_RESET);
    }
    if (resetCode.isExpired()) {
      throw new VerificationCodeExpiredException(VerificationPurpose.PASSWORD_RESET);
    }
    if (!resetCode.getCode().equals(command.verificationCode())) {
      throw new VerificationCodeInvalidException(VerificationPurpose.PASSWORD_RESET);
    }

    // 4. Resolver el usuario dentro del scope del tenant (protección cross-tenant)
    TenantId tenantId = tenant.getId();
    var user = userRepository.findByIdAndTenantId(new UserId(resetCode.getUserId().value()), tenantId)
        .orElseThrow(() -> new UserNotFoundException("requestId", command.requestId()));

    // 5. Verificar que el usuario está en estado RESET_PASSWORD
    if (!user.isResetPassword()) {
      throw new UserNotInResetPasswordStatusException(user.getEmail() != null ? user.getEmail().value() : "unknown");
    }

    // 6. Verificar que la contraseña temporal coincide
    if (!credentialEncoder.matches(command.temporaryPassword(), user.getPasswordHash().value())) {
      throw new IncorrectCurrentPasswordException();
    }

    // 7. Validar que newPassword coincide con confirmNewPassword
    if (!command.newPassword().equals(command.confirmNewPassword())) {
      throw new PasswordMismatchException("new_password and confirm_new_password must match");
    }

    // 8. Validar política de la nueva contraseña (permanente)
    PasswordValidationHelper.validate(command.newPassword(), false);

    // 9. Hashear y persistir la nueva contraseña + activar cuenta
    String newHash = credentialEncoder.encode(command.newPassword());
    user.updatePassword(PasswordHash.of(newHash));
    user.activate();
    userRepository.save(user);

    // 10. Marcar el código como usado
    codeRepository.markUsed(resetCode);

    return new ResetPasswordResult(true);
  }

  private UUID parseRequestId(String requestId) {
    try {
      return UUID.fromString(requestId);
    } catch (IllegalArgumentException ex) {
      throw new PasswordResetRequestNotFoundException(requestId);
    }
  }
}
