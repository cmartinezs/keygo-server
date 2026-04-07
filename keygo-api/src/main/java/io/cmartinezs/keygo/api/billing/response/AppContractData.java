package io.cmartinezs.keygo.api.billing.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.domain.shared.util.EmailMasker;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response data for app contract endpoints — billing model v2.
 */
public record AppContractData(
    UUID id,
    @JsonProperty("client_app_id") UUID clientAppId,
    @JsonProperty("selected_plan_version_id") UUID selectedPlanVersionId,
    @JsonProperty("billing_period") String billingPeriod,
    String status,
    @JsonProperty("notification_email") String notificationEmail,
    @JsonProperty("contractor_first_name") String contractorFirstName,
    @JsonProperty("contractor_last_name") String contractorLastName,
    @JsonProperty("company_name") String companyName,
    @JsonProperty("contractor_id") UUID contractorId,
    @JsonProperty("email_verified") boolean emailVerified,
    @JsonProperty("payment_verified") boolean paymentVerified,
    @JsonProperty("expires_at") OffsetDateTime expiresAt,
    @JsonProperty("created_at") OffsetDateTime createdAt
) {
  public static AppContractData from(AppContract c) {
    return new AppContractData(
        c.getId(),
        c.getClientAppId(),
        c.getSelectedPlanVersionId(),
        c.getBillingPeriod(),
        c.getStatus().name(),
        EmailMasker.mask(c.getContractorEmail()),
        c.getContractorFirstName(),
        c.getContractorLastName(),
        c.getCompanyName(),
        c.getContractorId(),
        c.isEmailVerified(),
        c.isPaymentVerified(),
        c.getExpiresAt(),
        c.getCreatedAt()
    );
  }

}
