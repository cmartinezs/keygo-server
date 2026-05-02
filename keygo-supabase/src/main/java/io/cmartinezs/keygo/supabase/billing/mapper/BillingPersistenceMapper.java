package io.cmartinezs.keygo.supabase.billing.mapper;

import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanBillingOption;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.domain.billing.invoice.model.Invoice;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;
import io.cmartinezs.keygo.supabase.billing.entity.*;

/**
 * Static mapper between billing domain models and JPA entities.
 * @author cmartinezs
 * @version 1.0
 */
public final class BillingPersistenceMapper {

  private BillingPersistenceMapper() {}

  // ── AppPlan ─────────────────────────────────────────────────────────────

  public static AppPlan toDomain(AppPlanEntity e) {
    return AppPlan.builder()
        .id(e.getId())
        .clientAppId(e.getClientApp() != null ? e.getClientApp().getId() : null)
        .code(e.getCode())
        .name(e.getName())
        .description(e.getDescription())
        .status(e.getStatus())
        .isPublic(e.isPublic())
        .build();
  }

  // ── AppPlanVersion ───────────────────────────────────────────────────────

  public static AppPlanVersion toDomain(AppPlanVersionEntity e) {
    return AppPlanVersion.builder()
        .id(e.getId())
        .appPlanId(e.getAppPlan().getId())
        .version(e.getVersion())
        .currency(e.getCurrency())
        .setupFee(e.getSetupFee())
        .trialDays(e.getTrialDays())
        .effectiveFrom(e.getEffectiveFrom())
        .effectiveTo(e.getEffectiveTo())
        .status(e.getStatus())
        .build();
  }

  // ── AppPlanBillingOption ─────────────────────────────────────────────────

  public static AppPlanBillingOption toDomain(AppPlanBillingOptionEntity e) {
    return AppPlanBillingOption.builder()
        .id(e.getId())
        .appPlanVersionId(e.getAppPlanVersion().getId())
        .billingPeriod(e.getBillingPeriod())
        .basePrice(e.getBasePrice())
        .discountPct(e.getDiscountPct())
        .isDefault(e.isDefault())
        .build();
  }

  // ── AppPlanEntitlement ───────────────────────────────────────────────────

  public static AppPlanEntitlement toDomain(AppPlanEntitlementEntity e) {
    return AppPlanEntitlement.builder()
        .id(e.getId())
        .appPlanVersionId(e.getAppPlanVersion().getId())
        .metricCode(e.getMetricCode())
        .metricType(e.getMetricType())
        .limitValue(e.getLimitValue())
        .periodType(e.getPeriodType())
        .enforcementMode(e.getEnforcementMode())
        .isEnabled(e.isEnabled())
        .build();
  }

  // ── AppContract ──────────────────────────────────────────────────────────

  public static AppContract toDomain(AppContractEntity e) {
    String[] contactName = resolveContactName(e);
    return AppContract.builder()
        .id(e.getId())
        .clientAppId(e.getClientApp() != null ? e.getClientApp().getId() : null)
        .selectedPlanVersionId(e.getSelectedPlanVersion().getId())
        .billingPeriod(e.getBillingPeriod())
        .status(e.getStatus())
        .contractorEmail(e.getContractorEmail())
        .contractorFirstName(contactName[0])
        .contractorLastName(contactName[1])
        .companyName(e.getCompanyName())
        .companyTaxId(e.getCompanyTaxId())
        .companyAddress(e.getCompanyAddress())
        .contractorId(e.getContractor() != null ? e.getContractor().getId() : null)
        .emailVerifiedAt(e.getEmailVerifiedAt())
        .paymentVerifiedAt(e.getPaymentVerifiedAt())
        .expiresAt(e.getExpiresAt())
        .createdAt(e.getCreatedAt())
        .updatedAt(e.getUpdatedAt())
        .build();
  }

  // ── AppSubscription ──────────────────────────────────────────────────────

  public static AppSubscription toDomain(AppSubscriptionEntity e) {
    return AppSubscription.builder()
        .id(e.getId())
        .clientAppId(e.getClientApp() != null ? e.getClientApp().getId() : null)
        .appPlanVersionId(e.getAppPlanVersion().getId())
        .contractId(e.getContract() != null ? e.getContract().getId() : null)
        .contractorId(e.getContractor().getId())
        .status(e.getStatus())
        .currentPeriodStart(e.getCurrentPeriodStart())
        .currentPeriodEnd(e.getCurrentPeriodEnd())
        .cancelAtPeriodEnd(e.isCancelAtPeriodEnd())
        .cancelledAt(e.getCancelledAt())
        .nextBillingAt(e.getNextBillingAt())
        .autoRenew(e.isAutoRenew())
        .createdAt(e.getCreatedAt())
        .updatedAt(e.getUpdatedAt())
        .build();
  }

  // ── Invoice ──────────────────────────────────────────────────────────────

  public static Invoice toDomain(InvoiceEntity e) {
    return Invoice.builder()
        .id(e.getId())
        .subscriptionId(e.getSubscription().getId())
        .invoiceNumber(e.getInvoiceNumber())
        .status(e.getStatus())
        .issueDate(e.getIssueDate())
        .dueDate(e.getDueDate())
        .periodStart(e.getPeriodStart())
        .periodEnd(e.getPeriodEnd())
        .currency(e.getCurrency())
        .subtotal(e.getSubtotal())
        .taxAmount(e.getTaxAmount())
        .total(e.getTotal())
        .billingNameSnapshot(e.getBillingNameSnapshot())
        .billingTaxIdSnapshot(e.getBillingTaxIdSnapshot())
        .billingAddressSnapshot(e.getBillingAddressSnapshot())
        .planNameSnapshot(e.getPlanNameSnapshot())
        .planVersionSnapshot(e.getPlanVersionSnapshot())
        .pdfUrl(null)
        .createdAt(e.getCreatedAt())
        .build();
  }

  private static String[] resolveContactName(AppContractEntity entity) {
    if (entity.getContractorFirstName() != null && !entity.getContractorFirstName().isBlank()
        && entity.getContractorLastName() != null && !entity.getContractorLastName().isBlank()) {
      return new String[] {entity.getContractorFirstName(), entity.getContractorLastName()};
    }
    return splitContactName(entity.getBillingContactName());
  }

  private static String[] splitContactName(String contactName) {
    if (contactName == null || contactName.isBlank()) {
      return new String[] {"N/A", "N/A"};
    }
    String normalized = contactName.trim().replaceAll("\\s+", " ");
    int separator = normalized.indexOf(' ');
    if (separator < 0) {
      return new String[] {normalized, normalized};
    }
    String firstName = normalized.substring(0, separator).trim();
    String lastName = normalized.substring(separator + 1).trim();
    return new String[] {
        firstName.isBlank() ? "N/A" : firstName,
        lastName.isBlank() ? firstName : lastName
    };
  }
}

