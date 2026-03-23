package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.RegisterTenantUserCommand;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.EmailVerificationRepositoryPort;
import io.cmartinezs.keygo.app.user.port.PasswordHasherPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.DuplicateUserException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.EmailVerification;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Use case: register a new user in a tenant's client app.
 * <p>Caso de uso: registrar un nuevo usuario en la app de un tenant.
 * The user is created with PENDING status. A 6-digit verification code is generated,
 * persisted, and sent to the user's email. The code is valid for 30 minutes.
 * <p>El usuario se crea con estado PENDING. Se genera un código de 6 dígitos, se persiste
 * y se envía al email del usuario. El código es válido por 30 minutos.
 * @author cmartinezs
 * @version 1.0
 */
public class RegisterTenantUserUseCase {

  private static final int VERIFICATION_CODE_LENGTH = 6;
  private static final int VERIFICATION_EXPIRY_MINUTES = 30;

  private final TenantRepositoryPort tenantRepositoryPort;
  private final ClientAppRepositoryPort clientAppRepositoryPort;
  private final UserRepositoryPort userRepositoryPort;
  private final PasswordHasherPort passwordHasherPort;
  private final EmailVerificationRepositoryPort emailVerificationRepositoryPort;
  private final EmailNotificationPort emailNotificationPort;
  private final SecureRandom secureRandom;

  public RegisterTenantUserUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      PasswordHasherPort passwordHasherPort,
      EmailVerificationRepositoryPort emailVerificationRepositoryPort,
      EmailNotificationPort emailNotificationPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.clientAppRepositoryPort = clientAppRepositoryPort;
    this.userRepositoryPort = userRepositoryPort;
    this.passwordHasherPort = passwordHasherPort;
    this.emailVerificationRepositoryPort = emailVerificationRepositoryPort;
    this.emailNotificationPort = emailNotificationPort;
    this.secureRandom = new SecureRandom();
  }

  /**
   * Execute the registration flow.
   * @param command the registration command
   * @return the created (PENDING) User
   * @throws TenantNotFoundException     if the tenant does not exist
   * @throws TenantSuspendedException    if the tenant is suspended
   * @throws ClientAppNotFoundException  if the client app does not belong to this tenant
   * @throws DuplicateUserException      if email or username already exists in the tenant
   */
  public User execute(RegisterTenantUserCommand command) {
    // 1. Validate tenant
    Tenant tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    if (tenant.isSuspended()) {
      throw new TenantSuspendedException(command.tenantSlug());
    }

    // 2. Validate client app belongs to this tenant
    clientAppRepositoryPort.findByClientIdAndTenantId(ClientId.of(command.clientId()), tenant.getId())
        .orElseThrow(() -> new ClientAppNotFoundException(command.clientId()));

    // 3. Check uniqueness within tenant
    EmailAddress email = EmailAddress.of(command.email());
    Username username = Username.of(command.username());

    if (userRepositoryPort.existsByTenantIdAndEmail(tenant.getId(), email)) {
      throw new DuplicateUserException("email", command.email());
    }
    if (userRepositoryPort.existsByTenantIdAndUsername(tenant.getId(), username)) {
      throw new DuplicateUserException("username", command.username());
    }

    // 4. Create user with PENDING status
    String hashedPassword = passwordHasherPort.hash(command.rawPassword());
    User user = User.builder()
        .id(UserId.generate())
        .tenantId(tenant.getId())
        .username(username)
        .email(email)
        .passwordHash(PasswordHash.of(hashedPassword))
        .firstName(command.firstName())
        .lastName(command.lastName())
        .status(UserStatus.PENDING)
        .build();

    User savedUser = userRepositoryPort.save(user);

    // 5. Generate and persist verification code
    String code = generateCode();
    Instant expiresAt = Instant.now().plus(VERIFICATION_EXPIRY_MINUTES, ChronoUnit.MINUTES);
    EmailVerification verification = EmailVerification.create(savedUser.getId(), tenant.getId(), code, expiresAt);
    emailVerificationRepositoryPort.save(verification);

    // 6. Send verification email
    emailNotificationPort.sendVerificationEmail(email.value(), username.value(), code);

    return savedUser;
  }

  private String generateCode() {
    int max = (int) Math.pow(10, VERIFICATION_CODE_LENGTH);
    int code = secureRandom.nextInt(max);
    return String.format("%0" + VERIFICATION_CODE_LENGTH + "d", code);
  }
}

