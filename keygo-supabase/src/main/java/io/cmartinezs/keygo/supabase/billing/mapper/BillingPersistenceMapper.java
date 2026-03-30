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
        .clientAppId(e.getClientApp().getId())
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
    return AppContract.builder()
        .id(e.getId())
        .clientAppId(e.getClientApp().getId())
        .selectedPlanVersionId(e.getSelectedPlanVersion().getId())
        .billingPeriod(e.getBillingPeriod())
        .status(e.getStatus())
        .contractorEmail(e.getContractorEmail())
        .contractorFirstName(e.getContractorFirstName())
        .contractorLastName(e.getContractorLastName())
        .companyName(e.getCompanyName())
        .companySlug(e.getCompanySlug())
        .companyTaxId(e.getCompanyTaxId())
        .companyAddress(e.getCompanyAddress())
        .verificationCode(e.getVerificationCode())
        .verificationCodeExpiresAt(e.getVerificationCodeExpiresAt())
        .emailVerifiedAt(e.getEmailVerifiedAt())
        .paymentVerifiedAt(e.getPaymentVerifiedAt())
        .expiresAt(e.getExpiresAt())
        .createdAt(e.getCreatedAt())
        .updatedAt(e.getUpdatedAt())
        .subscriberTenantId(e.getSubscriberTenant() != null ? e.getSubscriberTenant().getId() : null)
        .subscriberTenantUserId(e.getSubscriberTenantUser() != null ? e.getSubscriberTenantUser().getId() : null)
        .build();
  }

  // ── AppSubscription ──────────────────────────────────────────────────────

  public static AppSubscription toDomain(AppSubscriptionEntity e) {
    return AppSubscription.builder()
        .id(e.getId())
        .clientAppId(e.getClientApp().getId())
        .appPlanVersionId(e.getAppPlanVersion().getId())
        .contractId(e.getContract() != null ? e.getContract().getId() : null)
        .subscriberTenantId(e.getSubscriberTenant() != null ? e.getSubscriberTenant().getId() : null)
        .subscriberTenantUserId(e.getSubscriberTenantUser() != null ? e.getSubscriberTenantUser().getId() : null)
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
        .pdfUrl(e.getPdfUrl())
        .createdAt(e.getCreatedAt())
        .build();
  }
}

