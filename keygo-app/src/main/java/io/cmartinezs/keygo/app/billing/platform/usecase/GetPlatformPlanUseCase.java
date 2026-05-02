package io.cmartinezs.keygo.app.billing.platform.usecase;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanBillingOptionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.result.AppPlanResult;
import io.cmartinezs.keygo.app.billing.platform.exception.PlanNotFoundException;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanBillingOption;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use case: retrieve a single platform-level plan by code (clientAppId IS NULL),
 * including its active versions, billing options and entitlements.
 *
 * @author cmartinezs
 * @version 2.0
 */
public class GetPlatformPlanUseCase {

  private final AppPlanRepositoryPort planRepo;
  private final AppPlanVersionRepositoryPort versionRepo;
  private final AppPlanBillingOptionRepositoryPort billingOptionRepo;
  private final AppPlanEntitlementRepositoryPort entitlementRepo;

  public GetPlatformPlanUseCase(
      AppPlanRepositoryPort planRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppPlanBillingOptionRepositoryPort billingOptionRepo,
      AppPlanEntitlementRepositoryPort entitlementRepo) {
    this.planRepo = planRepo;
    this.versionRepo = versionRepo;
    this.billingOptionRepo = billingOptionRepo;
    this.entitlementRepo = entitlementRepo;
  }

  public AppPlanResult execute(String planCode) {
    var plan = planRepo.findPlatformPlanByCode(planCode)
        .orElseThrow(() -> new PlanNotFoundException(planCode));

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
