package io.cmartinezs.keygo.domain.billing.contracting.model;

import io.cmartinezs.keygo.domain.billing.contracting.exception.ContractStateViolationException;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain model for an app contract (the contracting process) — billing model v2.
 * A contract transitions through states until it is ACTIVE,
 * at which point a Subscription is created for the Contractor.
 * <p>{@code clientAppId}: NULL = platform contract, NOT NULL = app contract.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public class AppContract {

  private final UUID id;
  /** Empty = platform contract, present = app contract. */
  private final Optional<UUID> clientAppId;
  private final UUID selectedPlanVersionId;
  private final String billingPeriod;
  private ContractStatus status;

  // Contractor data (always present, captured at signup)
  private final String contractorEmail;
  private final String contractorFirstName;
  private final String contractorLastName;

  // Company data (optional, for B2B invoicing)
  private final String companyName;
  private final String companyTaxId;
  private final String companyAddress;

  // Contractor link (set during payment approval / provisioning)
  private UUID contractorId;

  // Traceability
  private OffsetDateTime emailVerifiedAt;
  private OffsetDateTime paymentVerifiedAt;
  private final OffsetDateTime expiresAt;
  private final OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  @Builder
  private AppContract(
      UUID id,
      UUID clientAppId,
      UUID selectedPlanVersionId,
      String billingPeriod,
      ContractStatus status,
      String contractorEmail,
      String contractorFirstName,
      String contractorLastName,
      String companyName,
      String companyTaxId,
      String companyAddress,
      UUID contractorId,
      OffsetDateTime emailVerifiedAt,
      OffsetDateTime paymentVerifiedAt,
      OffsetDateTime expiresAt,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    if (selectedPlanVersionId == null) throw new IllegalArgumentException("selectedPlanVersionId cannot be null");
    if (contractorEmail == null || contractorEmail.isBlank()) throw new IllegalArgumentException("contractorEmail cannot be blank");
    if (contractorFirstName == null || contractorFirstName.isBlank()) throw new IllegalArgumentException("contractorFirstName cannot be blank");
    if (contractorLastName == null || contractorLastName.isBlank()) throw new IllegalArgumentException("contractorLastName cannot be blank");
    if (status == null) throw new IllegalArgumentException("status cannot be null");
    if (expiresAt == null) throw new IllegalArgumentException("expiresAt cannot be null");

    this.id = id;
    this.clientAppId = Optional.ofNullable(clientAppId);
    this.selectedPlanVersionId = selectedPlanVersionId;
    this.billingPeriod = billingPeriod;
    this.status = status;
    this.contractorEmail = contractorEmail;
    this.contractorFirstName = contractorFirstName;
    this.contractorLastName = contractorLastName;
    this.companyName = companyName;
    this.companyTaxId = companyTaxId;
    this.companyAddress = companyAddress;
    this.contractorId = contractorId;
    this.emailVerifiedAt = emailVerifiedAt;
    this.paymentVerifiedAt = paymentVerifiedAt;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /**
   * Generates a username from the contractor's name: first initial + last name.
   * Sanitizes input by normalizing accented characters (NFD → strip combining marks)
   * and removing any character not allowed by the Username value object.
   */
  public String generateUsername() {
    String sanitizedFirst = sanitizeForUsername(contractorFirstName);
    String sanitizedLast  = sanitizeForUsername(contractorLastName);

    String initial = sanitizedFirst.isEmpty() ? "" : String.valueOf(sanitizedFirst.charAt(0));
    StringBuilder candidate = new StringBuilder((initial + sanitizedLast).toLowerCase());

    while (candidate.length() < 3) candidate.append("_");
    if (candidate.length() > 100) candidate = new StringBuilder(candidate.substring(0, 100));

    return candidate.toString();
  }

  private static String sanitizeForUsername(String input) {
    if (input == null || input.isBlank()) return "";
    String decomposed = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
    String stripped = decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    return stripped.replaceAll("[^a-zA-Z0-9_\\-.]+", "");
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

  public boolean isActive() {
    return ContractStatus.ACTIVE.equals(this.status);
  }

  public void markEmailVerified(OffsetDateTime verifiedAt) {
    if (!ContractStatus.PENDING_EMAIL_VERIFICATION.equals(this.status)) {
      throw new ContractStateViolationException(this.id, this.status, "markEmailVerified");
    }
    this.emailVerifiedAt = verifiedAt;
    this.status = ContractStatus.PENDING_PAYMENT;
    this.updatedAt = verifiedAt;
  }

  public void linkContractor(UUID linkedContractorId, OffsetDateTime now) {
    if (linkedContractorId == null) {
      throw new IllegalArgumentException("linkedContractorId cannot be null");
    }
    this.contractorId = linkedContractorId;
    this.updatedAt = now;
  }

  public void markPaymentApproved(OffsetDateTime approvedAt) {
    if (!ContractStatus.PENDING_PAYMENT.equals(this.status)
        && !ContractStatus.READY_TO_ACTIVATE.equals(this.status)) {
      throw new ContractStateViolationException(this.id, this.status, "markPaymentApproved");
    }
    this.paymentVerifiedAt = approvedAt;
    this.status = ContractStatus.READY_TO_ACTIVATE;
    this.updatedAt = approvedAt;
  }

  public void activate(OffsetDateTime activatedAt) {
    if (!isReadyToActivate()) {
      throw new ContractStateViolationException(this.id, this.status, "activate");
    }
    if (this.contractorId == null) {
      throw new ContractStateViolationException(this.id, "cannot be activated without a linked contractor");
    }
    this.status = ContractStatus.ACTIVE;
    this.updatedAt = activatedAt;
  }
}
