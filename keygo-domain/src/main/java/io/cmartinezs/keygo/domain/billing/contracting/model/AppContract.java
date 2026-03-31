package io.cmartinezs.keygo.domain.billing.contracting.model;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Domain model for an app contract (the contracting process) — billing model v2.
 * A contract transitions through states until it is ACTIVE,
 * at which point a Subscription is created for the Contractor.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public class AppContract {

  private final UUID id;
  private final UUID clientAppId;
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

  // Contractor link (set during email verification → PENDING_PAYMENT transition)
  private UUID contractorId;

  // Email verification (independent from email_verifications table)
  private String verificationCode;
  private OffsetDateTime verificationCodeExpiresAt;

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
      String verificationCode,
      OffsetDateTime verificationCodeExpiresAt,
      OffsetDateTime emailVerifiedAt,
      OffsetDateTime paymentVerifiedAt,
      OffsetDateTime expiresAt,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    if (clientAppId == null) throw new IllegalArgumentException("clientAppId cannot be null");
    if (selectedPlanVersionId == null) throw new IllegalArgumentException("selectedPlanVersionId cannot be null");
    if (contractorEmail == null || contractorEmail.isBlank()) throw new IllegalArgumentException("contractorEmail cannot be blank");
    if (contractorFirstName == null || contractorFirstName.isBlank()) throw new IllegalArgumentException("contractorFirstName cannot be blank");
    if (contractorLastName == null || contractorLastName.isBlank()) throw new IllegalArgumentException("contractorLastName cannot be blank");
    if (status == null) throw new IllegalArgumentException("status cannot be null");
    if (expiresAt == null) throw new IllegalArgumentException("expiresAt cannot be null");

    this.id = id;
    this.clientAppId = clientAppId;
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
    this.verificationCode = verificationCode;
    this.verificationCodeExpiresAt = verificationCodeExpiresAt;
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

  /**
   * Verifies the email verification code and advances the status to PENDING_PAYMENT.
   * Also links the given contractorId to this contract.
   */
  public void verifyCode(String inputCode, UUID resolvedContractorId, OffsetDateTime now) {
    if (ContractStatus.ACTIVE.equals(this.status) || ContractStatus.CANCELLED.equals(this.status)
        || ContractStatus.EXPIRED.equals(this.status) || ContractStatus.FAILED.equals(this.status)) {
      throw new IllegalStateException("El contrato está en estado terminal: " + this.status);
    }
    if (!ContractStatus.PENDING_EMAIL_VERIFICATION.equals(this.status)) {
      throw new IllegalStateException("El email ya fue verificado para el contrato: " + this.id);
    }
    if (this.verificationCode == null || !this.verificationCode.equalsIgnoreCase(inputCode)) {
      throw new IllegalArgumentException("Código de verificación inválido");
    }
    if (this.verificationCodeExpiresAt != null && now.isAfter(this.verificationCodeExpiresAt)) {
      throw new IllegalStateException("El código de verificación ha expirado");
    }
    this.contractorId = resolvedContractorId;
    markEmailVerified(now);
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

  public void activate(OffsetDateTime activatedAt) {
    if (!isReadyToActivate()) {
      throw new IllegalStateException("Contract is not in READY_TO_ACTIVATE state: " + this.status);
    }
    if (this.contractorId == null) {
      throw new IllegalStateException("Contract cannot be activated without a linked Contractor");
    }
    this.status = ContractStatus.ACTIVE;
    this.updatedAt = activatedAt;
  }
}
