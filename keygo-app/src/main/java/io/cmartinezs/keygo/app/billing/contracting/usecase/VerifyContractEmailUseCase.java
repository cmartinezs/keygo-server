package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.contracting.exception.ContractNotFoundException;
import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.PlatformUserRoleRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contractor.model.Contractor;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorStatus;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Use case: verify the email code of a contract — billing model v2 (platform-centric).
 * <ol>
 *   <li>Validates the 6-digit code.</li>
 *   <li>Finds or creates a PlatformUser using the contractor email.</li>
 *   <li>If the user is new, generates a secure temporary password, hashes it, assigns status
 *       RESET_PASSWORD and sends the credentials by email.</li>
 *   <li>Assigns platform roles (keygo_user, keygo_tenant_admin) if not already present.</li>
 *   <li>Finds or creates a Contractor linked to that PlatformUser.</li>
 *   <li>Links the Contractor to the contract and advances status to PENDING_PAYMENT.</li>
 * </ol>
 *
 * @author cmartinezs
 * @version 3.0
 */
public class VerifyContractEmailUseCase {

  private static final String PASSWORD_CHARS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
  private static final int PASSWORD_LENGTH = 14;

  private final AppContractRepositoryPort contractRepo;
  private final PlatformUserRepositoryPort platformUserRepo;
  private final PlatformUserRoleRepositoryPort platformUserRoleRepo;
  private final ContractorRepositoryPort contractorRepo;
  private final CredentialEncoderPort credentialEncoder;
  private final EmailNotificationPort emailNotification;
  private final SecureRandom secureRandom;

  public VerifyContractEmailUseCase(
      AppContractRepositoryPort contractRepo,
      PlatformUserRepositoryPort platformUserRepo,
      PlatformUserRoleRepositoryPort platformUserRoleRepo,
      ContractorRepositoryPort contractorRepo,
      CredentialEncoderPort credentialEncoder,
      EmailNotificationPort emailNotification) {
    this.contractRepo = contractRepo;
    this.platformUserRepo = platformUserRepo;
    this.platformUserRoleRepo = platformUserRoleRepo;
    this.contractorRepo = contractorRepo;
    this.credentialEncoder = credentialEncoder;
    this.emailNotification = emailNotification;
    this.secureRandom = new SecureRandom();
  }

  public AppContractResult execute(UUID contractId, String inputCode) {
    var contract = contractRepo.findById(contractId)
        .orElseThrow(() -> new ContractNotFoundException(contractId));

    OffsetDateTime now = OffsetDateTime.now();

    // ── Validate code FIRST — before any side effects ──
    contract.validateVerificationCode(inputCode, now);

    final String contractorEmail = contract.getContractorEmail();
    final String contractorFirst = contract.getContractorFirstName();
    final String contractorLast  = contract.getContractorLastName();
    final String generatedUsername = contract.generateUsername();

    // Find or create PlatformUser
    PlatformUser platformUser = platformUserRepo.findByEmail(EmailAddress.of(contractorEmail))
        .orElseGet(() -> {
          String rawPassword = generateTemporaryPassword();
          String hashedPwd   = credentialEncoder.encode(rawPassword);

          PlatformUser newUser = PlatformUser.builder()
              .username(Username.of(generatedUsername))
              .email(EmailAddress.of(contractorEmail))
              .passwordHash(PasswordHash.of(hashedPwd))
              .status(UserStatus.RESET_PASSWORD)
              .firstName(contractorFirst)
              .lastName(contractorLast)
              .build();

          PlatformUser saved = platformUserRepo.save(newUser);

          emailNotification.sendEmail(
              EmailNotificationPort.TYPE_TEMPORARY_PASSWORD,
              contractorEmail, generatedUsername,
              Map.of("userUsername", generatedUsername,
                  "userFirstName", contractorFirst != null ? contractorFirst : "",
                  "userLastName", contractorLast != null ? contractorLast : "",
                  "temporaryPassword", rawPassword));

          return saved;
        });

    // Assign platform roles if not already present
    UUID platformUserId = platformUser.getId().value();
    if (!platformUserRoleRepo.hasRole(platformUserId, "keygo_user")) {
      platformUserRoleRepo.assign(platformUserId, "keygo_user");
    }
    if (!platformUserRoleRepo.hasRole(platformUserId, "keygo_tenant_admin")) {
      platformUserRoleRepo.assign(platformUserId, "keygo_tenant_admin");
    }

    // Find or create Contractor linked to this PlatformUser
    Contractor contractor = contractorRepo.findByPlatformUserId(platformUserId)
        .orElseGet(() -> {
          Contractor newContractor = Contractor.builder()
              .platformUserId(platformUserId)
              .status(ContractorStatus.PENDING)
              .build();
          return contractorRepo.save(newContractor);
        });

    // Link contractor and advance status to PENDING_PAYMENT
    contract.verifyCode(inputCode, contractor.getId(), now);
    contract = contractRepo.save(contract);

    return new AppContractResult(contract, null);
  }

  /**
   * Generates a cryptographically secure temporary password of {@value #PASSWORD_LENGTH} characters.
   * Contains at least one uppercase letter, one lowercase letter, one digit and one special character.
   */
  String generateTemporaryPassword() {
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
