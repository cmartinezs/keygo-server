package io.cmartinezs.keygo.app.billing.platform.usecase;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanRepositoryPort;
import io.cmartinezs.keygo.app.billing.platform.exception.PlanNotFoundException;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;

/**
 * Use case: retrieve a single platform-level plan by code (clientAppId IS NULL).
 *
 * @author cmartinezs
 * @version 1.0
 */
public class GetPlatformPlanUseCase {

  private final AppPlanRepositoryPort planRepo;

  public GetPlatformPlanUseCase(AppPlanRepositoryPort planRepo) {
    this.planRepo = planRepo;
  }

  public AppPlan execute(String planCode) {
    return planRepo.findPlatformPlanByCode(planCode)
        .orElseThrow(() -> new PlanNotFoundException(planCode));
  }
}
