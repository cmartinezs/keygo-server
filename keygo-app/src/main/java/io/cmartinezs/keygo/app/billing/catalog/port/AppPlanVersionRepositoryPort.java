package io.cmartinezs.keygo.app.billing.catalog.port;

import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port OUT — persistence contract for AppPlanVersion.
 * @author cmartinezs
 * @version 1.0
 */
public interface AppPlanVersionRepositoryPort {
  List<AppPlanVersion> findActiveByAppPlanId(UUID appPlanId);
  Optional<AppPlanVersion> findById(UUID id);
  AppPlanVersion save(AppPlanVersion version);
  void saveAll(List<AppPlanVersion> versions);
}

