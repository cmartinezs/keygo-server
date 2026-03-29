package io.cmartinezs.keygo.app.billing.contracting.result;

import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;

/**
 * Result returned by contracting use cases.
 * @param contract     the contract (never null)
 * @param subscription the active subscription (null until ACTIVATED)
 */
public record AppContractResult(
    AppContract contract,
    AppSubscription subscription
) {}

