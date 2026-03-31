package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanBillingOptionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;
import io.cmartinezs.keygo.app.billing.invoice.port.InvoiceRepositoryPort;
import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanBillingOption;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import io.cmartinezs.keygo.domain.billing.invoice.model.Invoice;
import io.cmartinezs.keygo.domain.billing.invoice.model.InvoiceStatus;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Use case: activate a contract — creates subscription + invoice — billing model v2.
 * No tenant/user creation here; the contractor creates tenants separately after activation.
 * @author cmartinezs
 * @version 1.0
 */
public class ActivateAppContractUseCase {

  private final AppContractRepositoryPort contractRepo;
  private final AppPlanVersionRepositoryPort versionRepo;
  private final AppPlanBillingOptionRepositoryPort billingOptionRepo;
  private final AppSubscriptionRepositoryPort subscriptionRepo;
  private final InvoiceRepositoryPort invoiceRepo;

  public ActivateAppContractUseCase(
      AppContractRepositoryPort contractRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppPlanBillingOptionRepositoryPort billingOptionRepo,
      AppSubscriptionRepositoryPort subscriptionRepo,
      InvoiceRepositoryPort invoiceRepo) {
    this.contractRepo = contractRepo;
    this.versionRepo = versionRepo;
    this.billingOptionRepo = billingOptionRepo;
    this.subscriptionRepo = subscriptionRepo;
    this.invoiceRepo = invoiceRepo;
  }

  public AppContractResult execute(UUID contractId) {
    AppContract contract = contractRepo.findById(contractId)
        .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

    // Idempotent — already active
    if (ContractStatus.ACTIVE.equals(contract.getStatus())) {
      var existingSub = subscriptionRepo
          .findByClientAppIdAndContractorId(contract.getClientAppId(), contract.getContractorId())
          .orElse(null);
      return new AppContractResult(contract, existingSub);
    }

    if (!ContractStatus.READY_TO_ACTIVATE.equals(contract.getStatus())) {
      throw new IllegalStateException("Contract is not ready to activate. Current status: " + contract.getStatus());
    }
    if (!contract.isPaymentVerified()) {
      throw new IllegalStateException("Payment has not been verified for contract: " + contractId);
    }
    if (contract.getContractorId() == null) {
      throw new IllegalStateException("Contract has no linked Contractor: " + contractId);
    }

    AppPlanVersion planVersion = versionRepo.findById(contract.getSelectedPlanVersionId())
        .orElseThrow(() -> new IllegalStateException("Plan version not found"));

    BillingPeriod chosenPeriod = contract.getBillingPeriod() != null
        ? BillingPeriod.valueOf(contract.getBillingPeriod())
        : null;

    AppPlanBillingOption billingOption = chosenPeriod != null
        ? billingOptionRepo.findByAppPlanVersionIdAndBillingPeriod(planVersion.getId(), chosenPeriod).orElse(null)
        : null;

    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime periodEnd = computePeriodEnd(chosenPeriod, now);

    // Create subscription for the Contractor
    AppSubscription subscription = AppSubscription.builder()
        .clientAppId(contract.getClientAppId())
        .appPlanVersionId(planVersion.getId())
        .contractId(contract.getId())
        .contractorId(contract.getContractorId())
        .status(SubscriptionStatus.ACTIVE)
        .currentPeriodStart(now)
        .currentPeriodEnd(periodEnd)
        .cancelAtPeriodEnd(false)
        .autoRenew(true)
        .nextBillingAt(periodEnd)
        .createdAt(now)
        .updatedAt(now)
        .build();
    subscription = subscriptionRepo.save(subscription);

    // Generate first invoice
    generateInvoice(subscription, contract, planVersion, billingOption, now);

    // Mark contract as ACTIVE
    contract.activate(now);
    contractRepo.save(contract);

    return new AppContractResult(contract, subscription);
  }

  private OffsetDateTime computePeriodEnd(BillingPeriod billingPeriod, OffsetDateTime start) {
    if (billingPeriod == null) return start.plusYears(100);
    return switch (billingPeriod) {
      case YEARLY -> start.plusYears(1);
      case ONE_TIME -> start.plusYears(100);
      default -> start.plusMonths(1);
    };
  }

  private void generateInvoice(
      AppSubscription sub,
      AppContract contract,
      AppPlanVersion planVersion,
      AppPlanBillingOption billingOption,
      OffsetDateTime now) {

    String invoiceNumber = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    LocalDate today = now.toLocalDate();
    BigDecimal basePrice = billingOption != null ? billingOption.getBasePrice() : BigDecimal.ZERO;

    Invoice invoice = Invoice.builder()
        .subscriptionId(sub.getId())
        .invoiceNumber(invoiceNumber)
        .status(InvoiceStatus.ISSUED)
        .issueDate(today)
        .dueDate(today.plusDays(30))
        .periodStart(today)
        .periodEnd(sub.getCurrentPeriodEnd().toLocalDate())
        .currency(planVersion.getCurrency())
        .subtotal(basePrice)
        .taxAmount(BigDecimal.ZERO)
        .total(basePrice)
        .billingNameSnapshot(contract.getContractorFirstName() + " " + contract.getContractorLastName())
        .billingTaxIdSnapshot(contract.getCompanyTaxId())
        .billingAddressSnapshot(contract.getCompanyAddress())
        .planVersionSnapshot(planVersion.getVersion())
        .createdAt(now)
        .build();

    invoiceRepo.save(invoice);
  }
}
