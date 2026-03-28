package io.cmartinezs.keygo.app.platform.result;

import lombok.Builder;
import lombok.Getter;

/**
 * Result of the GetPlatformStatsUseCase — aggregated platform-wide counters.
 * <p>Resultado del GetPlatformStatsUseCase — contadores agregados a nivel plataforma.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class PlatformStatsResult {
  private long totalTenants;
  private long activeTenants;
  private long suspendedTenants;
  private long pendingTenants;

  private long totalUsers;
  private long activeUsers;
  private long pendingUsers;
  private long suspendedUsers;

  private long totalApps;
  private long activeSigningKeys;
}

