package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.app.user.result.RecoverPasswordResult;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.exception.VerificationCodeAlreadyUsedException;
import io.cmartinezs.keygo.domain.user.exception.VerificationCodeExpiredException;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PasswordValidationHelper;
import io.cmartinezs.keygo.domain.user.model.VerificationCode;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;

/**
 * Caso de uso: restablecer contraseña de plataforma usando token de recuperación.
 *
 * <p>Equivalente a {@link RecoverPasswordUseCase} pero sin tenant scope.
 * Se invoca desde {@code POST /api/v1/platform/account/recover-password}.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class RecoverPlatformPasswordUseCase {

  private final PlatformUserRepositoryPort platformUserRepository;
  private final VerificationCodeRepositoryPort codeRepository;
  private final CredentialEncoderPort credentialEncoder;

  public RecoverPlatformPasswordUseCase(
      PlatformUserRepositoryPort platformUserRepository,
      VerificationCodeRepositoryPort codeRepository,
      CredentialEncoderPort credentialEncoder) {
    this.platformUserRepository = platformUserRepository;
    this.codeRepository = codeRepository;
    this.credentialEncoder = credentialEncoder;
  }

  /**
   * Ejecuta el restablecimiento de contraseña con token de recuperación.
   *
   * @param recoveryToken token hex de 32 caracteres recibido por email
   * @param newPassword   nueva contraseña (debe cumplir política de seguridad)
   * @return resultado con {@code recovered = true}
   */
  public RecoverPasswordResult execute(String recoveryToken, String newPassword) {
    VerificationCode recoveryCode = codeRepository.findByCodeAndPurpose(
            recoveryToken, VerificationPurpose.PASSWORD_RECOVERY)
        .orElseThrow(() -> new UserNotFoundException("recovery_token", recoveryToken));

    if (recoveryCode.isExpired()) {
      throw new VerificationCodeExpiredException(VerificationPurpose.PASSWORD_RECOVERY);
    }

    if (recoveryCode.isUsed()) {
      throw new VerificationCodeAlreadyUsedException(VerificationPurpose.PASSWORD_RECOVERY);
    }

    PasswordValidationHelper.validate(newPassword, false);

    var user = platformUserRepository.findById(recoveryCode.getUserId())
        .orElseThrow(() -> new UserNotFoundException("id", recoveryCode.getUserId().toString()));

    String newHash = credentialEncoder.encode(newPassword);
    user.updatePassword(PasswordHash.of(newHash));
    if (user.isPending()) {
      user.activate();
    }
    platformUserRepository.save(user);

    codeRepository.markUsed(recoveryCode);

    return new RecoverPasswordResult(true);
  }
}
