package io.cmartinezs.keygo.domain.billing.contracting.model;

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
  private ContractStatus status;

  // Contractor data (always present)
  private final String contractorEmail;
  private final String contractorFirstName;
  private final String contractorLastName;

  // Company data (for B2B onboarding)
  private final String companyName;
  private final String companySlug;
  private final String companyTaxId;
  private final String companyAddress;

  // Email verification (contrato propio, antes de que exista un TenantUser)
  private String verificationCode;
  private OffsetDateTime verificationCodeExpiresAt;

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
      ContractStatus status,
      String contractorEmail,
      String contractorFirstName,
      String contractorLastName,
      String companyName,
      String companySlug,
      String companyTaxId,
      String companyAddress,
      String verificationCode,
      OffsetDateTime verificationCodeExpiresAt,
      OffsetDateTime emailVerifiedAt,
      OffsetDateTime paymentVerifiedAt,
      OffsetDateTime expiresAt,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt,
      UUID subscriberTenantId,
      UUID subscriberTenantUserId) {
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
    this.companySlug = companySlug;
    this.companyTaxId = companyTaxId;
    this.companyAddress = companyAddress;
    this.verificationCode = verificationCode;
    this.verificationCodeExpiresAt = verificationCodeExpiresAt;
    this.emailVerifiedAt = emailVerifiedAt;
    this.paymentVerifiedAt = paymentVerifiedAt;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.subscriberTenantId = subscriberTenantId;
    this.subscriberTenantUserId = subscriberTenantUserId;
  }

  /**
   * Generates a username from the contractor's name: first initial + last name.
   * Sanitizes input by normalizing accented characters (NFD → strip combining marks)
   * and removing any character not allowed by the Username value object
   * ({@code [a-zA-Z0-9_\-.]}). Result is lowercase, at least 3 characters
   * and at most 100 characters (matching the JPA column length).
   *
   * <p>Examples:
   * <ul>
   *   <li>"Carlos" / "Martínez" → "cmartinez"</li>
   *   <li>"José" / "Ñoño" → "jnono"</li>
   *   <li>"Ana" / "Li" → "ali"</li>
   *   <li>"Ana" / "I" → "ai_"  (padded to min 3)</li>
   * </ul>
   *
   * @return a valid username string ready to wrap in {@code Username.of(...)}.
   */
  public String generateUsername() {
    String sanitizedFirst = sanitizeForUsername(contractorFirstName);
    String sanitizedLast  = sanitizeForUsername(contractorLastName);

    String initial = sanitizedFirst.isEmpty() ? "" : String.valueOf(sanitizedFirst.charAt(0));
    StringBuilder candidate = new StringBuilder((initial + sanitizedLast).toLowerCase());

    // Ensure minimum length (pad with '_')
    while (candidate.length() < 3) {
      candidate.append("_");
    }

    // Truncate to column / Username max (100)
    if (candidate.length() > 100) {
      candidate = new StringBuilder(candidate.substring(0, 100));
    }

    return candidate.toString();
  }

  /**
   * Strips diacritics and removes characters not allowed in a Username.
   * Uses only standard {@code java.text.Normalizer} — no external dependencies.
   */
  private static String sanitizeForUsername(String input) {
    if (input == null || input.isBlank()) {
      return "";
    }
    // NFD normalization decomposes accented chars into base + combining mark
    String decomposed = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
    // Remove combining diacritical marks (U+0300 – U+036F)
    String stripped = decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    // Keep only allowed chars: letters, digits, underscore, dash, dot
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

  public boolean isActivated() {
    return ContractStatus.ACTIVATED.equals(this.status);
  }

  /**
   * Verifica el código de verificación de email del contrato.
   * Si es válido, avanza el estado a PENDING_PAYMENT.
   */
  public void verifyCode(String inputCode, OffsetDateTime now) {
    if (ContractStatus.ACTIVATED.equals(this.status) || ContractStatus.CANCELLED.equals(this.status)
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
