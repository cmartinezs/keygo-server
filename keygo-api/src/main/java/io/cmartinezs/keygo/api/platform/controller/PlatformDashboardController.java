package io.cmartinezs.keygo.api.platform.controller;

import io.cmartinezs.keygo.api.platform.response.PlatformDashboardData;
import io.cmartinezs.keygo.api.platform.response.PlatformDashboardData.ActivityItem;
import io.cmartinezs.keygo.api.platform.response.PlatformDashboardData.AlertItem;
import io.cmartinezs.keygo.api.platform.response.PlatformDashboardData.AppSummary;
import io.cmartinezs.keygo.api.platform.response.PlatformDashboardData.CountSummary;
import io.cmartinezs.keygo.api.platform.response.PlatformDashboardData.MembershipSummary;
import io.cmartinezs.keygo.api.platform.response.PlatformDashboardData.PendingActionItem;
import io.cmartinezs.keygo.api.platform.response.PlatformDashboardData.QuickActionItem;
import io.cmartinezs.keygo.api.platform.response.PlatformDashboardData.RankEntry;
import io.cmartinezs.keygo.api.platform.response.PlatformDashboardData.RankingSummary;
import io.cmartinezs.keygo.api.platform.response.PlatformDashboardData.RegistrationSummary;
import io.cmartinezs.keygo.api.platform.response.PlatformDashboardData.SecurityCounts;
import io.cmartinezs.keygo.api.platform.response.PlatformDashboardData.SecuritySummary;
import io.cmartinezs.keygo.api.platform.response.PlatformDashboardData.ServiceSummary;
import io.cmartinezs.keygo.api.platform.response.PlatformDashboardData.SigningKeySummary;
import io.cmartinezs.keygo.api.platform.response.PlatformDashboardData.TopologySummary;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.platform.port.PlatformDashboardPort.ActiveSigningKeyInfo;
import io.cmartinezs.keygo.app.platform.result.PlatformDashboardResult;
import io.cmartinezs.keygo.app.platform.result.PlatformDashboardResult.DashboardAlert;
import io.cmartinezs.keygo.app.platform.usecase.GetPlatformDashboardUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * REST controller for the platform admin dashboard — aggregated operational view.
 * <p>Controlador REST para el dashboard administrativo — vista operacional agregada.
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Administrative endpoints — requires ADMIN Bearer JWT")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class PlatformDashboardController {

  private final GetPlatformDashboardUseCase getDashboardUseCase;

  public PlatformDashboardController(GetPlatformDashboardUseCase getDashboardUseCase) {
    this.getDashboardUseCase = getDashboardUseCase;
  }

  /**
   * Returns the full platform dashboard in a single call.
   * <p>Retorna el dashboard completo de la plataforma en una sola llamada.
   *
   * @return 200 OK with aggregated dashboard data
   */
  @GetMapping("/platform/dashboard")
  @Operation(
      summary = "Get platform admin dashboard",
      description = "Returns a full aggregated view of the platform: service status, "
          + "security metrics, tenant/user/app/membership counts, rankings, "
          + "pending actions and recent activity. Requires ADMIN role.")
  @ApiResponse(responseCode = "200", description = "Platform dashboard retrieved successfully (code: PLATFORM_DASHBOARD_RETRIEVED)")
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "403", description = "Insufficient permissions — ADMIN role required (code: INSUFFICIENT_PERMISSIONS)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<PlatformDashboardData>> getDashboard() {
    PlatformDashboardResult result = getDashboardUseCase.execute();
    PlatformDashboardData data = toData(result);

    BaseResponse<PlatformDashboardData> response = BaseResponse.<PlatformDashboardData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.PLATFORM_DASHBOARD_RETRIEVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  // ── Mapping ───────────────────────────────────────────────────────────────

  private PlatformDashboardData toData(PlatformDashboardResult r) {
    return PlatformDashboardData.builder()
        .service(ServiceSummary.builder()
            .title(r.getServiceTitle())
            .name(r.getServiceName())
            .version(r.getServiceVersion())
            .environment(r.getServiceEnvironment())
            .status(r.getServiceStatus())
            .build())
        .security(SecuritySummary.builder()
            .activeSigningKey(toSigningKeySummary(r.getActiveSigningKey()))
            .counts(SecurityCounts.builder()
                .activeSigningKeys(r.getActiveSigningKeys())
                .retiredSigningKeys(r.getRetiredSigningKeys())
                .revokedSigningKeys(r.getRevokedSigningKeys())
                .activeSessions(r.getActiveSessions())
                .expiredSessions(r.getExpiredSessions())
                .terminatedSessions(r.getTerminatedSessions())
                .activeRefreshTokens(r.getActiveRefreshTokens())
                .usedRefreshTokens(r.getUsedRefreshTokens())
                .expiredRefreshTokens(r.getExpiredRefreshTokens())
                .revokedRefreshTokens(r.getRevokedRefreshTokens())
                .pendingAuthorizationCodes(r.getPendingAuthCodes())
                .usedAuthorizationCodes(r.getUsedAuthCodes())
                .expiredAuthorizationCodes(r.getExpiredAuthCodes())
                .revokedAuthorizationCodes(r.getRevokedAuthCodes())
                .build())
            .alerts(toAlertItems(r.getAlerts()))
            .build())
        .tenants(CountSummary.builder()
            .total(r.getTotalTenants())
            .active(r.getActiveTenants())
            .pending(r.getPendingTenants())
            .suspended(r.getSuspendedTenants())
            .recentlyCreated(r.getRecentlyCreatedTenants())
            .build())
        .users(CountSummary.builder()
            .total(r.getTotalUsers())
            .active(r.getActiveUsers())
            .pending(r.getPendingUsers())
            .suspended(r.getSuspendedUsers())
            .recentlyCreated(r.getRecentlyCreatedUsers())
            .build())
        .apps(AppSummary.builder()
            .total(r.getTotalApps())
            .active(r.getActiveApps())
            .pending(r.getPendingApps())
            .suspended(r.getSuspendedApps())
            .publicCount(r.getPublicApps())
            .confidentialCount(r.getConfidentialApps())
            .withoutRedirectUris(r.getAppsWithoutRedirectUris())
            .build())
        .memberships(MembershipSummary.builder()
            .total(r.getTotalMemberships())
            .active(r.getActiveMemberships())
            .pending(r.getPendingMemberships())
            .suspended(r.getSuspendedMemberships())
            .usersWithoutMembership(r.getUsersWithoutMembership())
            .build())
        .registration(RegistrationSummary.builder()
            .pendingEmailVerifications(r.getPendingEmailVerifications())
            .expiredPendingVerifications(r.getExpiredPendingVerifications())
            .recentRegistrations(r.getRecentRegistrations())
            .recentVerifications(r.getRecentVerifications())
            .build())
        .topology(TopologySummary.builder()
            .avgUsersPerTenant(r.getAvgUsersPerTenant())
            .avgAppsPerTenant(r.getAvgAppsPerTenant())
            .avgMembershipsPerApp(r.getAvgMembershipsPerApp())
            .tenantsWithoutApps(r.getTenantsWithoutApps())
            .tenantsWithoutUsers(r.getTenantsWithoutUsers())
            .build())
        .rankings(RankingSummary.builder()
            .topTenantsByUsers(r.getTopTenantsByUsers().stream()
                .map(e -> RankEntry.builder()
                    .slug(e.tenantSlug())
                    .name(e.tenantName())
                    .count(e.count())
                    .build())
                .toList())
            .topAppsByMemberships(r.getTopAppsByMemberships().stream()
                .map(e -> RankEntry.builder()
                    .slug(e.clientId())
                    .name(e.appName())
                    .tenantSlug(e.tenantSlug())
                    .count(e.count())
                    .build())
                .toList())
            .build())
        .pendingActions(r.getPendingActions().stream()
            .map(a -> PendingActionItem.builder()
                .type(a.type())
                .count(a.count())
                .route(a.route())
                .build())
            .toList())
        .recentActivity(r.getRecentActivity().stream()
            .map(a -> ActivityItem.builder()
                .type(a.type())
                .label(a.label())
                .occurredAt(a.occurredAt().toInstant())
                .route(a.route())
                .build())
            .toList())
        .quickActions(r.getQuickActions().stream()
            .map(q -> QuickActionItem.builder()
                .code(q.code())
                .label(q.label())
                .route(q.route())
                .build())
            .toList())
        .build();
  }

  private SigningKeySummary toSigningKeySummary(ActiveSigningKeyInfo key) {
    if (key == null) {
      return null;
    }
    long ageDays = ChronoUnit.DAYS.between(key.activatedAt(), Instant.now());
    return SigningKeySummary.builder()
        .kid(key.kid())
        .algorithm(key.algorithm())
        .activatedAt(key.activatedAt())
        .ageDays(ageDays)
        .build();
  }

  private List<AlertItem> toAlertItems(List<DashboardAlert> alerts) {
    if (alerts == null) return List.of();
    return alerts.stream()
        .map(a -> AlertItem.builder()
            .level(a.level())
            .code(a.code())
            .message(a.message())
            .build())
        .toList();
  }
}

