package io.cmartinezs.keygo.api.billing.response;

import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response data for app contract endpoints.
 */
public record AppContractData(
    UUID id,
    UUID clientAppId,
    UUID selectedPlanVersionId,
    String billingPeriod,
    String status,
    String contractorEmail,
    String contractorFirstName,
    String contractorLastName,
    String companyName,
    String companySlug,
    boolean emailVerified,
    boolean paymentVerified,
    OffsetDateTime expiresAt,
    OffsetDateTime createdAt
) {
  public static AppContractData from(AppContract c) {
    return new AppContractData(
        c.getId(),
        c.getClientAppId(),
        c.getSelectedPlanVersionId(),
        c.getBillingPeriod(),
        c.getStatus().name(),
        c.getContractorEmail(),
        c.getContractorFirstName(),
        c.getContractorLastName(),
        c.getCompanyName(),
        c.getCompanySlug(),
        c.isEmailVerified(),
        c.isPaymentVerified(),
        c.getExpiresAt(),
        c.getCreatedAt()
    );
  }

  /** Solo para referencia de schema OpenAPI — no instanciar en lógica de negocio. */
  public static final class Response extends BaseResponse<AppContractData> {}
}
