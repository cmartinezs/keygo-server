package io.cmartinezs.keygo.api.platform.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for the platform admin dashboard — aggregates all operational metrics.
 * <p>DTO de respuesta para el dashboard administrativo — agrega todas las métricas operacionales.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class PlatformDashboardData {

  private ServiceSummary service;
  private SecuritySummary security;
  private CountSummary tenants;
  private CountSummary users;
  private AppSummary apps;
  private MembershipSummary memberships;
  private RegistrationSummary registration;
  private TopologySummary topology;
  private RankingSummary rankings;
  private List<PendingActionItem> pendingActions;
  private List<ActivityItem> recentActivity;
  private List<QuickActionItem> quickActions;

  // ── Nested DTOs ───────────────────────────────────────────────────────────

  @Getter
  @Builder
  public static class ServiceSummary {
    private String title;
    private String name;
    private String version;
    private String environment;
    private String status;
  }

  @Getter
  @Builder
  public static class CountSummary {
    private long total;
    private long active;
    private long pending;
    private long suspended;
    private long recentlyCreated;
  }

  @Getter
  @Builder
  public static class AppSummary {
    private long total;
    private long active;
    private long pending;
    private long suspended;
    private long publicCount;
    private long confidentialCount;
    private long withoutRedirectUris;
  }

  @Getter
  @Builder
  public static class MembershipSummary {
    private long total;
    private long active;
    private long pending;
    private long suspended;
    private long usersWithoutMembership;
  }

  @Getter
  @Builder
  public static class SecuritySummary {
    private SigningKeySummary activeSigningKey;
    private SecurityCounts counts;
    private List<AlertItem> alerts;
  }

  @Getter
  @Builder
  public static class SigningKeySummary {
    private String kid;
    private String algorithm;
    private Instant activatedAt;
    private long ageDays;
  }

  @Getter
  @Builder
  public static class SecurityCounts {
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
    private long pendingAuthorizationCodes;
    private long usedAuthorizationCodes;
    private long expiredAuthorizationCodes;
    private long revokedAuthorizationCodes;
  }

  @Getter
  @Builder
  public static class AlertItem {
    private String level;
    private String code;
    private String message;
  }

  @Getter
  @Builder
  public static class RegistrationSummary {
    private long pendingEmailVerifications;
    private long expiredPendingVerifications;
    private long recentRegistrations;
    private long recentVerifications;
  }

  @Getter
  @Builder
  public static class TopologySummary {
    private double avgUsersPerTenant;
    private double avgAppsPerTenant;
    private double avgMembershipsPerApp;
    private long tenantsWithoutApps;
    private long tenantsWithoutUsers;
  }

  @Getter
  @Builder
  public static class RankingSummary {
    private List<RankEntry> topTenantsByUsers;
    private List<RankEntry> topAppsByMemberships;
  }

  @Getter
  @Builder
  public static class RankEntry {
    private String slug;
    private String name;
    private String tenantSlug; // only populated for app entries
    private long count;
  }

  @Getter
  @Builder
  public static class PendingActionItem {
    private String type;
    private long count;
    private String route;
  }

  @Getter
  @Builder
  public static class ActivityItem {
    private String type;
    private String label;
    private Instant occurredAt;
    private String route;
  }

  @Getter
  @Builder
  public static class QuickActionItem {
    private String code;
    private String label;
    private String route;
  }
}

