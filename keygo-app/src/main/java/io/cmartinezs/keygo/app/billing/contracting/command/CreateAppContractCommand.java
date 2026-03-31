package io.cmartinezs.keygo.app.billing.contracting.command;

import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;

import java.util.UUID;

/**
 * Command to create a new app contract.
 */
public record CreateAppContractCommand(
    UUID clientAppId,
    UUID planVersionId,
    BillingPeriod billingPeriod,
    String contractorEmail,
    String contractorFirstName,
    String contractorLastName,
    // Company fields (optional, for B2B invoicing — no longer creates a Tenant)
    String companyName,
    String companyTaxId,
    String companyAddress
) {}
