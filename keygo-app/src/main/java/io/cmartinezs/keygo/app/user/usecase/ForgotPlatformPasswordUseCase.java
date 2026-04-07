package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.app.user.result.ForgotPasswordResult;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.VerificationCode;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso: solicitar un token de recuperación de contraseña para un usuario de plataforma.
 *
 * <p>Equivalente a {@link ForgotPasswordUseCase} pero sin tenant scope.
 * Se invoca desde {@code POST /api/v1/platform/account/forgot-password}.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class ForgotPlatformPasswordUseCase {

  private static final int TOKEN_TTL_MINUTES = 30;

  private final PlatformUserRepositoryPort platformUserRepository;
  private final VerificationCodeRepositoryPort codeRepository;
  private final EmailNotificationPort emailNotification;

  public ForgotPlatformPasswordUseCase(
      PlatformUserRepositoryPort platformUserRepository,
      VerificationCodeRepositoryPort codeRepository,
      EmailNotificationPort emailNotification) {
    this.platformUserRepository = platformUserRepository;
    this.codeRepository = codeRepository;
    this.emailNotification = emailNotification;
  }

  /**
   * Ejecuta la solicitud de recuperación de contraseña.
   *
   * @param email dirección de correo del usuario de plataforma
   * @return resultado con {@code sent = true} siempre (no revela si el email existe)
   */
  public ForgotPasswordResult execute(String email) {
    var userOpt = platformUserRepository.findByEmail(EmailAddress.of(email));

    if (userOpt.isEmpty()) {
      return new ForgotPasswordResult(true);
    }

    var user = userOpt.get();

    String rawToken = UUID.randomUUID().toString().replace("-", "");
    Instant expiresAt = Instant.now().plus(TOKEN_TTL_MINUTES, ChronoUnit.MINUTES);

    var recoveryCode = VerificationCode.create(
        user.getId(), VerificationPurpose.PASSWORD_RECOVERY, rawToken, expiresAt);
    codeRepository.upsert(recoveryCode);

    emailNotification.sendEmail(
        EmailNotificationPort.TYPE_PASSWORD_RECOVERY,
        email,
        user.getUsername().value(),
        Map.of(
            "userUsername", user.getUsername().value(),
            "userFirstName", user.getFirstName() != null ? user.getFirstName() : "",
            "userLastName", user.getLastName() != null ? user.getLastName() : "",
            "recoveryToken", rawToken));

    return new ForgotPasswordResult(true);
  }
}
