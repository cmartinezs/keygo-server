package io.cmartinezs.keygo.app.platform.result;

import io.cmartinezs.keygo.app.platform.port.PlatformDashboardPort.ActiveSigningKeyInfo;
import io.cmartinezs.keygo.app.platform.port.PlatformDashboardPort.ActivityEntry;
import io.cmartinezs.keygo.app.platform.port.PlatformDashboardPort.AppRankEntry;
import io.cmartinezs.keygo.app.platform.port.PlatformDashboardPort.TenantRankEntry;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Aggregated result of the GetPlatformDashboardUseCase.
 * <p>Resultado agregado del GetPlatformDashboardUseCase.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class PlatformDashboardResult {

  // ── Service summary ────────────────────────────────────────────────────────
  private String serviceTitle;
  private String serviceName;
  private String serviceVersion;
  private String serviceEnvironment;
  private String serviceStatus;

  // ── Tenants ────────────────────────────────────────────────────────────────
  private long totalTenants;
  private long activeTenants;
  private long pendingTenants;
  private long suspendedTenants;
  private long recentlyCreatedTenants;

  // ── Users ──────────────────────────────────────────────────────────────────
  private long totalUsers;
  private long activeUsers;
  private long pendingUsers;
  private long suspendedUsers;
  private long recentlyCreatedUsers;
  private long usersWithoutMembership;

  // ── Apps ───────────────────────────────────────────────────────────────────
  private long totalApps;
  private long activeApps;
  private long pendingApps;
  private long suspendedApps;
  private long publicApps;
  private long confidentialApps;
  private long appsWithoutRedirectUris;

  // ── Memberships ────────────────────────────────────────────────────────────
  private long totalMemberships;
  private long activeMemberships;
  private long pendingMemberships;
  private long suspendedMemberships;

  // ── Security ───────────────────────────────────────────────────────────────
  private ActiveSigningKeyInfo activeSigningKey;
  private long activeSigningKeys;
  private long retiredSigningKeys;
  private long revokedSigningKeys;
  private long activeSessions;
  private long expiredSessions;
  private long terminatedSessions;
  private long activeRefreshTokens;
  private long usedRefreshTokens;
  private long expiredRefreshTokens;
  private long revokedRefreshTokens;
  private long pendingAuthCodes;
  private long usedAuthCodes;
  private long expiredAuthCodes;
  private long revokedAuthCodes;
  private List<DashboardAlert> alerts;

  // ── Registration ───────────────────────────────────────────────────────────
  private long pendingEmailVerifications;
  private long expiredPendingVerifications;
  private long recentRegistrations;
  private long recentVerifications;

  // ── Topology ───────────────────────────────────────────────────────────────
  private double avgUsersPerTenant;
  private double avgAppsPerTenant;
  private double avgMembershipsPerApp;
  private long tenantsWithoutApps;
  private long tenantsWithoutUsers;

  // ── Rankings ───────────────────────────────────────────────────────────────
  private List<TenantRankEntry> topTenantsByUsers;
  private List<AppRankEntry> topAppsByMemberships;

  // ── Pending actions ────────────────────────────────────────────────────────
  private List<PendingAction> pendingActions;

  // ── Recent activity ────────────────────────────────────────────────────────
  private List<ActivityEntry> recentActivity;

  // ── Quick actions ──────────────────────────────────────────────────────────
  private List<QuickAction> quickActions;

  // ── Nested types ──────────────────────────────────────────────────────────

  public record DashboardAlert(String level, String code, String message) {}

  public record PendingAction(String type, long count, String route) {}

  public record QuickAction(String code, String label, String route) {}
}

