package io.cmartinezs.keygo.app.billing.contracting.command;

import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;

import java.util.UUID;

/**
 * Command to create a new app contract.
 */
public record CreateAppContractCommand(
    UUID clientAppId,
    UUID planVersionId,
    BillingPeriod billingPeriod,
    SubscriberType subscriberType,
    String contractorEmail,
    String contractorFirstName,
    String contractorLastName,
    // Only required for TENANT subscriber type
    String companyName,
    String companySlug,
    String companyTaxId,
    String companyAddress
) {}

