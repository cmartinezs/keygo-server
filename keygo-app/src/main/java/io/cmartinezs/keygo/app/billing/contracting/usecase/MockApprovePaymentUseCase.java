package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.contracting.exception.ContractInvalidStateException;
import io.cmartinezs.keygo.app.billing.contracting.exception.ContractNotFoundException;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;
import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.billing.contractor.port.ContractorUserRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.membership.command.AssignPlatformRoleCommand;
import io.cmartinezs.keygo.app.membership.usecase.AssignPlatformRoleUseCase;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.domain.billing.contractor.model.Contractor;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorStatus;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorType;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorUserRole;
import io.cmartinezs.keygo.domain.membership.model.PlatformRoleCode;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;
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

  private static final List<String> CONTRACTOR_ROLES = List.of(
      PlatformRoleCode.KEYGO_ACCOUNT_ADMIN.code(),
      PlatformRoleCode.KEYGO_USER.code()
  );

  private final AppContractRepositoryPort contractRepo;
  private final PlatformUserRepositoryPort platformUserRepo;
  private final AssignPlatformRoleUseCase assignPlatformRoleUseCase;
  private final ContractorRepositoryPort contractorRepo;
  private final ContractorUserRepositoryPort contractorUserRepo;
  private final CredentialEncoderPort credentialEncoder;
  private final EmailNotificationPort emailNotification;
  private final boolean mockPaymentEnabled;
  private final SecureRandom secureRandom = new SecureRandom();

  public MockApprovePaymentUseCase(
      AppContractRepositoryPort contractRepo,
      PlatformUserRepositoryPort platformUserRepo,
      AssignPlatformRoleUseCase assignPlatformRoleUseCase,
      ContractorRepositoryPort contractorRepo,
      ContractorUserRepositoryPort contractorUserRepo,
      CredentialEncoderPort credentialEncoder,
      EmailNotificationPort emailNotification,
      boolean mockPaymentEnabled) {
    this.contractRepo = contractRepo;
    this.platformUserRepo = platformUserRepo;
    this.assignPlatformRoleUseCase = assignPlatformRoleUseCase;
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

    // (a) Resolver/crear usuario — rawPassword queda en el coordinador para enviarlo al final
    String rawPassword = null;
    PlatformUser platformUser;
    var optUser = platformUserRepo.findByEmail(EmailAddress.of(currentContract.getContractorEmail()));
    if (optUser.isEmpty()) {
      rawPassword = generateTemporaryPassword();
      platformUser = createPlatformUser(currentContract, credentialEncoder.encode(rawPassword));
    } else {
      platformUser = optUser.get();
    }

    // (b) Asignar roles de plataforma
    assignPlatformRoleUseCase.execute(
        new AssignPlatformRoleCommand(platformUser.getId().value(), CONTRACTOR_ROLES));

    // (c) Resolver/crear contractor
    Contractor contractor = resolveOrCreateContractor(currentContract, platformUser.getId().value());

    // (d) Asegurar membresía OWNER
    ensureOwnerMembership(contractor.getId(), platformUser.getId().value());

    // (e) Vincular contractor al contrato y marcar pago aprobado
    applyPaymentApproval(currentContract, contractor.getId(), now);

    // (f) Persistir contrato
    var contract = contractRepo.save(currentContract);

    // Email enviado al final: solo si se creó un usuario nuevo y todo lo anterior tuvo éxito
    if (rawPassword != null) {
      sendWelcomeEmail(currentContract, rawPassword);
    }

    return new AppContractResult(contract, null);
  }

  private PlatformUser createPlatformUser(AppContract contract, String hashedPassword) {
    return platformUserRepo.save(
        PlatformUser.builder()
            .username(Username.of(contract.generateUsername()))
            .email(EmailAddress.of(contract.getContractorEmail()))
            .passwordHash(PasswordHash.of(hashedPassword))
            .status(UserStatus.RESET_PASSWORD)
            .firstName(contract.getContractorFirstName())
            .lastName(contract.getContractorLastName())
            .build());
  }

  private Contractor resolveOrCreateContractor(AppContract contract, UUID platformUserId) {
    return contractorRepo.findByPlatformUserId(platformUserId)
        .orElseGet(() -> contractorRepo.save(Contractor.builder()
            .primaryContactPlatformUserId(platformUserId)
            .type(resolveContractorType(contract))
            .displayName(resolveContractorDisplayName(contract))
            .legalName(normalizeBlank(contract.getCompanyName()))
            .taxId(normalizeBlank(contract.getCompanyTaxId()))
            .billingEmail(contract.getContractorEmail())
            .status(ContractorStatus.PENDING)
            .build()));
  }

  private void applyPaymentApproval(AppContract contract,
      UUID contractorId, OffsetDateTime now) {
    contract.linkContractor(contractorId, now);
    contract.markPaymentApproved(now);
  }

  private void sendWelcomeEmail(AppContract contract, String rawPassword) {
    emailNotification.sendEmail(
        EmailNotificationPort.TYPE_TEMPORARY_PASSWORD,
        contract.getContractorEmail(),
        contract.generateUsername(),
        Map.of("userUsername", contract.generateUsername(),
            "userFirstName", contract.getContractorFirstName() != null ? contract.getContractorFirstName() : "",
            "userLastName", contract.getContractorLastName() != null ? contract.getContractorLastName() : "",
            "temporaryPassword", rawPassword));
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

