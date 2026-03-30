package io.cmartinezs.keygo.app.billing.catalog.port;

import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanBillingOption;
import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port OUT — persistence contract for AppPlanBillingOption.
 * @author cmartinezs
 * @version 1.0
 */
public interface AppPlanBillingOptionRepositoryPort {
  List<AppPlanBillingOption> findByAppPlanVersionId(UUID appPlanVersionId);
  Optional<AppPlanBillingOption> findByAppPlanVersionIdAndBillingPeriod(UUID appPlanVersionId, BillingPeriod billingPeriod);
  void saveAll(List<AppPlanBillingOption> options);
}



