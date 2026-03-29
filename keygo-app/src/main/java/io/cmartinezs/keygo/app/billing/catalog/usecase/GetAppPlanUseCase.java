package io.cmartinezs.keygo.app.billing.catalog.usecase;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.result.AppPlanResult;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;

import java.util.List;
import java.util.UUID;

/**
 * Use case: retrieve a single public plan with its entitlements.
 * @author cmartinezs
 * @version 1.0
 */
public class GetAppPlanUseCase {

  private final AppPlanRepositoryPort planRepo;
  private final AppPlanVersionRepositoryPort versionRepo;
  private final AppPlanEntitlementRepositoryPort entitlementRepo;

  public GetAppPlanUseCase(
      AppPlanRepositoryPort planRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppPlanEntitlementRepositoryPort entitlementRepo) {
    this.planRepo = planRepo;
    this.versionRepo = versionRepo;
    this.entitlementRepo = entitlementRepo;
  }

  public AppPlanResult execute(UUID clientAppId, String planCode) {
    var plan = planRepo.findByClientAppIdAndCode(clientAppId, planCode)
        .filter(p -> p.isPublic() && p.isActive())
        .orElseThrow(() -> new ClientAppNotFoundException(
            "Plan not found or not public: " + planCode));

    var versions = versionRepo.findActiveByAppPlanId(plan.getId());
    List<AppPlanEntitlement> entitlements = versions.isEmpty()
        ? List.of()
        : entitlementRepo.findByAppPlanVersionId(versions.get(0).getId());

    return new AppPlanResult(plan, versions, entitlements);
  }
}



