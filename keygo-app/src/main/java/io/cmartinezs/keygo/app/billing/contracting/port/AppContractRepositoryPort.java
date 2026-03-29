package io.cmartinezs.keygo.app.billing.contracting.port;

import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;

import java.util.Optional;
import java.util.UUID;

/**
 * Port OUT — persistence contract for AppContract.
 * @author cmartinezs
 * @version 1.0
 */
public interface AppContractRepositoryPort {
  AppContract save(AppContract contract);
  Optional<AppContract> findById(UUID id);
  Optional<AppContract> findByClientAppIdAndCompanySlug(UUID clientAppId, String companySlug);
  Optional<AppContract> findByClientAppIdAndContractorEmail(UUID clientAppId, String email);
  boolean existsByClientAppIdAndCompanySlug(UUID clientAppId, String companySlug);
}

