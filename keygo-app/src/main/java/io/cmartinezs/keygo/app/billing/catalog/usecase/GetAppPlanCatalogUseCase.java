package io.cmartinezs.keygo.app.billing.catalog.usecase;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanBillingOptionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.result.AppPlanResult;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanBillingOption;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use case: retrieve the public plan catalog for a ClientApp.
 * Plans are returned ordered by sort_order ascending (cheapest first).
 * @author cmartinezs
 * @version 1.0
 */
public class GetAppPlanCatalogUseCase {

  private final AppPlanRepositoryPort planRepo;
  private final AppPlanVersionRepositoryPort versionRepo;
  private final AppPlanBillingOptionRepositoryPort billingOptionRepo;
  private final AppPlanEntitlementRepositoryPort entitlementRepo;

  public GetAppPlanCatalogUseCase(
      AppPlanRepositoryPort planRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppPlanBillingOptionRepositoryPort billingOptionRepo,
      AppPlanEntitlementRepositoryPort entitlementRepo) {
    this.planRepo = planRepo;
    this.versionRepo = versionRepo;
    this.billingOptionRepo = billingOptionRepo;
    this.entitlementRepo = entitlementRepo;
  }

  public List<AppPlanResult> execute(UUID clientAppId) {
    return planRepo.findPublicByClientAppId(clientAppId)
        .stream()
        .filter(AppPlan::isActive)
        .sorted(java.util.Comparator.comparingInt(AppPlan::getSortOrder))
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
