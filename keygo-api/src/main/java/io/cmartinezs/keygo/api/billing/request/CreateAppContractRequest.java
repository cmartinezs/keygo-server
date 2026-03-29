package io.cmartinezs.keygo.api.billing.request;

import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;

/**
 * Request body for creating an app contract.
 */
public record CreateAppContractRequest(
    String planVersionId,
    BillingPeriod billingPeriod,
    String contractorEmail,
    String contractorFirstName,
    String contractorLastName,
    // Company fields (optional, for B2B onboarding)
    String companyName,
    String companySlug,
    String companyTaxId,
    String companyAddress
) {}
