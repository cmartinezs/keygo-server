package io.cmartinezs.keygo.app.billing.catalog.port;

import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port OUT — persistence contract for AppPlan.
 * @author cmartinezs
 * @version 1.0
 */
public interface AppPlanRepositoryPort {
  List<AppPlan> findPublicByClientAppId(UUID clientAppId);
  List<AppPlan> findAllByClientAppId(UUID clientAppId);
  Optional<AppPlan> findByClientAppIdAndCode(UUID clientAppId, String code);
  boolean existsByClientAppIdAndCode(UUID clientAppId, String code);
  AppPlan save(AppPlan plan);

  /** Platform plans: WHERE client_app_id IS NULL AND status = ACTIVE AND is_public = true */
  List<AppPlan> findPlatformPlans();

  /** Single platform plan by code: WHERE client_app_id IS NULL AND code = ? */
  Optional<AppPlan> findPlatformPlanByCode(String code);
}
