package io.cmartinezs.keygo.app.billing.platform.usecase;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanBillingOptionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.result.AppPlanResult;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanBillingOption;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use case: retrieve the public platform-level plan catalog (clientAppId IS NULL).
 * Plans are returned ordered by sort_order ascending (cheapest first),
 * including their active versions, billing options and entitlements.
 *
 * @author cmartinezs
 * @version 2.0
 */
public class GetPlatformPlanCatalogUseCase {

  private final AppPlanRepositoryPort planRepo;
  private final AppPlanVersionRepositoryPort versionRepo;
  private final AppPlanBillingOptionRepositoryPort billingOptionRepo;
  private final AppPlanEntitlementRepositoryPort entitlementRepo;

  public GetPlatformPlanCatalogUseCase(
      AppPlanRepositoryPort planRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppPlanBillingOptionRepositoryPort billingOptionRepo,
      AppPlanEntitlementRepositoryPort entitlementRepo) {
    this.planRepo = planRepo;
    this.versionRepo = versionRepo;
    this.billingOptionRepo = billingOptionRepo;
    this.entitlementRepo = entitlementRepo;
  }

  public List<AppPlanResult> execute() {
    return planRepo.findPlatformPlans().stream()
        .sorted(Comparator.comparingInt(AppPlan::getSortOrder))
        .map(this::buildResult)
        .toList();
  }

  private AppPlanResult buildResult(AppPlan plan) {
    List<AppPlanVersion> versions = versionRepo.findActiveByAppPlanId(plan.getId());

    Map<UUID, List<AppPlanBillingOption>> billingOptionsByVersion = versions.stream()
        .collect(Collectors.toMap(
            AppPlanVersion::getId,
            v -> billingOptionRepo.findByAppPlanVersionId(v.getId())
        ));

    List<AppPlanEntitlement> entitlements = versions.isEmpty()
        ? List.of()
        : entitlementRepo.findByAppPlanVersionId(versions.getFirst().getId());

    return new AppPlanResult(plan, versions, billingOptionsByVersion, entitlements);
  }
}
