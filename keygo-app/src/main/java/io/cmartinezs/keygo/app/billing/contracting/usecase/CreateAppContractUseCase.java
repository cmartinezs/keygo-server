package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.contracting.command.CreateAppContractCommand;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;

import java.security.SecureRandom;
import java.time.OffsetDateTime;

/**
 * Use case: create a new app contract (beginning the contracting flow).
 * Generates a verification code and sends it to the contractor's email.
 * Handles TENANT (B2B) and TENANT_USER (B2C) branches.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class CreateAppContractUseCase {

  private static final SecureRandom RANDOM = new SecureRandom();

  private final AppContractRepositoryPort contractRepo;
  private final AppPlanVersionRepositoryPort versionRepo;
  private final EmailNotificationPort emailNotification;
  private final int contractExpiryHours;
  private final int verificationCodeExpiryMinutes;

  public CreateAppContractUseCase(
      AppContractRepositoryPort contractRepo,
      AppPlanVersionRepositoryPort versionRepo,
      EmailNotificationPort emailNotification,
      int contractExpiryHours,
      int verificationCodeExpiryMinutes) {
    this.contractRepo = contractRepo;
    this.versionRepo = versionRepo;
    this.emailNotification = emailNotification;
    this.contractExpiryHours = contractExpiryHours;
    this.verificationCodeExpiryMinutes = verificationCodeExpiryMinutes;
  }

  public AppContractResult execute(CreateAppContractCommand cmd) {
    // Validate plan version exists
    AppPlanVersion planVersion = versionRepo.findById(cmd.planVersionId())
        .orElseThrow(() -> new IllegalArgumentException("Plan version not found: " + cmd.planVersionId()));

    // For B2B: validate companySlug is present and not already in use
    if (SubscriberType.TENANT.equals(cmd.subscriberType())) {
      if (cmd.companySlug() == null || cmd.companySlug().isBlank()) {
        throw new IllegalArgumentException("companySlug es requerido para contratos de tipo TENANT");
      }
      if (contractRepo.existsByClientAppIdAndCompanySlug(cmd.clientAppId(), cmd.companySlug())) {
        throw new IllegalArgumentException("El company_slug ya está en uso para esta app: " + cmd.companySlug());
      }
    }

    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime expiresAt = now.plusHours(contractExpiryHours);
    OffsetDateTime codeExpiresAt = now.plusMinutes(verificationCodeExpiryMinutes);

    // Generate a 6-digit numeric verification code (SecureRandom for safety)
    String verificationCode = String.format("%06d", RANDOM.nextInt(1_000_000));

    AppContract contract = AppContract.builder()
        .clientAppId(cmd.clientAppId())
        .selectedPlanVersionId(cmd.planVersionId())
        .billingPeriod(cmd.billingPeriod() != null ? cmd.billingPeriod().name() : "MONTHLY")
        .subscriberType(cmd.subscriberType())
        .status(ContractStatus.PENDING_EMAIL_VERIFICATION)
        .contractorEmail(cmd.contractorEmail())
        .contractorFirstName(cmd.contractorFirstName())
        .contractorLastName(cmd.contractorLastName())
        .companyName(cmd.companyName())
        .companySlug(cmd.companySlug())
        .companyTaxId(cmd.companyTaxId())
        .companyAddress(cmd.companyAddress())
        .verificationCode(verificationCode)
        .verificationCodeExpiresAt(codeExpiresAt)
        .expiresAt(expiresAt)
        .createdAt(now)
        .updatedAt(now)
        .build();

    contract = contractRepo.save(contract);

    // Enviar email con el código de verificación
    String recipientName = contract.getContractorFirstName() + " " + contract.getContractorLastName();
    emailNotification.sendVerificationEmail(contract.getContractorEmail(), recipientName, verificationCode);

    return new AppContractResult(contract, null);
  }
}
