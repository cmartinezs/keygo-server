package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.command.CreateAppContractCommand;
import io.cmartinezs.keygo.app.billing.contracting.exception.ContractorEmailAlreadyExistsException;
import io.cmartinezs.keygo.app.billing.contracting.exception.PlanVersionNotFoundException;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;
import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Use case: create a new app contract (beginning of the contracting flow) — billing model v2.
 * Generates a verification code and sends it to the contractor's email.
 * No B2B/B2C branch distinction here; that was removed in model v2.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class CreateAppContractUseCase {

  private static final SecureRandom RANDOM = new SecureRandom();

  private final AppContractRepositoryPort contractRepo;
  private final AppPlanVersionRepositoryPort versionRepo;
  private final ClientAppRepositoryPort clientAppRepo;
  private final ContractorRepositoryPort contractorRepo;
  private final EmailNotificationPort emailNotification;
  private final int contractExpiryHours;
  private final int verificationCodeExpiryMinutes;

  public CreateAppContractUseCase(
      AppContractRepositoryPort contractRepo,
      AppPlanVersionRepositoryPort versionRepo,
      ClientAppRepositoryPort clientAppRepo,
      ContractorRepositoryPort contractorRepo,
      EmailNotificationPort emailNotification,
      int contractExpiryHours,
      int verificationCodeExpiryMinutes) {
    this.contractRepo = contractRepo;
    this.versionRepo = versionRepo;
    this.clientAppRepo = clientAppRepo;
    this.contractorRepo = contractorRepo;
    this.emailNotification = emailNotification;
    this.contractExpiryHours = contractExpiryHours;
    this.verificationCodeExpiryMinutes = verificationCodeExpiryMinutes;
  }

  public AppContractResult execute(CreateAppContractCommand cmd) {
    versionRepo.findById(cmd.planVersionId())
        .orElseThrow(() -> new PlanVersionNotFoundException(cmd.planVersionId()));

    // Validate clientAppId exists and resolve provider tenantId
    var clientApp = clientAppRepo.findById(ClientAppId.of(cmd.clientAppId()))
        .orElseThrow(() -> new IllegalArgumentException("Client app not found: " + cmd.clientAppId()));
    UUID providerTenantId = clientApp.getTenantId().value();

    // Check if email already registered as contractor in provider tenant
    contractorRepo.findByTenantUserEmail(providerTenantId, cmd.contractorEmail())
        .ifPresent(existingContractor -> {
          throw new ContractorEmailAlreadyExistsException(cmd.contractorEmail());
        });

    OffsetDateTime now = OffsetDateTime.now();
    String verificationCode = String.format("%06d", RANDOM.nextInt(1_000_000));

    AppContract contract = AppContract.builder()
        .clientAppId(cmd.clientAppId())
        .selectedPlanVersionId(cmd.planVersionId())
        .billingPeriod(cmd.billingPeriod() != null ? cmd.billingPeriod().name() : "MONTHLY")
        .status(ContractStatus.PENDING_EMAIL_VERIFICATION)
        .contractorEmail(cmd.contractorEmail())
        .contractorFirstName(cmd.contractorFirstName())
        .contractorLastName(cmd.contractorLastName())
        .companyName(cmd.companyName())
        .companyTaxId(cmd.companyTaxId())
        .companyAddress(cmd.companyAddress())
        .verificationCode(verificationCode)
        .verificationCodeExpiresAt(now.plusMinutes(verificationCodeExpiryMinutes))
        .expiresAt(now.plusHours(contractExpiryHours))
        .createdAt(now)
        .updatedAt(now)
        .build();

    contract = contractRepo.save(contract);

    String recipientName = contract.getContractorFirstName() + " " + contract.getContractorLastName();
    emailNotification.sendContractVerificationEmail(
        contract.getContractorEmail(), recipientName, verificationCode, contract.getId());

    return new AppContractResult(contract, null);
  }
}
