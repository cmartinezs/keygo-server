package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.VerifyEmailCommand;
import io.cmartinezs.keygo.app.user.port.EmailVerificationRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.EmailVerificationExpiredException;
import io.cmartinezs.keygo.domain.user.exception.EmailVerificationInvalidException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.EmailVerification;
import io.cmartinezs.keygo.domain.user.model.User;

/**
 * Use case: verify a user's email address using the code sent during registration.
 * <p>Caso de uso: verificar el email de un usuario usando el código enviado durante el registro.
 * If the code is valid and not expired, the user is activated (status → ACTIVE).
 * If the code is expired, an exception is thrown informing to request a new code.
 * If the code is incorrect or already used, an exception is thrown.
 * <p>Si el código es válido y no expiró, el usuario queda activado (estado → ACTIVE).
 * Si el código expiró, se lanza una excepción indicando que se debe solicitar uno nuevo.
 * Si el código es incorrecto o ya fue usado, se lanza una excepción.
 * @author cmartinezs
 * @version 1.0
 */
public class VerifyEmailUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final ClientAppRepositoryPort clientAppRepositoryPort;
  private final UserRepositoryPort userRepositoryPort;
  private final EmailVerificationRepositoryPort emailVerificationRepositoryPort;

  public VerifyEmailUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      EmailVerificationRepositoryPort emailVerificationRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.clientAppRepositoryPort = clientAppRepositoryPort;
    this.userRepositoryPort = userRepositoryPort;
    this.emailVerificationRepositoryPort = emailVerificationRepositoryPort;
  }

  /**
   * Execute the email verification flow.
   * @param command the verification command
   * @return the activated User
   * @throws TenantNotFoundException            if the tenant does not exist
   * @throws ClientAppNotFoundException         if the client app does not belong to this tenant
   * @throws UserNotFoundException              if no user matches the email within the tenant
   * @throws EmailVerificationExpiredException  if the verification code has expired
   * @throws EmailVerificationInvalidException  if the code is incorrect or already used
   */
  public User execute(VerifyEmailCommand command) {
    // 1. Validate tenant and client app
    Tenant tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    clientAppRepositoryPort.findByClientIdAndTenantId(ClientId.of(command.clientId()), tenant.getId())
        .orElseThrow(() -> new ClientAppNotFoundException(command.clientId()));

    // 2. Find user by email
    EmailAddress email = EmailAddress.of(command.email());
    User user = userRepositoryPort.findByTenantIdAndEmail(tenant.getId(), email)
        .orElseThrow(() -> new UserNotFoundException(command.email()));

    // 3. Retrieve latest verification
    EmailVerification verification = emailVerificationRepositoryPort
        .findLatestByUserIdAndTenantId(user.getId(), tenant.getId())
        .orElseThrow(() -> new EmailVerificationInvalidException(command.email()));

    // 4. Check expiry first (expiry takes priority over wrong code)
    if (verification.isExpired()) {
      throw new EmailVerificationExpiredException(command.email());
    }

    // 5. Check if already used or code mismatch
    if (verification.isUsed() || !verification.getCode().equals(command.code())) {
      throw new EmailVerificationInvalidException(command.email());
    }

    // 6. Mark used and activate user
    verification.markUsed();
    emailVerificationRepositoryPort.save(verification);

    user.activate();
    return userRepositoryPort.save(user);
  }
}

