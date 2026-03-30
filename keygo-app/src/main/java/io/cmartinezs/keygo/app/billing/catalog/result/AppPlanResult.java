package io.cmartinezs.keygo.app.billing.catalog.result;

import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanBillingOption;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Result returned by catalog use cases.
 *
 * @param plan                  the plan (includes sortOrder)
 * @param versions              active versions of the plan
 * @param billingOptionsByVersion billing options keyed by version ID; empty list = free plan
 * @param entitlements          entitlements of the first active version (may be empty)
 */
public record AppPlanResult(
    AppPlan plan,
    List<AppPlanVersion> versions,
    Map<UUID, List<AppPlanBillingOption>> billingOptionsByVersion,
    List<AppPlanEntitlement> entitlements
) {}
