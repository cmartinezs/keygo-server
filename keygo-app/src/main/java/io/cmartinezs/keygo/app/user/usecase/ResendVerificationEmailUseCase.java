package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.ResendVerificationCommand;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.EmailVerificationRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.EmailVerificationStillActiveException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.EmailVerification;
import io.cmartinezs.keygo.domain.user.model.User;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Use case: resend a verification email to a pending user.
 * <p>Caso de uso: reenviar el email de verificación a un usuario pendiente.
 * A new code can only be requested once the previous one has expired.
 * <p>Un nuevo código solo se puede solicitar cuando el anterior ya ha expirado.
 * @author cmartinezs
 * @version 1.0
 */
public class ResendVerificationEmailUseCase {

  private static final int VERIFICATION_CODE_LENGTH = 6;
  private static final int VERIFICATION_EXPIRY_MINUTES = 30;

  private final TenantRepositoryPort tenantRepositoryPort;
  private final ClientAppRepositoryPort clientAppRepositoryPort;
  private final UserRepositoryPort userRepositoryPort;
  private final EmailVerificationRepositoryPort emailVerificationRepositoryPort;
  private final EmailNotificationPort emailNotificationPort;
  private final SecureRandom secureRandom;

  public ResendVerificationEmailUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      EmailVerificationRepositoryPort emailVerificationRepositoryPort,
      EmailNotificationPort emailNotificationPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.clientAppRepositoryPort = clientAppRepositoryPort;
    this.userRepositoryPort = userRepositoryPort;
    this.emailVerificationRepositoryPort = emailVerificationRepositoryPort;
    this.emailNotificationPort = emailNotificationPort;
    this.secureRandom = new SecureRandom();
  }

  /**
   * Execute the resend verification flow.
   * @param command the resend command
   * @throws TenantNotFoundException               if the tenant does not exist
   * @throws ClientAppNotFoundException            if the client app does not belong to this tenant
   * @throws UserNotFoundException                 if no user matches the email within the tenant
   * @throws EmailVerificationStillActiveException if the current code has not expired yet
   */
  public void execute(ResendVerificationCommand command) {
    // 1. Validate tenant and client app
    Tenant tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    clientAppRepositoryPort.findByClientIdAndTenantId(ClientId.of(command.clientId()), tenant.getId())
        .orElseThrow(() -> new ClientAppNotFoundException(command.clientId()));

    // 2. Find user by email
    EmailAddress email = EmailAddress.of(command.email());
    User user = userRepositoryPort.findByTenantIdAndEmail(tenant.getId(), email)
        .orElseThrow(() -> new UserNotFoundException(command.email()));

    // 3. Check if there is a still-active code (not expired and not used)
    emailVerificationRepositoryPort
        .findLatestByUserIdAndTenantId(user.getId(), tenant.getId())
        .ifPresent(existing -> {
          if (!existing.isExpired() && !existing.isUsed()) {
            throw new EmailVerificationStillActiveException(command.email());
          }
        });

    // 4. Generate new code and persist
    String code = generateCode();
    Instant expiresAt = Instant.now().plus(VERIFICATION_EXPIRY_MINUTES, ChronoUnit.MINUTES);
    EmailVerification verification = EmailVerification.create(user.getId(), tenant.getId(), code, expiresAt);
    emailVerificationRepositoryPort.save(verification);

    // 5. Send new verification email
    emailNotificationPort.sendVerificationEmail(email.value(), user.getUsername().value(), code);
  }

  private String generateCode() {
    int max = (int) Math.pow(10, VERIFICATION_CODE_LENGTH);
    int code = secureRandom.nextInt(max);
    return String.format("%0" + VERIFICATION_CODE_LENGTH + "d", code);
  }
}

