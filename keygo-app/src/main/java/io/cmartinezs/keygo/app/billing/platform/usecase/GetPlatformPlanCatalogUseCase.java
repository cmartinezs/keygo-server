package io.cmartinezs.keygo.app.billing.platform.usecase;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanRepositoryPort;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;

import java.util.Comparator;
import java.util.List;

/**
 * Use case: retrieve the public platform-level plan catalog (clientAppId IS NULL).
 * Plans are returned ordered by sort_order ascending (cheapest first).
 *
 * @author cmartinezs
 * @version 1.0
 */
public class GetPlatformPlanCatalogUseCase {

  private final AppPlanRepositoryPort planRepo;

  public GetPlatformPlanCatalogUseCase(AppPlanRepositoryPort planRepo) {
    this.planRepo = planRepo;
  }

  public List<AppPlan> execute() {
    return planRepo.findPlatformPlans().stream()
        .sorted(Comparator.comparingInt(AppPlan::getSortOrder))
        .toList();
  }
}
