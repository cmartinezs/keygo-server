package io.cmartinezs.keygo.app.billing.catalog.usecase;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanBillingOptionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.result.AppPlanResult;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanBillingOption;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use case: retrieve a single public plan with its billing options and entitlements.
 * @author cmartinezs
 * @version 1.0
 */
public class GetAppPlanUseCase {

  private final AppPlanRepositoryPort planRepo;
  private final AppPlanVersionRepositoryPort versionRepo;
  private final AppPlanBillingOptionRepositoryPort billingOptionRepo;
  private final AppPlanEntitlementRepositoryPort entitlementRepo;

  public GetAppPlanUseCase(
      AppPlanRepositoryPort planRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppPlanBillingOptionRepositoryPort billingOptionRepo,
      AppPlanEntitlementRepositoryPort entitlementRepo) {
    this.planRepo = planRepo;
    this.versionRepo = versionRepo;
    this.billingOptionRepo = billingOptionRepo;
    this.entitlementRepo = entitlementRepo;
  }

  public AppPlanResult execute(UUID clientAppId, String planCode) {
    var plan = planRepo.findByClientAppIdAndCode(clientAppId, planCode)
        .filter(p -> p.isPublic() && p.isActive())
        .orElseThrow(() -> new ClientAppNotFoundException(
            "Plan not found or not public: " + planCode));

    List<AppPlanVersion> versions = versionRepo.findActiveByAppPlanId(plan.getId());

    Map<UUID, List<AppPlanBillingOption>> billingOptionsByVersion = versions.stream()
        .collect(Collectors.toMap(
            AppPlanVersion::getId,
            v -> billingOptionRepo.findByAppPlanVersionId(v.getId())
        ));

    List<AppPlanEntitlement> entitlements = versions.isEmpty()
        ? List.of()
        : entitlementRepo.findByAppPlanVersionId(versions.get(0).getId());

    return new AppPlanResult(plan, versions, billingOptionsByVersion, entitlements);
  }
}
