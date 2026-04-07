package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.SendPasswordResetCodeCommand;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.VerificationCode;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;
import io.cmartinezs.keygo.app.user.result.SendPasswordResetCodeResult;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.Username;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

/**
 * Caso de uso: enviar un código de verificación de 6 dígitos al usuario bloqueado por
 * {@code status=RESET_PASSWORD}.
 *
 * <p>Se invoca desde {@code AuthorizationController.login()} cuando
 * {@code AuthenticateUserForAuthorizationUseCase} lanza
 * {@code UserPasswordResetRequiredException}.
 *
 * <p>Flujo:
 * <ol>
 *   <li>Resolver el tenant por {@code tenantSlug}.</li>
 *   <li>Buscar el usuario por email o username.</li>
 *   <li>Generar un código de 6 dígitos aleatorio.</li>
 *   <li>Persistir (upsert — invalida código anterior si existía) con TTL de 15 minutos.</li>
 *   <li>Enviar el código al email del usuario.</li>
 * </ol>
 *
 * @author cmartinezs
 * @version 1.0
 */
public class SendPasswordResetCodeUseCase {

  private static final int CODE_TTL_MINUTES = 15;
  private final SecureRandom secureRandom = new SecureRandom();

  private final TenantRepositoryPort tenantRepository;
  private final UserRepositoryPort userRepository;
  private final VerificationCodeRepositoryPort codeRepository;
  private final EmailNotificationPort emailNotification;

  public SendPasswordResetCodeUseCase(
      TenantRepositoryPort tenantRepository,
      UserRepositoryPort userRepository,
      VerificationCodeRepositoryPort codeRepository,
      EmailNotificationPort emailNotification) {
    this.tenantRepository = tenantRepository;
    this.userRepository = userRepository;
    this.codeRepository = codeRepository;
    this.emailNotification = emailNotification;
  }

  /**
   * Genera, persiste y envía el código de verificación.
   *
   * @param command parámetros del comando
   * @return resultado con el {@code requestId} de la solicitud persistida
   * @throws TenantNotFoundException si el tenant no existe
   * @throws UserNotFoundException   si el usuario no existe en el tenant
   */
  public SendPasswordResetCodeResult execute(SendPasswordResetCodeCommand command) {
    var tenant = tenantRepository.findBySlug(TenantSlug.of(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    User user = findUser(tenant.getId(), command.emailOrUsername());

    String rawCode = generateCode();
    Instant expiresAt = Instant.now().plus(CODE_TTL_MINUTES, ChronoUnit.MINUTES);

    var resetCode = VerificationCode.create(user.getId(), VerificationPurpose.PASSWORD_RESET, rawCode, expiresAt);
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
            "expiresInMinutes", CODE_TTL_MINUTES));

    return new SendPasswordResetCodeResult(persisted.getId());
  }

  private User findUser(io.cmartinezs.keygo.domain.tenant.model.TenantId tenantId, String emailOrUsername) {
    Optional<User> userOpt = tryFindByEmail(tenantId, emailOrUsername);
    if (userOpt.isEmpty()) {
      userOpt = tryFindByUsername(tenantId, emailOrUsername);
    }
    return userOpt.orElseThrow(() -> new UserNotFoundException("email/username", emailOrUsername));
  }

  private Optional<User> tryFindByEmail(io.cmartinezs.keygo.domain.tenant.model.TenantId tenantId, String credential) {
    try {
      return userRepository.findByTenantIdAndEmail(tenantId, EmailAddress.of(credential));
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
  }

  private Optional<User> tryFindByUsername(io.cmartinezs.keygo.domain.tenant.model.TenantId tenantId, String credential) {
    try {
      return userRepository.findByTenantIdAndUsername(tenantId, Username.of(credential));
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
  }

  /** Genera un código numérico de 6 dígitos con ceros a la izquierda si es necesario. */
  String generateCode() {
    int value = secureRandom.nextInt(1_000_000);
    return String.format("%06d", value);
  }
}

