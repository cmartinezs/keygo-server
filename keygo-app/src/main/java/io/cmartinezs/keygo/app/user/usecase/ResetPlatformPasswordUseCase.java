package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.user.exception.IncorrectCurrentPasswordException;
import io.cmartinezs.keygo.app.user.exception.PasswordMismatchException;
import io.cmartinezs.keygo.app.user.exception.UserNotInResetPasswordStatusException;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.app.user.result.ResetPasswordResult;
import io.cmartinezs.keygo.domain.user.exception.PasswordResetRequestNotFoundException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.exception.VerificationCodeExpiredException;
import io.cmartinezs.keygo.domain.user.exception.VerificationCodeInvalidException;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PasswordValidationHelper;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;

import java.util.UUID;

/**
 * Caso de uso: restablecer contraseña de plataforma con contraseña temporal + código de verificación.
 *
 * <p>Equivalente a {@link ResetPasswordUseCase} pero sin tenant scope.
 * Se invoca desde {@code POST /api/v1/platform/account/reset-password}.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class ResetPlatformPasswordUseCase {

  private final PlatformUserRepositoryPort platformUserRepository;
  private final CredentialEncoderPort credentialEncoder;
  private final VerificationCodeRepositoryPort codeRepository;

  public ResetPlatformPasswordUseCase(
      PlatformUserRepositoryPort platformUserRepository,
      CredentialEncoderPort credentialEncoder,
      VerificationCodeRepositoryPort codeRepository) {
    this.platformUserRepository = platformUserRepository;
    this.credentialEncoder = credentialEncoder;
    this.codeRepository = codeRepository;
  }

  /**
   * Ejecuta el restablecimiento de contraseña con contraseña temporal y código de verificación.
   *
   * @param requestId          UUID de la solicitud de reset
   * @param temporaryPassword  contraseña temporal
   * @param newPassword        nueva contraseña definitiva
   * @param confirmNewPassword confirmación
   * @param verificationCode   código de 6 dígitos
   * @return resultado con {@code reset = true}
   */
  public ResetPasswordResult execute(
      String requestId,
      String temporaryPassword,
      String newPassword,
      String confirmNewPassword,
      String verificationCode) {

    UUID parsedRequestId = parseRequestId(requestId);
    var resetCode = codeRepository.findById(parsedRequestId)
        .orElseThrow(() -> new PasswordResetRequestNotFoundException(requestId));

    if (resetCode.isUsed()) {
      throw new VerificationCodeInvalidException(VerificationPurpose.PASSWORD_RESET);
    }
    if (resetCode.isExpired()) {
      throw new VerificationCodeExpiredException(VerificationPurpose.PASSWORD_RESET);
    }
    if (!resetCode.getCode().equals(verificationCode)) {
      throw new VerificationCodeInvalidException(VerificationPurpose.PASSWORD_RESET);
    }

    var user = platformUserRepository.findById(resetCode.getUserId())
        .orElseThrow(() -> new UserNotFoundException("requestId", requestId));

    if (!user.isResetPassword()) {
      throw new UserNotInResetPasswordStatusException(
          user.getEmail() != null ? user.getEmail().value() : "unknown");
    }

    if (!credentialEncoder.matches(temporaryPassword, user.getPasswordHash().value())) {
      throw new IncorrectCurrentPasswordException();
    }

    if (!newPassword.equals(confirmNewPassword)) {
      throw new PasswordMismatchException("new_password and confirm_new_password must match");
    }

    PasswordValidationHelper.validate(newPassword, false);

    String newHash = credentialEncoder.encode(newPassword);
    user.updatePassword(PasswordHash.of(newHash));
    user.activate();
    platformUserRepository.save(user);

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
