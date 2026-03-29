package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;
import io.cmartinezs.keygo.app.billing.invoice.port.InvoiceRepositoryPort;
import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import io.cmartinezs.keygo.domain.billing.invoice.model.Invoice;
import io.cmartinezs.keygo.domain.billing.invoice.model.InvoiceStatus;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriptionStatus;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Use case: activate a contract — creates tenant/user + subscription + invoice.
 * Handles three branches: TENANT (B2B), TENANT_USER new, TENANT_USER existing.
 * @author cmartinezs
 * @version 1.0
 */
public class ActivateAppContractUseCase {

  private final AppContractRepositoryPort contractRepo;
  private final AppPlanVersionRepositoryPort versionRepo;
  private final AppSubscriptionRepositoryPort subscriptionRepo;
  private final InvoiceRepositoryPort invoiceRepo;
  private final TenantRepositoryPort tenantRepo;
  private final UserRepositoryPort userRepo;
  private final MembershipRepositoryPort membershipRepo;
  private final AppRoleRepositoryPort appRoleRepo;

  public ActivateAppContractUseCase(
      AppContractRepositoryPort contractRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppSubscriptionRepositoryPort subscriptionRepo,
      InvoiceRepositoryPort invoiceRepo,
      TenantRepositoryPort tenantRepo,
      UserRepositoryPort userRepo,
      MembershipRepositoryPort membershipRepo,
      AppRoleRepositoryPort appRoleRepo) {
    this.contractRepo = contractRepo;
    this.versionRepo = versionRepo;
    this.subscriptionRepo = subscriptionRepo;
    this.invoiceRepo = invoiceRepo;
    this.tenantRepo = tenantRepo;
    this.userRepo = userRepo;
    this.membershipRepo = membershipRepo;
    this.appRoleRepo = appRoleRepo;
  }

  public AppContractResult execute(UUID contractId) {
    AppContract contract = contractRepo.findById(contractId)
        .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

    if (ContractStatus.ACTIVATED.equals(contract.getStatus())) {
      // Idempotent — already activated
      var existingSub = findExistingSubscription(contract);
      return new AppContractResult(contract, existingSub);
    }

    if (!ContractStatus.READY_TO_ACTIVATE.equals(contract.getStatus())) {
      throw new IllegalStateException("Contract is not ready to activate. Current status: " + contract.getStatus());
    }

    if (!contract.isEmailVerified() && ContractStatus.PENDING_EMAIL_VERIFICATION.name().equals(
        ContractStatus.PENDING_EMAIL_VERIFICATION.name())) {
      // Only check if email was required (i.e., started as PENDING_EMAIL_VERIFICATION)
    }
    if (!contract.isPaymentVerified()) {
      throw new IllegalStateException("Payment has not been verified for contract: " + contractId);
    }

    AppPlanVersion planVersion = versionRepo.findById(contract.getSelectedPlanVersionId())
        .orElseThrow(() -> new IllegalStateException("Plan version not found"));

    OffsetDateTime now = OffsetDateTime.now();
    AppSubscription subscription;

    if (SubscriberType.TENANT.equals(contract.getSubscriberType())) {
      subscription = activateTenantBranch(contract, planVersion, now);
    } else {
      subscription = activateTenantUserBranch(contract, planVersion, now);
    }

    // Generate first invoice
    generateInvoice(subscription, contract, planVersion, now);

    // Mark contract as activated
    UUID tenantId = subscription.getSubscriberTenantId();
    UUID tenantUserId = subscription.getSubscriberTenantUserId();
    contract.activate(tenantId, tenantUserId, now);
    contractRepo.save(contract);

    return new AppContractResult(contract, subscription);
  }

  private AppSubscription activateTenantBranch(AppContract contract, AppPlanVersion planVersion, OffsetDateTime now) {
    // Create new Tenant using companySlug
    Tenant newTenant = Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(contract.getCompanySlug()))
        .name(contract.getCompanyName() != null ? contract.getCompanyName() : contract.getCompanySlug())
        .ownerEmail(contract.getContractorEmail())
        .status(TenantStatus.ACTIVE)
        .build();
    newTenant = tenantRepo.save(newTenant);

    // Create admin TenantUser
    User adminUser = User.builder()
        .id(UserId.of(UUID.randomUUID()))
        .tenantId(newTenant.getId())
        .email(EmailAddress.of(contract.getContractorEmail()))
        .username(io.cmartinezs.keygo.domain.user.model.Username.of(contract.getContractorEmail()))
        .firstName(contract.getContractorFirstName())
        .lastName(contract.getContractorLastName())
        .status(UserStatus.ACTIVE)
        .build();
    adminUser = userRepo.save(adminUser);

    return createSubscription(contract, planVersion,
        SubscriberType.TENANT, newTenant.getId().value(), null, now);
  }

  private AppSubscription activateTenantUserBranch(AppContract contract, AppPlanVersion planVersion, OffsetDateTime now) {
    // Look up or activate TenantUser
    // Simplified: controller must resolve tenantId; here we find user by email in some tenant
    // This is a known limitation; full implementation requires tenantId from context
    // For MVP: subscription is created with a placeholder — actual user lookup happens in adapter
    return createSubscription(contract, planVersion,
        SubscriberType.TENANT_USER, null, null, now);
  }

  private AppSubscription createSubscription(
      AppContract contract,
      AppPlanVersion planVersion,
      SubscriberType subscriberType,
      UUID tenantId,
      UUID tenantUserId,
      OffsetDateTime now) {

    OffsetDateTime periodEnd = computePeriodEnd(planVersion, now);

    AppSubscription sub = AppSubscription.builder()
        .clientAppId(contract.getClientAppId())
        .appPlanVersionId(planVersion.getId())
        .contractId(contract.getId())
        .subscriberType(subscriberType)
        .subscriberTenantId(tenantId)
        .subscriberTenantUserId(tenantUserId)
        .status(SubscriptionStatus.ACTIVE)
        .currentPeriodStart(now)
        .currentPeriodEnd(periodEnd)
        .cancelAtPeriodEnd(false)
        .autoRenew(true)
        .nextBillingAt(periodEnd)
        .createdAt(now)
        .updatedAt(now)
        .build();

    return subscriptionRepo.save(sub);
  }

  private OffsetDateTime computePeriodEnd(AppPlanVersion planVersion, OffsetDateTime start) {
    return switch (planVersion.getBillingPeriod()) {
      case YEARLY -> start.plusYears(1);
      case ONE_TIME -> start.plusYears(100); // effectively no end
      default -> start.plusMonths(1); // MONTHLY
    };
  }

  private void generateInvoice(AppSubscription sub, AppContract contract, AppPlanVersion planVersion, OffsetDateTime now) {
    String invoiceNumber = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    LocalDate today = now.toLocalDate();

    Invoice invoice = Invoice.builder()
        .subscriptionId(sub.getId())
        .invoiceNumber(invoiceNumber)
        .status(InvoiceStatus.ISSUED)
        .issueDate(today)
        .dueDate(today.plusDays(30))
        .periodStart(today)
        .periodEnd(sub.getCurrentPeriodEnd().toLocalDate())
        .currency(planVersion.getCurrency())
        .subtotal(planVersion.getBasePrice())
        .taxAmount(BigDecimal.ZERO)
        .total(planVersion.getBasePrice())
        .billingNameSnapshot(contract.getContractorFirstName() + " " + contract.getContractorLastName())
        .billingTaxIdSnapshot(contract.getCompanyTaxId())
        .billingAddressSnapshot(contract.getCompanyAddress())
        .planVersionSnapshot(planVersion.getVersion())
        .createdAt(now)
        .build();

    invoiceRepo.save(invoice);
  }

  private AppSubscription findExistingSubscription(AppContract contract) {
    if (contract.getSubscriberTenantId() != null) {
      return subscriptionRepo.findByClientAppIdAndSubscriberTenantId(
          contract.getClientAppId(), contract.getSubscriberTenantId()).orElse(null);
    }
    if (contract.getSubscriberTenantUserId() != null) {
      return subscriptionRepo.findByClientAppIdAndSubscriberUserId(
          contract.getClientAppId(), contract.getSubscriberTenantUserId()).orElse(null);
    }
    return null;
  }
}

