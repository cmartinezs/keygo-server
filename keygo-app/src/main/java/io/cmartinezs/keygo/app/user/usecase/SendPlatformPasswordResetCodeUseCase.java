package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.app.user.result.SendPasswordResetCodeResult;
import io.cmartinezs.keygo.domain.shared.util.EmailMasker;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.Username;
import io.cmartinezs.keygo.domain.user.model.VerificationCode;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * Caso de uso: enviar código de verificación para reset de contraseña a un usuario de plataforma.
 *
 * <p>Equivalente a {@link SendPasswordResetCodeUseCase} pero para usuarios globales de plataforma
 * (sin tenant scope). Se invoca desde {@code PlatformAuthController} cuando el login detecta
 * {@code UserPasswordResetRequiredException}.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class SendPlatformPasswordResetCodeUseCase {

  private static final int CODE_TTL_MINUTES = 15;
  private final SecureRandom secureRandom = new SecureRandom();

  private final PlatformUserRepositoryPort platformUserRepository;
  private final VerificationCodeRepositoryPort codeRepository;
  private final EmailNotificationPort emailNotification;

  public SendPlatformPasswordResetCodeUseCase(
      PlatformUserRepositoryPort platformUserRepository,
      VerificationCodeRepositoryPort codeRepository,
      EmailNotificationPort emailNotification) {
    this.platformUserRepository = platformUserRepository;
    this.codeRepository = codeRepository;
    this.emailNotification = emailNotification;
  }

  /**
   * Genera, persiste y envía el código de verificación al usuario de plataforma.
   *
   * @param emailOrUsername email o username del usuario
   * @return resultado con el {@code requestId} de la solicitud persistida
   * @throws UserNotFoundException si el usuario no existe
   */
  public SendPasswordResetCodeResult execute(String emailOrUsername) {
    PlatformUser user = findUser(emailOrUsername);

    String rawCode = generateCode();
    Instant expiresAt = Instant.now().plus(CODE_TTL_MINUTES, ChronoUnit.MINUTES);

    var resetCode = VerificationCode.create(
        user.getId(), VerificationPurpose.PASSWORD_RESET, rawCode, expiresAt);
    VerificationCode persisted = codeRepository.upsert(resetCode);

    emailNotification.sendEmail(
        EmailNotificationPort.TYPE_PASSWORD_RESET_CODE,
        user.getEmail().value(),
        user.getUsername().value(),
        Map.of(
            "userUsername", user.getUsername().value(),
            "userFirstName", user.getFirstName() != null ? user.getFirstName() : "",
            "userLastName", user.getLastName() != null ? user.getLastName() : "",
            "verificationCode", rawCode,
            "reset_code_id", persisted.getId().toString(),
            "expiresInMinutes", CODE_TTL_MINUTES));

    return new SendPasswordResetCodeResult(persisted.getId(), EmailMasker.mask(user.getEmail().value()));
  }

  private PlatformUser findUser(String emailOrUsername) {
    boolean looksLikeEmail = emailOrUsername.contains("@");
    if (looksLikeEmail) {
      return platformUserRepository.findByEmail(EmailAddress.of(emailOrUsername))
          .orElseThrow(() -> new UserNotFoundException("email", emailOrUsername));
    }
    return platformUserRepository.findByUsername(Username.of(emailOrUsername))
        .orElseThrow(() -> new UserNotFoundException("username", emailOrUsername));
  }

  String generateCode() {
    int value = secureRandom.nextInt(1_000_000);
    return String.format("%06d", value);
  }
}
