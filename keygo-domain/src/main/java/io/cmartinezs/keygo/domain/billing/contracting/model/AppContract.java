package io.cmartinezs.keygo.domain.billing.contracting.model;

import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Domain model for an app contract (the contracting process).
 * A contract transitions through states until it is ACTIVATED,
 * at which point a TenantSubscription is created.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public class AppContract {

  private final UUID id;
  private final UUID clientAppId;
  private final UUID selectedPlanVersionId;
  private final String billingPeriod;
  private final SubscriberType subscriberType;
  private ContractStatus status;

  // Contractor data (always present)
  private final String contractorEmail;
  private final String contractorFirstName;
  private final String contractorLastName;

  // Company data (only when subscriberType = TENANT)
  private final String companyName;
  private final String companySlug;
  private final String companyTaxId;
  private final String companyAddress;

  // Traceability
  private OffsetDateTime emailVerifiedAt;
  private OffsetDateTime paymentVerifiedAt;
  private final OffsetDateTime expiresAt;
  private final OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  // Subscriber references (set on ACTIVATED)
  private UUID subscriberTenantId;
  private UUID subscriberTenantUserId;

  @Builder
  private AppContract(
      UUID id,
      UUID clientAppId,
      UUID selectedPlanVersionId,
      String billingPeriod,
      SubscriberType subscriberType,
      ContractStatus status,
      String contractorEmail,
      String contractorFirstName,
      String contractorLastName,
      String companyName,
      String companySlug,
      String companyTaxId,
      String companyAddress,
      OffsetDateTime emailVerifiedAt,
      OffsetDateTime paymentVerifiedAt,
      OffsetDateTime expiresAt,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt,
      UUID subscriberTenantId,
      UUID subscriberTenantUserId) {
    if (clientAppId == null) throw new IllegalArgumentException("clientAppId cannot be null");
    if (selectedPlanVersionId == null) throw new IllegalArgumentException("selectedPlanVersionId cannot be null");
    if (subscriberType == null) throw new IllegalArgumentException("subscriberType cannot be null");
    if (contractorEmail == null || contractorEmail.isBlank()) throw new IllegalArgumentException("contractorEmail cannot be blank");
    if (contractorFirstName == null || contractorFirstName.isBlank()) throw new IllegalArgumentException("contractorFirstName cannot be blank");
    if (contractorLastName == null || contractorLastName.isBlank()) throw new IllegalArgumentException("contractorLastName cannot be blank");
    if (status == null) throw new IllegalArgumentException("status cannot be null");
    if (expiresAt == null) throw new IllegalArgumentException("expiresAt cannot be null");

    this.id = id;
    this.clientAppId = clientAppId;
    this.selectedPlanVersionId = selectedPlanVersionId;
    this.billingPeriod = billingPeriod;
    this.subscriberType = subscriberType;
    this.status = status;
    this.contractorEmail = contractorEmail;
    this.contractorFirstName = contractorFirstName;
    this.contractorLastName = contractorLastName;
    this.companyName = companyName;
    this.companySlug = companySlug;
    this.companyTaxId = companyTaxId;
    this.companyAddress = companyAddress;
    this.emailVerifiedAt = emailVerifiedAt;
    this.paymentVerifiedAt = paymentVerifiedAt;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.subscriberTenantId = subscriberTenantId;
    this.subscriberTenantUserId = subscriberTenantUserId;
  }

  public boolean isEmailVerified() {
    return emailVerifiedAt != null;
  }

  public boolean isPaymentVerified() {
    return paymentVerifiedAt != null;
  }

  public boolean isReadyToActivate() {
    return ContractStatus.READY_TO_ACTIVATE.equals(this.status);
  }

  public boolean isActivated() {
    return ContractStatus.ACTIVATED.equals(this.status);
  }

  public void markEmailVerified(OffsetDateTime verifiedAt) {
    this.emailVerifiedAt = verifiedAt;
    if (ContractStatus.PENDING_EMAIL_VERIFICATION.equals(this.status)) {
      this.status = ContractStatus.PENDING_PAYMENT;
    }
    this.updatedAt = verifiedAt;
  }

  public void markPaymentApproved(OffsetDateTime approvedAt) {
    this.paymentVerifiedAt = approvedAt;
    this.status = ContractStatus.READY_TO_ACTIVATE;
    this.updatedAt = approvedAt;
  }

  public void activate(UUID tenantId, UUID tenantUserId, OffsetDateTime activatedAt) {
    if (!isReadyToActivate()) {
      throw new IllegalStateException("Contract is not in READY_TO_ACTIVATE state: " + this.status);
    }
    if (tenantId != null && tenantUserId != null) {
      throw new IllegalArgumentException("Cannot set both tenantId and tenantUserId on a contract");
    }
    this.subscriberTenantId = tenantId;
    this.subscriberTenantUserId = tenantUserId;
    this.status = ContractStatus.ACTIVATED;
    this.updatedAt = activatedAt;
  }
}

