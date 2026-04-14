package io.cmartinezs.keygo.api.billing.request;

import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for creating an app contract.
 * <p>In billing model v2, the contract is independent of the tenant/app URL path.
 * The target client app is identified directly via {@code clientAppId} in the body.
 */
public record CreateAppContractRequest(
    // Target client app (optional for platform-level contracts; app-level contracts supply this)
    String clientAppId,
    @NotBlank(message = "plan_version_id is required") String planVersionId,
    @NotNull(message = "billing_period is required") BillingPeriod billingPeriod,
    @NotBlank(message = "contractor_email is required")
    @Email(message = "contractor_email must be a valid email address")
    String contractorEmail,
    @NotBlank(message = "contractor_first_name is required") String contractorFirstName,
    @NotBlank(message = "contractor_last_name is required") String contractorLastName,
    // Company fields (optional, for B2B invoicing — no longer creates a Tenant in model v2)
    String companyName,
    String companyTaxId,
    String companyAddress
) {}
