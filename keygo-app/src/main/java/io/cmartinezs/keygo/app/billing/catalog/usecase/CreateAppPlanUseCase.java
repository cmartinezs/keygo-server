package io.cmartinezs.keygo.app.billing.catalog.usecase;

import io.cmartinezs.keygo.app.billing.catalog.command.CreateAppPlanCommand;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.result.AppPlanResult;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanStatus;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersionStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Use case: create a new plan with its first version and entitlements.
 * @author cmartinezs
 * @version 1.0
 */
public class CreateAppPlanUseCase {

  private final AppPlanRepositoryPort planRepo;
  private final AppPlanVersionRepositoryPort versionRepo;
  private final AppPlanEntitlementRepositoryPort entitlementRepo;

  public CreateAppPlanUseCase(
      AppPlanRepositoryPort planRepo,
      AppPlanVersionRepositoryPort versionRepo,
      AppPlanEntitlementRepositoryPort entitlementRepo) {
    this.planRepo = planRepo;
    this.versionRepo = versionRepo;
    this.entitlementRepo = entitlementRepo;
  }

  public AppPlanResult execute(CreateAppPlanCommand cmd) {
    if (planRepo.existsByClientAppIdAndCode(cmd.clientAppId(), cmd.code())) {
      throw new IllegalArgumentException("Plan code already exists for this app: " + cmd.code());
    }

    AppPlan plan = AppPlan.builder()
        .clientAppId(cmd.clientAppId())
        .code(cmd.code())
        .name(cmd.name())
        .description(cmd.description())
        .status(AppPlanStatus.ACTIVE)
        .isPublic(cmd.isPublic())
        .build();
    plan = planRepo.save(plan);

    AppPlanVersion version = AppPlanVersion.builder()
        .appPlanId(plan.getId())
        .version(cmd.version())
        .currency(cmd.currency() != null ? cmd.currency() : "MXN")
        .billingPeriod(cmd.billingPeriod())
        .basePrice(cmd.basePrice() != null ? cmd.basePrice() : BigDecimal.ZERO)
        .setupFee(BigDecimal.ZERO)
        .trialDays(cmd.trialDays())
        .effectiveFrom(cmd.effectiveFrom())
        .status(AppPlanVersionStatus.ACTIVE)
        .build();
    version = versionRepo.save(version);

    List<AppPlanEntitlement> entitlements = List.of();
    if (cmd.entitlements() != null && !cmd.entitlements().isEmpty()) {
      final UUID versionId = version.getId();
      entitlements = cmd.entitlements().stream()
          .map(e -> AppPlanEntitlement.builder()
              .appPlanVersionId(versionId)
              .metricCode(e.metricCode())
              .metricType(e.metricType())
              .limitValue(e.limitValue())
              .periodType(e.periodType())
              .enforcementMode(e.enforcementMode())
              .isEnabled(e.isEnabled())
              .build())
          .toList();
      entitlementRepo.saveAll(entitlements);
    }

    return new AppPlanResult(plan, List.of(version), entitlements);
  }
}
