package io.cmartinezs.keygo.api.billing.request;

import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;

/**
 * Request body for creating an app contract.
 */
public record CreateAppContractRequest(
    String planVersionId,
    BillingPeriod billingPeriod,
    SubscriberType subscriberType,
    String contractorEmail,
    String contractorFirstName,
    String contractorLastName,
    // Company fields (TENANT subscriber type only)
    String companyName,
    String companySlug,
    String companyTaxId,
    String companyAddress
) {}

