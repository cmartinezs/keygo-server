package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.contracting.exception.ContractNotFoundException;
import io.cmartinezs.keygo.app.billing.contracting.exception.ProviderAppNotFoundException;
import io.cmartinezs.keygo.app.membership.exception.AppRoleNotFoundException;
import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.PasswordHasherPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contractor.model.Contractor;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.MembershipId;
import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;
import io.cmartinezs.keygo.domain.membership.model.RoleCode;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Use case: verify the email code of a contract — billing model v2.
 * <ol>
 *   <li>Validates the 6-digit code.</li>
 *   <li>Finds or creates a TenantUser in the provider's tenant using the contractor email.</li>
 *   <li>If the user is new, generates a secure temporary password, hashes it, assigns status
 *       RESET_PASSWORD and sends the credentials by email.</li>
 *   <li>Finds or creates a Contractor linked to that TenantUser.</li>
 *   <li>Links the Contractor to the contract and advances status to PENDING_PAYMENT.</li>
 * </ol>
 *
 * @author cmartinezs
 * @version 2.0
 */
public class VerifyContractEmailUseCase {

  private static final String PASSWORD_CHARS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
  private static final int PASSWORD_LENGTH = 14;

  private final AppContractRepositoryPort contractRepo;
  private final ClientAppRepositoryPort clientAppRepo;
  private final UserRepositoryPort userRepo;
  private final ContractorRepositoryPort contractorRepo;
  private final MembershipRepositoryPort membershipRepo;
  private final AppRoleRepositoryPort appRoleRepo;
  private final PasswordHasherPort passwordHasher;
  private final EmailNotificationPort emailNotification;
  private final SecureRandom secureRandom;

  public VerifyContractEmailUseCase(
      AppContractRepositoryPort contractRepo,
      ClientAppRepositoryPort clientAppRepo,
      UserRepositoryPort userRepo,
      ContractorRepositoryPort contractorRepo,
      MembershipRepositoryPort membershipRepo,
      AppRoleRepositoryPort appRoleRepo,
      PasswordHasherPort passwordHasher,
      EmailNotificationPort emailNotification) {
    this.contractRepo = contractRepo;
    this.clientAppRepo = clientAppRepo;
    this.userRepo = userRepo;
    this.contractorRepo = contractorRepo;
    this.membershipRepo = membershipRepo;
    this.appRoleRepo = appRoleRepo;
    this.passwordHasher = passwordHasher;
    this.emailNotification = emailNotification;
    this.secureRandom = new SecureRandom();
  }

  public AppContractResult execute(UUID contractId, String inputCode) {
    var contract = contractRepo.findById(contractId)
        .orElseThrow(() -> new ContractNotFoundException(contractId));

    OffsetDateTime now = OffsetDateTime.now();

    // Extract fields before lambdas (contract gets reassigned later → not effectively final)
    final UUID clientAppId = contract.getClientAppId();
    final String contractorEmail   = contract.getContractorEmail();
    final String contractorFirst   = contract.getContractorFirstName();
    final String contractorLast    = contract.getContractorLastName();
    final String generatedUsername = contract.generateUsername();

    // Resolve provider tenant from the contract's client app
    var providerApp = clientAppRepo.findById(ClientAppId.of(clientAppId))
        .orElseThrow(() -> new ProviderAppNotFoundException(clientAppId));

    // Find or create TenantUser in the provider's tenant
    final EmailAddress email   = EmailAddress.of(contractorEmail);
    final var tenantId         = providerApp.getTenantId();

    User tenantUser = userRepo.findByTenantIdAndEmail(tenantId, email)
        .orElseGet(() -> {
          String rawPassword  = generateTemporaryPassword();
          String hashedPwd    = passwordHasher.hash(rawPassword);

          User newUser = User.builder()
              .tenantId(tenantId)
              .email(email)
              .username(Username.of(generatedUsername))
              .firstName(contractorFirst)
              .lastName(contractorLast)
              .passwordHash(PasswordHash.of(hashedPwd))
              .status(UserStatus.RESET_PASSWORD)
              .build();

          User saved = userRepo.save(newUser);

          // Send temporary password by email (fire-and-forget; IllegalStateException on failure)
          emailNotification.sendTemporaryPasswordEmail(
              contractorEmail, generatedUsername, rawPassword);

          return saved;
        });

    // Find or create Contractor linked to this TenantUser
    final UUID tenantUserIdFinal = tenantUser.getId().value();
    Contractor contractor = contractorRepo.findByTenantUserId(tenantUserIdFinal)
        .orElseGet(() -> {
          Contractor newContractor = Contractor.builder()
              .id(UUID.randomUUID())
              .tenantUserId(tenantUserIdFinal)
              .status(ContractorStatus.PENDING)
              .build();
          return contractorRepo.save(newContractor);
        });

    // Ensure membership exists for this user in the provider's client app
    if (!membershipRepo.existsByUserAndClientApp(tenantUserIdFinal, clientAppId)) {
      Membership membership = Membership.builder()
          .id(MembershipId.generate())
          .userId(UserId.of(tenantUserIdFinal))
          .clientAppId(ClientAppId.of(clientAppId))
          .status(MembershipStatus.ACTIVE)
          .build();

      // Assign ADMIN_TENANT role so the contractor can manage their own tenant
      AppRole adminTenantRole = appRoleRepo.findByClientAppAndCode(clientAppId, RoleCode.adminTenantRole())
          .orElseThrow(() -> new AppRoleNotFoundException(clientAppId, RoleCode.adminTenantRole()));
      membership.assignRole(adminTenantRole);

      membershipRepo.save(membership);
    }

    // Verify code and link contractor — advances status to PENDING_PAYMENT
    contract.verifyCode(inputCode, contractor.getId(), now);
    contract = contractRepo.save(contract);

    // subscription is null at this stage — it is created during ActivateAppContractUseCase
    return new AppContractResult(contract, null);
  }

  /**
   * Generates a cryptographically secure temporary password of {@value #PASSWORD_LENGTH} characters.
   * Contains at least one uppercase letter, one lowercase letter, one digit and one special character.
   * <p>Genera una contraseña temporal criptográficamente segura de {@value #PASSWORD_LENGTH} caracteres.
   */
  String generateTemporaryPassword() {
    // Guarantee at least one char from each required class
    String upper   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String lower   = "abcdefghijklmnopqrstuvwxyz";
    String digits  = "0123456789";
    String special = "!@#$%&*";

    char[] pwd = new char[PASSWORD_LENGTH];
    pwd[0] = upper.charAt(secureRandom.nextInt(upper.length()));
    pwd[1] = lower.charAt(secureRandom.nextInt(lower.length()));
    pwd[2] = digits.charAt(secureRandom.nextInt(digits.length()));
    pwd[3] = special.charAt(secureRandom.nextInt(special.length()));

    for (int i = 4; i < PASSWORD_LENGTH; i++) {
      pwd[i] = PASSWORD_CHARS.charAt(secureRandom.nextInt(PASSWORD_CHARS.length()));
    }

    // Fisher-Yates shuffle to randomize character positions
    for (int i = PASSWORD_LENGTH - 1; i > 0; i--) {
      int j = secureRandom.nextInt(i + 1);
      char tmp = pwd[i];
      pwd[i] = pwd[j];
      pwd[j] = tmp;
    }

    return new String(pwd);
  }
}
