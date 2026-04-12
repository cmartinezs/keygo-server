package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.contracting.exception.ContractInvalidStateException;
import io.cmartinezs.keygo.app.billing.contracting.exception.ContractNotFoundException;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;
import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.billing.contractor.port.ContractorUserRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.membership.port.PlatformUserRoleRepositoryPort;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contractor.model.Contractor;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorStatus;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorType;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorUserRole;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Use case: simulate payment approval (dev/test only).
 * Reads keygo.billing.mock-payment-enabled flag. If false, returns 404.
 * @author cmartinezs
 * @version 1.0
 */
public class MockApprovePaymentUseCase {

  private static final String PASSWORD_CHARS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
  private static final int PASSWORD_LENGTH = 14;

  private final AppContractRepositoryPort contractRepo;
  private final PlatformUserRepositoryPort platformUserRepo;
  private final PlatformUserRoleRepositoryPort platformUserRoleRepo;
  private final ContractorRepositoryPort contractorRepo;
  private final ContractorUserRepositoryPort contractorUserRepo;
  private final CredentialEncoderPort credentialEncoder;
  private final EmailNotificationPort emailNotification;
  private final boolean mockPaymentEnabled;
  private final SecureRandom secureRandom = new SecureRandom();

  public MockApprovePaymentUseCase(
      AppContractRepositoryPort contractRepo,
      PlatformUserRepositoryPort platformUserRepo,
      PlatformUserRoleRepositoryPort platformUserRoleRepo,
      ContractorRepositoryPort contractorRepo,
      ContractorUserRepositoryPort contractorUserRepo,
      CredentialEncoderPort credentialEncoder,
      EmailNotificationPort emailNotification,
      boolean mockPaymentEnabled) {
    this.contractRepo = contractRepo;
    this.platformUserRepo = platformUserRepo;
    this.platformUserRoleRepo = platformUserRoleRepo;
    this.contractorRepo = contractorRepo;
    this.contractorUserRepo = contractorUserRepo;
    this.credentialEncoder = credentialEncoder;
    this.emailNotification = emailNotification;
    this.mockPaymentEnabled = mockPaymentEnabled;
  }

  public boolean isMockEnabled() {
    return mockPaymentEnabled;
  }

  public AppContractResult execute(UUID contractId) {
    if (!mockPaymentEnabled) {
      throw new UnsupportedOperationException("Mock payment is disabled in this environment");
    }

    final var currentContract = contractRepo.findById(contractId)
        .orElseThrow(() -> new ContractNotFoundException(contractId));

    if (ContractStatus.ACTIVE.equals(currentContract.getStatus())
        || ContractStatus.CANCELLED.equals(currentContract.getStatus())) {
      throw new ContractInvalidStateException(contractId, currentContract.getStatus());
    }
    if (ContractStatus.READY_TO_ACTIVATE.equals(currentContract.getStatus())) {
      return new AppContractResult(currentContract, null);
    }
    if (!ContractStatus.PENDING_PAYMENT.equals(currentContract.getStatus())) {
      throw new ContractInvalidStateException(contractId, currentContract.getStatus(), "payment can only be approved after email verification");
    }

    OffsetDateTime now = OffsetDateTime.now();
    PlatformUser platformUser = platformUserRepo.findByEmail(EmailAddress.of(currentContract.getContractorEmail()))
        .orElseGet(() -> provisionPlatformUser(currentContract));
    UUID platformUserId = platformUser.getId().value();

    if (!platformUserRoleRepo.hasRole(platformUserId, "keygo_user")) {
      platformUserRoleRepo.assign(platformUserId, "keygo_user");
    }
    if (!platformUserRoleRepo.hasRole(platformUserId, "keygo_tenant_admin")) {
      platformUserRoleRepo.assign(platformUserId, "keygo_tenant_admin");
    }

    Contractor contractor = contractorRepo.findByPlatformUserId(platformUserId)
        .orElseGet(() -> contractorRepo.save(Contractor.builder()
            .primaryContactPlatformUserId(platformUserId)
            .type(resolveContractorType(currentContract))
            .displayName(resolveContractorDisplayName(currentContract))
            .legalName(normalizeBlank(currentContract.getCompanyName()))
            .taxId(normalizeBlank(currentContract.getCompanyTaxId()))
            .billingEmail(currentContract.getContractorEmail())
            .status(ContractorStatus.PENDING)
            .build()));
    ensureOwnerMembership(contractor.getId(), platformUserId);

    var contract = currentContract;
    contract.linkContractor(contractor.getId(), now);
    contract.markPaymentApproved(now);
    contract = contractRepo.save(contract);
    return new AppContractResult(contract, null);
  }

  private PlatformUser provisionPlatformUser(io.cmartinezs.keygo.domain.billing.contracting.model.AppContract contract) {
    String rawPassword = generateTemporaryPassword();
    String hashedPwd = credentialEncoder.encode(rawPassword);

    PlatformUser saved = platformUserRepo.save(
        PlatformUser.builder()
            .username(Username.of(contract.generateUsername()))
            .email(EmailAddress.of(contract.getContractorEmail()))
            .passwordHash(PasswordHash.of(hashedPwd))
            .status(UserStatus.RESET_PASSWORD)
            .firstName(contract.getContractorFirstName())
            .lastName(contract.getContractorLastName())
            .build());

    emailNotification.sendEmail(
        EmailNotificationPort.TYPE_TEMPORARY_PASSWORD,
        contract.getContractorEmail(),
        contract.generateUsername(),
        Map.of("userUsername", contract.generateUsername(),
            "userFirstName", contract.getContractorFirstName() != null ? contract.getContractorFirstName() : "",
            "userLastName", contract.getContractorLastName() != null ? contract.getContractorLastName() : "",
            "temporaryPassword", rawPassword));

    return saved;
  }

  String generateTemporaryPassword() {
    String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String lower = "abcdefghijklmnopqrstuvwxyz";
    String digits = "0123456789";
    String special = "!@#$%&*";

    char[] pwd = new char[PASSWORD_LENGTH];
    pwd[0] = upper.charAt(secureRandom.nextInt(upper.length()));
    pwd[1] = lower.charAt(secureRandom.nextInt(lower.length()));
    pwd[2] = digits.charAt(secureRandom.nextInt(digits.length()));
    pwd[3] = special.charAt(secureRandom.nextInt(special.length()));

    for (int i = 4; i < PASSWORD_LENGTH; i++) {
      pwd[i] = PASSWORD_CHARS.charAt(secureRandom.nextInt(PASSWORD_CHARS.length()));
    }

    for (int i = PASSWORD_LENGTH - 1; i > 0; i--) {
      int j = secureRandom.nextInt(i + 1);
      char tmp = pwd[i];
      pwd[i] = pwd[j];
      pwd[j] = tmp;
    }

    return new String(pwd);
  }

  private ContractorType resolveContractorType(io.cmartinezs.keygo.domain.billing.contracting.model.AppContract contract) {
    return normalizeBlank(contract.getCompanyName()) != null ? ContractorType.COMPANY : ContractorType.PERSON;
  }

  private String resolveContractorDisplayName(io.cmartinezs.keygo.domain.billing.contracting.model.AppContract contract) {
    String companyName = normalizeBlank(contract.getCompanyName());
    if (companyName != null) {
      return companyName;
    }

    String fullName = ((contract.getContractorFirstName() != null ? contract.getContractorFirstName().trim() : "")
        + " "
        + (contract.getContractorLastName() != null ? contract.getContractorLastName().trim() : "")).trim();
    return fullName.isBlank() ? contract.getContractorEmail() : fullName;
  }

  private String normalizeBlank(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private void ensureOwnerMembership(UUID contractorId, UUID platformUserId) {
    if (!contractorUserRepo.hasRole(contractorId, platformUserId, ContractorUserRole.OWNER)) {
      contractorUserRepo.assign(contractorId, platformUserId, ContractorUserRole.OWNER);
    }
  }
}

