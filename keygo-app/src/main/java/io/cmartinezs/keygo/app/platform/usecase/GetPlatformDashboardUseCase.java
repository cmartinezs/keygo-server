package io.cmartinezs.keygo.app.platform.usecase;

import io.cmartinezs.keygo.app.platform.port.PlatformDashboardPort;
import io.cmartinezs.keygo.app.platform.port.PlatformDashboardPort.ActivityEntry;
import io.cmartinezs.keygo.app.platform.port.PlatformDashboardPort.AppRankEntry;
import io.cmartinezs.keygo.app.platform.port.PlatformDashboardPort.TenantRankEntry;
import io.cmartinezs.keygo.app.platform.port.ServiceInfoProvider;
import io.cmartinezs.keygo.app.platform.result.PlatformDashboardResult;
import io.cmartinezs.keygo.app.platform.result.PlatformDashboardResult.DashboardAlert;
import io.cmartinezs.keygo.app.platform.result.PlatformDashboardResult.PendingAction;
import io.cmartinezs.keygo.app.platform.result.PlatformDashboardResult.QuickAction;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.model.UserStatus;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Use case: retrieve a full aggregated platform dashboard in one call.
 * <p>Caso de uso: obtener el dashboard agregado de la plataforma en una sola llamada.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class GetPlatformDashboardUseCase {

  /** Window considered "recent" for newly created entities (7 days). */
  private static final int RECENT_DAYS = 7;

  /** Age threshold (days) for the active signing key to trigger a warning. */
  private static final long SIGNING_KEY_AGE_WARNING_DAYS = 30;

  private static final int TOP_RANKING_LIMIT = 5;
  private static final int RECENT_ACTIVITY_LIMIT = 10;

  // ── Status string constants (signing keys, sessions, tokens, auth codes) ──
  private static final String STATUS_ACTIVE     = "ACTIVE";
  private static final String STATUS_RETIRED    = "RETIRED";
  private static final String STATUS_REVOKED    = "REVOKED";
  private static final String STATUS_EXPIRED    = "EXPIRED";
  private static final String STATUS_TERMINATED = "TERMINATED";
  private static final String STATUS_USED       = "USED";
  // Auth codes use lowercase
  private static final String CODE_PENDING  = "pending";
  private static final String CODE_USED     = "used";
  private static final String CODE_EXPIRED  = "expired";
  private static final String CODE_REVOKED  = "revoked";

  private final PlatformDashboardPort dashboardPort;
  private final ServiceInfoProvider serviceInfoProvider;

  public GetPlatformDashboardUseCase(
      PlatformDashboardPort dashboardPort,
      ServiceInfoProvider serviceInfoProvider) {
    this.dashboardPort = dashboardPort;
    this.serviceInfoProvider = serviceInfoProvider;
  }

  /**
   * Executes the use case and returns a fully populated dashboard result.
   *
   * @return PlatformDashboardResult aggregating all platform data
   */
  public PlatformDashboardResult execute() {
    OffsetDateTime recentCutoff = OffsetDateTime.now(ZoneOffset.UTC).minusDays(RECENT_DAYS);
    Instant recentCutoffInstant = recentCutoff.toInstant();

    // ── Service info ─────────────────────────────────────────────────────────

    // ── Tenants ──────────────────────────────────────────────────────────────
    long totalTenants        = dashboardPort.countTenants();
    var  tenantStatusCounts  = dashboardPort.countTenantsByStatus();
    long activeTenants       = tenantStatusCounts.getOrDefault(TenantStatus.ACTIVE, 0L);
    long pendingTenants      = tenantStatusCounts.getOrDefault(TenantStatus.PENDING, 0L);
    long suspendedTenants    = tenantStatusCounts.getOrDefault(TenantStatus.SUSPENDED, 0L);
    long recentTenants       = dashboardPort.countTenantsCreatedAfter(recentCutoff);
    long tenantsWithoutApps  = dashboardPort.countTenantsWithoutApps();
    long tenantsWithoutUsers = dashboardPort.countTenantsWithoutUsers();

    // ── Users ─────────────────────────────────────────────────────────────────
    long totalUsers              = dashboardPort.countUsers();
    var  userStatusCounts        = dashboardPort.countUsersByStatus();
    long activeUsers             = userStatusCounts.getOrDefault(UserStatus.ACTIVE, 0L);
    long pendingUsers            = userStatusCounts.getOrDefault(UserStatus.PENDING, 0L);
    long suspendedUsers          = userStatusCounts.getOrDefault(UserStatus.SUSPENDED, 0L);
    long recentUsers             = dashboardPort.countUsersCreatedAfter(recentCutoff);
    long usersWithoutMembership  = dashboardPort.countUsersWithoutMembership();

    // ── Apps ──────────────────────────────────────────────────────────────────
    long totalApps              = dashboardPort.countApps();
    var  appStatusCounts        = dashboardPort.countAppsByStatus();
    long activeApps             = appStatusCounts.getOrDefault(ClientAppStatus.ACTIVE, 0L);
    long pendingApps            = appStatusCounts.getOrDefault(ClientAppStatus.PENDING, 0L);
    long suspendedApps          = appStatusCounts.getOrDefault(ClientAppStatus.SUSPENDED, 0L);
    var  appTypeCounts          = dashboardPort.countAppsByType();
    long publicApps             = appTypeCounts.getOrDefault(ClientType.PUBLIC, 0L);
    long confidentialApps       = appTypeCounts.getOrDefault(ClientType.CONFIDENTIAL, 0L);
    long appsWithoutRedirect    = dashboardPort.countAppsWithoutRedirectUris();

    // ── Memberships ───────────────────────────────────────────────────────────
    long totalMemberships        = dashboardPort.countMemberships();
    var  membershipStatusCounts  = dashboardPort.countMembershipsByStatus();
    long activeMemberships       = membershipStatusCounts.getOrDefault(MembershipStatus.ACTIVE, 0L);
    long pendingMemberships      = membershipStatusCounts.getOrDefault(MembershipStatus.PENDING, 0L);
    long suspendedMemberships    = membershipStatusCounts.getOrDefault(MembershipStatus.SUSPENDED, 0L);

    // ── Security ──────────────────────────────────────────────────────────────
    var activeKeyOpt         = dashboardPort.findActiveSigningKey();
    var signingKeyCounts     = dashboardPort.countSigningKeysByStatus();
    long activeSigningKeys   = signingKeyCounts.getOrDefault(STATUS_ACTIVE, 0L);
    long retiredSigningKeys  = signingKeyCounts.getOrDefault(STATUS_RETIRED, 0L);
    long revokedSigningKeys  = signingKeyCounts.getOrDefault(STATUS_REVOKED, 0L);
    var  sessionCounts       = dashboardPort.countSessionsByStatus();
    long activeSessions      = sessionCounts.getOrDefault(STATUS_ACTIVE, 0L);
    long expiredSessions     = sessionCounts.getOrDefault(STATUS_EXPIRED, 0L);
    long terminatedSessions  = sessionCounts.getOrDefault(STATUS_TERMINATED, 0L);
    var  rtCounts            = dashboardPort.countRefreshTokensByStatus();
    long activeRt            = rtCounts.getOrDefault(STATUS_ACTIVE, 0L);
    long usedRt              = rtCounts.getOrDefault(STATUS_USED, 0L);
    long expiredRt           = rtCounts.getOrDefault(STATUS_EXPIRED, 0L);
    long revokedRt           = rtCounts.getOrDefault(STATUS_REVOKED, 0L);
    var  codeCounts          = dashboardPort.countAuthCodesByStatus();
    long pendingCodes        = codeCounts.getOrDefault(CODE_PENDING, 0L);
    long usedCodes           = codeCounts.getOrDefault(CODE_USED, 0L);
    long expiredCodes        = codeCounts.getOrDefault(CODE_EXPIRED, 0L);
    long revokedCodes        = codeCounts.getOrDefault(CODE_REVOKED, 0L);

    // ── Registration ──────────────────────────────────────────────────────────
    long pendingVerifications  = dashboardPort.countPendingEmailVerifications();
    long expiredVerifications  = dashboardPort.countExpiredPendingEmailVerifications();
    long recentVerifications   = dashboardPort.countEmailVerificationsUsedAfter(recentCutoffInstant);

    // ── Rankings ──────────────────────────────────────────────────────────────
    List<TenantRankEntry> topTenants = dashboardPort.topTenantsByUserCount(TOP_RANKING_LIMIT);
    List<AppRankEntry>    topApps    = dashboardPort.topAppsByMembershipCount(TOP_RANKING_LIMIT);

    // ── Recent activity ───────────────────────────────────────────────────────
    List<ActivityEntry> recentActivity = dashboardPort.recentActivity(RECENT_ACTIVITY_LIMIT, recentCutoff);

    // ── Topology (calculated in use case) ─────────────────────────────────────
    double avgUsersPerTenant      = totalTenants > 0 ? (double) totalUsers / totalTenants : 0.0;
    double avgAppsPerTenant       = totalTenants > 0 ? (double) totalApps  / totalTenants : 0.0;
    double avgMembershipsPerApp   = totalApps    > 0 ? (double) totalMemberships / totalApps : 0.0;

    // ── Alerts (business rules applied in use case) ────────────────────────────
    List<DashboardAlert> alerts = buildAlerts(activeKeyOpt.orElse(null));

    // ── Pending actions ────────────────────────────────────────────────────────
    List<PendingAction> pendingActions = buildPendingActions(pendingTenants, pendingVerifications, usersWithoutMembership);

    // ── Quick actions (static) ────────────────────────────────────────────────
    List<QuickAction> quickActions = List.of(
        new QuickAction("CREATE_TENANT", "Crear tenant",    "/tenants/new"),
        new QuickAction("CREATE_APP",    "Registrar app",   "/apps/new"),
        new QuickAction("INVITE_USER",   "Invitar usuario", "/users/new")
    );

    return PlatformDashboardResult.builder()
        // service
        .serviceTitle(serviceInfoProvider.getTitle())
        .serviceName(serviceInfoProvider.getName())
        .serviceVersion(serviceInfoProvider.getVersion())
        .serviceEnvironment(serviceInfoProvider.getEnvironment())
        .serviceStatus(serviceInfoProvider.getStatus())
        // tenants
        .totalTenants(totalTenants)
        .activeTenants(activeTenants)
        .pendingTenants(pendingTenants)
        .suspendedTenants(suspendedTenants)
        .recentlyCreatedTenants(recentTenants)
        // users
        .totalUsers(totalUsers)
        .activeUsers(activeUsers)
        .pendingUsers(pendingUsers)
        .suspendedUsers(suspendedUsers)
        .recentlyCreatedUsers(recentUsers)
        .usersWithoutMembership(usersWithoutMembership)
        // apps
        .totalApps(totalApps)
        .activeApps(activeApps)
        .pendingApps(pendingApps)
        .suspendedApps(suspendedApps)
        .publicApps(publicApps)
        .confidentialApps(confidentialApps)
        .appsWithoutRedirectUris(appsWithoutRedirect)
        // memberships
        .totalMemberships(totalMemberships)
        .activeMemberships(activeMemberships)
        .pendingMemberships(pendingMemberships)
        .suspendedMemberships(suspendedMemberships)
        // security
        .activeSigningKey(activeKeyOpt.orElse(null))
        .activeSigningKeys(activeSigningKeys)
        .retiredSigningKeys(retiredSigningKeys)
        .revokedSigningKeys(revokedSigningKeys)
        .activeSessions(activeSessions)
        .expiredSessions(expiredSessions)
        .terminatedSessions(terminatedSessions)
        .activeRefreshTokens(activeRt)
        .usedRefreshTokens(usedRt)
        .expiredRefreshTokens(expiredRt)
        .revokedRefreshTokens(revokedRt)
        .pendingAuthCodes(pendingCodes)
        .usedAuthCodes(usedCodes)
        .expiredAuthCodes(expiredCodes)
        .revokedAuthCodes(revokedCodes)
        .alerts(alerts)
        // registration
        .pendingEmailVerifications(pendingVerifications)
        .expiredPendingVerifications(expiredVerifications)
        .recentRegistrations(recentUsers)
        .recentVerifications(recentVerifications)
        // topology
        .avgUsersPerTenant(avgUsersPerTenant)
        .avgAppsPerTenant(avgAppsPerTenant)
        .avgMembershipsPerApp(avgMembershipsPerApp)
        .tenantsWithoutApps(tenantsWithoutApps)
        .tenantsWithoutUsers(tenantsWithoutUsers)
        // rankings
        .topTenantsByUsers(topTenants)
        .topAppsByMemberships(topApps)
        // activity & actions
        .recentActivity(recentActivity)
        .pendingActions(pendingActions)
        .quickActions(quickActions)
        .build();
  }

  private List<DashboardAlert> buildAlerts(PlatformDashboardPort.ActiveSigningKeyInfo activeKey) {
    List<DashboardAlert> alerts = new ArrayList<>();
    if (activeKey == null) {
      alerts.add(new DashboardAlert("error", "NO_ACTIVE_SIGNING_KEY",
          "No active signing key found. Token issuance is not possible."));
    } else {
      long ageDays = ChronoUnit.DAYS.between(activeKey.activatedAt(), Instant.now());
      if (ageDays > SIGNING_KEY_AGE_WARNING_DAYS) {
        alerts.add(new DashboardAlert("warning", "SIGNING_KEY_AGE_HIGH",
            "Active signing key is older than " + SIGNING_KEY_AGE_WARNING_DAYS + " days"));
      }
    }
    return alerts;
  }

  private List<PendingAction> buildPendingActions(
      long pendingTenants, long pendingVerifications, long usersWithoutMembership) {
    List<PendingAction> actions = new ArrayList<>();
    if (pendingTenants > 0) {
      actions.add(new PendingAction("TENANT_APPROVAL", pendingTenants, "/tenants?status=PENDING"));
    }
    if (pendingVerifications > 0) {
      actions.add(new PendingAction("EMAIL_VERIFICATION", pendingVerifications,
          "/registration/verifications?status=PENDING"));
    }
    if (usersWithoutMembership > 0) {
      actions.add(new PendingAction("USER_WITHOUT_MEMBERSHIP", usersWithoutMembership,
          "/access/users-without-membership"));
    }
    return actions;
  }
}



