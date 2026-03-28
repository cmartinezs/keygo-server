package io.cmartinezs.keygo.app.platform.usecase;

import io.cmartinezs.keygo.app.platform.port.PlatformStatsPort;
import io.cmartinezs.keygo.app.platform.result.PlatformStatsResult;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.model.UserStatus;

/**
 * Use case: retrieve aggregated platform statistics.
 * <p>Caso de uso: obtener estadísticas agregadas de la plataforma.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class GetPlatformStatsUseCase {

  private final PlatformStatsPort platformStatsPort;

  public GetPlatformStatsUseCase(PlatformStatsPort platformStatsPort) {
    this.platformStatsPort = platformStatsPort;
  }

  /**
   * Executes the use case and returns the aggregated stats.
   *
   * @return PlatformStatsResult with all counters populated
   */
  public PlatformStatsResult execute() {
    return PlatformStatsResult.builder()
        .totalTenants(platformStatsPort.countTotalTenants())
        .activeTenants(platformStatsPort.countTenantsByStatus(TenantStatus.ACTIVE))
        .suspendedTenants(platformStatsPort.countTenantsByStatus(TenantStatus.SUSPENDED))
        .pendingTenants(platformStatsPort.countTenantsByStatus(TenantStatus.PENDING))
        .totalUsers(platformStatsPort.countTotalUsers())
        .activeUsers(platformStatsPort.countUsersByStatus(UserStatus.ACTIVE))
        .pendingUsers(platformStatsPort.countUsersByStatus(UserStatus.PENDING))
        .suspendedUsers(platformStatsPort.countUsersByStatus(UserStatus.SUSPENDED))
        .totalApps(platformStatsPort.countTotalApps())
        .activeSigningKeys(platformStatsPort.countActiveSigningKeys())
        .build();
  }
}

