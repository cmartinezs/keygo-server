package io.cmartinezs.keygo.app.billing.catalog.port;

import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;

import java.util.List;
import java.util.UUID;

/**
 * Port OUT — persistence contract for AppPlanEntitlement.
 * @author cmartinezs
 * @version 1.0
 */
public interface AppPlanEntitlementRepositoryPort {
  List<AppPlanEntitlement> findByAppPlanVersionId(UUID appPlanVersionId);
  void saveAll(List<AppPlanEntitlement> entitlements);
}

