package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.contracting.command.CreateAppContractCommand;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.EmailVerificationRepositoryPort;
import io.cmartinezs.keygo.app.user.port.PasswordHasherPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.EmailVerification;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case: create a new app contract (beginning the contracting flow).
 * Handles TENANT (B2B) and TENANT_USER (B2C) branches.
 * @author cmartinezs
 * @version 1.0
 */
public class CreateAppContractUseCase {

  private final AppContractRepositoryPort contractRepo;
  private final AppPlanVersionRepositoryPort versionRepo;
  private final UserRepositoryPort userRepo;
  private final PasswordHasherPort passwordHasher;
  private final EmailVerificationRepositoryPort emailVerificationRepo;
  private final EmailNotificationPort emailNotification;
  private final int contractExpiryHours;

  public CreateAppContractUseCase(
      AppContractRepositoryPort contractRepo,
      AppPlanVersionRepositoryPort versionRepo,
      UserRepositoryPort userRepo,
      PasswordHasherPort passwordHasher,
      EmailVerificationRepositoryPort emailVerificationRepo,
      EmailNotificationPort emailNotification,
      int contractExpiryHours) {
    this.contractRepo = contractRepo;
    this.versionRepo = versionRepo;
    this.userRepo = userRepo;
    this.passwordHasher = passwordHasher;
    this.emailVerificationRepo = emailVerificationRepo;
    this.emailNotification = emailNotification;
    this.contractExpiryHours = contractExpiryHours;
  }

  public AppContractResult execute(CreateAppContractCommand cmd) {
    // Validate plan version exists
    AppPlanVersion planVersion = versionRepo.findById(cmd.planVersionId())
        .orElseThrow(() -> new IllegalArgumentException("Plan version not found: " + cmd.planVersionId()));

    // Validate plan subscriber type matches contract subscriber type
    // (fetched via the plan linked to this version — handled in controller/adapter)

    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime expiresAt = now.plusHours(contractExpiryHours);

    ContractStatus initialStatus;

    if (SubscriberType.TENANT.equals(cmd.subscriberType())) {
      // B2B branch
      if (cmd.companySlug() == null || cmd.companySlug().isBlank()) {
        throw new IllegalArgumentException("companySlug is required for TENANT contracts");
      }
      if (contractRepo.existsByClientAppIdAndCompanySlug(cmd.clientAppId(), cmd.companySlug())) {
        throw new IllegalArgumentException("company_slug already used for this app: " + cmd.companySlug());
      }
      initialStatus = ContractStatus.PENDING_EMAIL_VERIFICATION;

    } else {
      // B2C branch — check if TenantUser exists
      // For B2C we need to look up the tenant via the clientApp (simplified: use clientAppId scope)
      // In practice the controller resolves tenantId from the path
      // We create the user if they don't exist
      initialStatus = resolveB2CInitialStatus(cmd, now);
    }

    AppContract contract = AppContract.builder()
        .clientAppId(cmd.clientAppId())
        .selectedPlanVersionId(cmd.planVersionId())
        .billingPeriod(cmd.billingPeriod() != null ? cmd.billingPeriod().name() : "MONTHLY")
        .subscriberType(cmd.subscriberType())
        .status(initialStatus)
        .contractorEmail(cmd.contractorEmail())
        .contractorFirstName(cmd.contractorFirstName())
        .contractorLastName(cmd.contractorLastName())
        .companyName(cmd.companyName())
        .companySlug(cmd.companySlug())
        .companyTaxId(cmd.companyTaxId())
        .companyAddress(cmd.companyAddress())
        .expiresAt(expiresAt)
        .createdAt(now)
        .updatedAt(now)
        .build();

    contract = contractRepo.save(contract);
    return new AppContractResult(contract, null);
  }

  private ContractStatus resolveB2CInitialStatus(CreateAppContractCommand cmd, OffsetDateTime now) {
    // Simplified: controller must pass tenantId in a more complete version.
    // For MVP, PENDING_EMAIL_VERIFICATION if no contextual tenantId.
    return ContractStatus.PENDING_EMAIL_VERIFICATION;
  }
}

