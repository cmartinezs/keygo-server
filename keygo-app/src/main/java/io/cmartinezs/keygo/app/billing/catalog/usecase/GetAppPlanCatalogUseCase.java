package io.cmartinezs.keygo.app.billing.catalog.usecase;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.result.AppPlanResult;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;

import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;

import java.util.List;
import java.util.UUID;

/**
 * Use case: retrieve the public plan catalog for a ClientApp.
 * Optionally filtered by subscriber type.
 * @author cmartinezs
 * @version 1.0
 */
public class GetAppPlanCatalogUseCase {

  private final AppPlanRepositoryPort planRepo;
  private final AppPlanVersionRepositoryPort versionRepo;
  private final AppPlanEntitlementRepositoryPort entitlementRepo;

  public GetAppPlanCatalogUseCase(
      AppPlanRepositoryPort planRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppPlanEntitlementRepositoryPort entitlementRepo) {
    this.planRepo = planRepo;
    this.versionRepo = versionRepo;
    this.entitlementRepo = entitlementRepo;
  }

  public List<AppPlanResult> execute(UUID clientAppId, SubscriberType subscriberType) {
    var plans = subscriberType == null
        ? planRepo.findPublicByClientAppId(clientAppId)
        : planRepo.findPublicByClientAppIdAndSubscriberType(clientAppId, subscriberType);

    return plans.stream()
        .filter(p -> p.isActive())
        .map(plan -> {
          var versions = versionRepo.findActiveByAppPlanId(plan.getId());
          List<AppPlanEntitlement> entitlements = versions.isEmpty()
              ? List.<AppPlanEntitlement>of()
              : entitlementRepo.findByAppPlanVersionId(versions.get(0).getId());
          return new AppPlanResult(plan, versions, entitlements);
        })
        .toList();
  }
}



