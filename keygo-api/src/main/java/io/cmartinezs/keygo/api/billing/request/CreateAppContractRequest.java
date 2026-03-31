package io.cmartinezs.keygo.api.billing.request;

import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;

/**
 * Request body for creating an app contract.
 * <p>In billing model v2, the contract is independent of the tenant/app URL path.
 * The target client app is identified directly via {@code clientAppId} in the body.
 */
public record CreateAppContractRequest(
    // Target client app (billing model v2: no tenantSlug/clientId path params needed)
    String clientAppId,
    String planVersionId,
    BillingPeriod billingPeriod,
    String contractorEmail,
    String contractorFirstName,
    String contractorLastName,
    // Company fields (optional, for B2B invoicing — no longer creates a Tenant in model v2)
    String companyName,
    String companyTaxId,
    String companyAddress
) {}
