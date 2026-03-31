package io.cmartinezs.keygo.api.billing.response;

import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for the resume-onboarding endpoint.
 * <p>Enriches the standard contract data with UI-oriented fields so the frontend
 * can restore the exact onboarding step without additional logic.
 *
 * @author cmartinezs
 * @version 1.0
 */
public record AppContractResumeData(
    UUID id,
    UUID clientAppId,
    UUID selectedPlanVersionId,
    String billingPeriod,
    String status,
    String contractorEmail,
    String contractorFirstName,
    String contractorLastName,
    String companyName,
    UUID contractorId,
    boolean emailVerified,
    boolean paymentVerified,
    /** True when the verification code has already expired and a new one must be requested. */
    boolean verificationCodeExpired,
    /**
     * Semantic hint for the frontend indicating the next required action.
     * <ul>
     *   <li>{@code ENTER_VERIFICATION_CODE} — code is valid, user should enter it</li>
     *   <li>{@code REQUEST_NEW_CODE} — code expired, user should request a resend</li>
     *   <li>{@code COMPLETE_PAYMENT} — email verified, waiting for payment</li>
     *   <li>{@code ACTIVATE} — payment done, ready to activate</li>
     *   <li>{@code COMPLETE} — contract is active</li>
     *   <li>{@code CANCELLED} — contract is in a terminal state</li>
     * </ul>
     */
    String nextAction,
    OffsetDateTime expiresAt,
    OffsetDateTime createdAt
) {

  public static AppContractResumeData from(AppContract c) {
    boolean codeExpired = c.isVerificationCodeExpired();
    return new AppContractResumeData(
        c.getId(),
        c.getClientAppId(),
        c.getSelectedPlanVersionId(),
        c.getBillingPeriod(),
        c.getStatus().name(),
        c.getContractorEmail(),
        c.getContractorFirstName(),
        c.getContractorLastName(),
        c.getCompanyName(),
        c.getContractorId(),
        c.isEmailVerified(),
        c.isPaymentVerified(),
        codeExpired,
        resolveNextAction(c.getStatus(), codeExpired),
        c.getExpiresAt(),
        c.getCreatedAt()
    );
  }

  private static String resolveNextAction(ContractStatus status, boolean codeExpired) {
    return switch (status) {
      case PENDING_EMAIL_VERIFICATION -> codeExpired ? "REQUEST_NEW_CODE" : "ENTER_VERIFICATION_CODE";
      case PENDING_PAYMENT            -> "COMPLETE_PAYMENT";
      case READY_TO_ACTIVATE          -> "ACTIVATE";
      case ACTIVE                     -> "COMPLETE";
      default                         -> "CANCELLED";
    };
  }

}

