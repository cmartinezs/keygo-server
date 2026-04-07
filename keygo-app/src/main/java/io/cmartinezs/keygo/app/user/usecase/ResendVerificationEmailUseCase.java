package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.ClockPort;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.ResendVerificationCommand;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.VerificationCode;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;
import io.cmartinezs.keygo.domain.user.model.User;

import java.security.SecureRandom;
import java.time.temporal.ChronoUnit;

/**
 * Use case: resend a verification email to a pending user.
 * <p>Caso de uso: reenviar el email de verificación a un usuario pendiente.
 *
 * <p><strong>Policy (aligned with the billing contracting flow):</strong>
 * <ul>
 *   <li>If the current code is still valid (not expired, not used) → resend the <em>same</em> code.
 *       The user simply did not receive the previous email.</li>
 *   <li>If the code has expired or there is no previous code → generate a new one, persist it
 *       atomically (pessimistic lock prevents duplicate inserts under concurrent requests), and
 *       send the email.</li>
 * </ul>
 *
 * @author cmartinezs
 * @version 1.1
 */
public class ResendVerificationEmailUseCase {

  private static final int VERIFICATION_CODE_LENGTH = 6;
  private static final int VERIFICATION_EXPIRY_MINUTES = 30;

  private final TenantRepositoryPort tenantRepositoryPort;
  private final ClientAppRepositoryPort clientAppRepositoryPort;
  private final UserRepositoryPort userRepositoryPort;
  private final VerificationCodeRepositoryPort verificationCodeRepositoryPort;
  private final EmailNotificationPort emailNotificationPort;
  private final ClockPort clock;
  private final SecureRandom secureRandom;

  public ResendVerificationEmailUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      VerificationCodeRepositoryPort verificationCodeRepositoryPort,
      EmailNotificationPort emailNotificationPort,
      ClockPort clock) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.clientAppRepositoryPort = clientAppRepositoryPort;
    this.userRepositoryPort = userRepositoryPort;
    this.verificationCodeRepositoryPort = verificationCodeRepositoryPort;
    this.emailNotificationPort = emailNotificationPort;
    this.clock = clock;
    this.secureRandom = new SecureRandom();
  }

  /**
   * Execute the resend verification flow.
   *
   * @param command the resend command
   * @throws TenantNotFoundException     if the tenant does not exist
   * @throws ClientAppNotFoundException  if the client app does not belong to this tenant
   * @throws UserNotFoundException       if no user matches the email within the tenant
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
        .orElseThrow(() -> new UserNotFoundException("email", command.email()));

    // 3. Prepare a new verification in case the current one is expired or absent.
    //    saveIfExpiredOrAbsent will atomically return the existing valid one if it exists,
    //    or persist and return the new one — preventing concurrent duplicate inserts.
    String newCode = generateCode();
    VerificationCode newVerification = VerificationCode.create(
        user.getId(), VerificationPurpose.EMAIL_VERIFICATION, newCode,
        clock.now().plus(VERIFICATION_EXPIRY_MINUTES, ChronoUnit.MINUTES));

    VerificationCode active = verificationCodeRepositoryPort
        .upsertIfExpiredOrAbsent(user.getId(), VerificationPurpose.EMAIL_VERIFICATION, newVerification);

    // 4. Send whichever code is active (same or newly generated)
    emailNotificationPort.sendVerificationEmail(email.value(), user.getUsername().value(), active.getCode());
  }

  private String generateCode() {
    int max = (int) Math.pow(10, VERIFICATION_CODE_LENGTH);
    int code = secureRandom.nextInt(max);
    return String.format("%0" + VERIFICATION_CODE_LENGTH + "d", code);
  }
}

