package io.cmartinezs.keygo.app.billing.catalog.result;

import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;

import java.util.List;

/**
 * Result returned by catalog use cases.
 * @param plan      the plan
 * @param versions  active versions of the plan
 * @param entitlements entitlements of the first active version (may be empty)
 */
public record AppPlanResult(
    AppPlan plan,
    List<AppPlanVersion> versions,
    List<AppPlanEntitlement> entitlements
) {}

