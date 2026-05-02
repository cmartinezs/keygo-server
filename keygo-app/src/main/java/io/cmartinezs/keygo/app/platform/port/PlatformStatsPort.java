package io.cmartinezs.keygo.app.platform.port;

import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.model.UserStatus;

/**
 * Port OUT — provides platform-wide aggregate statistics.
 * <p>Puerto de salida — provee estadísticas agregadas a nivel plataforma.
 *
 * @author cmartinezs
 * @version 1.0
 */
public interface PlatformStatsPort {

  /** Total number of tenants. */
  long countTotalTenants();

  /** Number of tenants with the given status. */
  long countTenantsByStatus(TenantStatus status);

  /** Total number of tenant-users (across all tenants). */
  long countTotalUsers();

  /** Number of tenant-users with the given status (across all tenants). */
  long countUsersByStatus(UserStatus status);

  /** Total number of client apps (across all tenants). */
  long countTotalApps();

  /** Number of signing keys currently in ACTIVE status. */
  long countActiveSigningKeys();
}

