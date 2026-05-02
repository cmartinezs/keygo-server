package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.RegisterTenantUserCommand;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.exception.SelfRegistrationNotAllowedException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.RegistrationPolicy;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.DuplicateUserException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PasswordValidationHelper;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import io.cmartinezs.keygo.domain.user.model.VerificationCode;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Use case: register a new user in a tenant's client app.
 *
 * <p>Caso de uso: registrar un nuevo usuario en la app de un tenant. The user is created with
 * PENDING status. A 6-digit verification code is generated, persisted, and sent to the user's
 * email. The code is valid for 30 minutes.
 *
 * <p>El usuario se crea con estado PENDING. Se genera un código de 6 dígitos, se persiste y se
 * envía al email del usuario. El código es válido por 30 minutos.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class RegisterTenantUserUseCase {

  private static final int VERIFICATION_CODE_LENGTH = 6;
  private static final int VERIFICATION_EXPIRY_MINUTES = 30;
  private static final int USERNAME_MAX_LENGTH = 100;
  private static final int USERNAME_COUNTER_DIGITS = 2;

  private final TenantRepositoryPort tenantRepositoryPort;
  private final ClientAppRepositoryPort clientAppRepositoryPort;
  private final UserRepositoryPort userRepositoryPort;
  private final CredentialEncoderPort credentialEncoderPort;
  private final VerificationCodeRepositoryPort verificationCodeRepositoryPort;
  private final EmailNotificationPort emailNotificationPort;
  private final SecureRandom secureRandom;

  public RegisterTenantUserUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      CredentialEncoderPort credentialEncoderPort,
      VerificationCodeRepositoryPort verificationCodeRepositoryPort,
      EmailNotificationPort emailNotificationPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.clientAppRepositoryPort = clientAppRepositoryPort;
    this.userRepositoryPort = userRepositoryPort;
    this.credentialEncoderPort = credentialEncoderPort;
    this.verificationCodeRepositoryPort = verificationCodeRepositoryPort;
    this.emailNotificationPort = emailNotificationPort;
    this.secureRandom = new SecureRandom();
  }

  /**
   * Execute the registration flow.
   *
   * @param command the registration command
   * @return the created (PENDING) User
   * @throws TenantNotFoundException if the tenant does not exist
   * @throws TenantSuspendedException if the tenant is suspended
   * @throws ClientAppNotFoundException if the client app does not belong to this tenant
   * @throws SelfRegistrationNotAllowedException if the app's registration policy is INVITE_ONLY
   * @throws DuplicateUserException if email or username already exists in the tenant
   */
  public User execute(RegisterTenantUserCommand command) {
    // 1. Validate tenant
    Tenant tenant =
        tenantRepositoryPort
            .findBySlug(TenantSlug.of(command.tenantSlug()))
            .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    if (tenant.isSuspended()) {
      throw new TenantSuspendedException(command.tenantSlug());
    }

    // 2. Validate client app belongs to this tenant and allows self-registration
    var clientApp =
        clientAppRepositoryPort
            .findByClientIdAndTenantId(ClientId.of(command.clientId()), tenant.getId())
            .orElseThrow(() -> new ClientAppNotFoundException(command.clientId()));

    if (RegistrationPolicy.INVITE_ONLY.equals(clientApp.getRegistrationPolicy())) {
      throw new SelfRegistrationNotAllowedException(command.clientId());
    }

    // 3. Check uniqueness within tenant
    EmailAddress email = EmailAddress.of(command.email());

    if (userRepositoryPort.existsByTenantIdAndEmail(tenant.getId(), email)) {
      throw new DuplicateUserException("email", command.email());
    }

    // 3.1 Generate username if not provided
    Username username;
    if (command.username() != null && !command.username().isBlank()) {
      username = Username.of(command.username());
      if (userRepositoryPort.existsByTenantIdAndUsername(tenant.getId(), username)) {
        throw new DuplicateUserException("username", command.username());
      }
    } else {
      username = generateUsername(tenant.getId(), command.firstName(), command.lastName());
    }

    // 4. Validar política de la contraseña (permanente, proporcionada por usuario)
    PasswordValidationHelper.validate(command.rawPassword(), false);

    // 5. Create user with PENDING status
    String hashedPassword = credentialEncoderPort.encode(command.rawPassword());
    User user =
        User.builder()
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
    VerificationCode verification =
        VerificationCode.create(
            savedUser.getId(), VerificationPurpose.EMAIL_VERIFICATION, code, expiresAt);
    verificationCodeRepositoryPort.upsert(verification);

    // 6. Send verification email
    emailNotificationPort.sendEmail(
        EmailNotificationPort.TYPE_EMAIL_VERIFICATION,
        email.value(),
        username.value(),
        Map.of(
            "userUsername",
            username.value(),
            "userFirstName",
            command.firstName() != null ? command.firstName() : "",
            "userLastName",
            command.lastName() != null ? command.lastName() : "",
            "verificationCode",
            code,
            "registration_id",
            savedUser.getId().value(),
            "client_id",
            command.clientId(),
            "expiresInMinutes",
            VERIFICATION_EXPIRY_MINUTES));

    return savedUser;
  }

  private String generateCode() {
    int max = (int) Math.pow(10, VERIFICATION_CODE_LENGTH);
    int code = secureRandom.nextInt(max);
    return String.format("%0" + VERIFICATION_CODE_LENGTH + "d", code);
  }

  private Username generateUsername(TenantId tenantId, String firstName, String lastName) {
    String base = normalizeUsernameBase(buildUsernameBase(firstName, lastName));
    String prefix = base.toLowerCase();

    List<String> taken =
        userRepositoryPort.findUsernamesByPrefix(tenantId, prefix).stream()
            .map(Username::value)
            .map(String::toLowerCase)
            .toList();

    int counter = 1;
    String candidate;

    if (!taken.contains(prefix)) {
      return Username.of(prefix);
    }

    do {
      candidate = buildCandidate(prefix, counter);
      counter++;
    } while (taken.contains(candidate.toLowerCase()));

    return Username.of(candidate);
  }

  private String buildUsernameBase(String firstName, String lastName) {
    String first = firstName != null ? firstName.trim() : "";
    String last = lastName != null ? lastName.trim() : "";

    if (!first.isEmpty() && !last.isEmpty()) {
      String initial = first.substring(0, 1);
      return initial + last;
    }
    if (!last.isEmpty()) {
      return last;
    }
    if (!first.isEmpty()) {
      return first;
    }
    return "user";
  }

  private String normalizeUsernameBase(String input) {
    return Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
  }

  private String buildCandidate(String prefix, int counter) {
    String paddedCounter = String.format("%0" + USERNAME_COUNTER_DIGITS + "d", counter);
    int maxPrefixLength = USERNAME_MAX_LENGTH - USERNAME_COUNTER_DIGITS;

    if (prefix.length() + paddedCounter.length() <= USERNAME_MAX_LENGTH) {
      return prefix + paddedCounter;
    }
    int truncationLen = prefix.length() - maxPrefixLength;
    return prefix.substring(0, prefix.length() - truncationLen) + paddedCounter;
  }
}
