package io.cmartinezs.keygo.app.platform.port;

import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.model.UserStatus;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Port OUT — provides all data needed by the platform dashboard use case.
 * <p>Puerto de salida — provee todos los datos necesarios para el caso de uso del dashboard.
 *
 * @author cmartinezs
 * @version 1.0
 */
public interface PlatformDashboardPort {

  // ── Tenants ────────────────────────────────────────────────────────────────

  /** Returns counts per TenantStatus (single GROUP BY query). */
  Map<TenantStatus, Long> countTenantsByStatus();

  long countTenants();

  long countTenantsCreatedAfter(OffsetDateTime cutoff);

  long countTenantsWithoutApps();

  long countTenantsWithoutUsers();

  // ── Users ──────────────────────────────────────────────────────────────────

  long countUsers();

  /** Returns counts per UserStatus (single GROUP BY query). */
  Map<UserStatus, Long> countUsersByStatus();

  long countUsersCreatedAfter(OffsetDateTime cutoff);

  long countUsersWithoutMembership();

  // ── Apps ───────────────────────────────────────────────────────────────────

  long countApps();

  /** Returns counts per ClientAppStatus (single GROUP BY query). */
  Map<ClientAppStatus, Long> countAppsByStatus();

  /** Returns counts per ClientType (single GROUP BY query). */
  Map<ClientType, Long> countAppsByType();

  long countAppsWithoutRedirectUris();

  long countAppsCreatedAfter(OffsetDateTime cutoff);

  // ── Memberships ────────────────────────────────────────────────────────────

  long countMemberships();

  /** Returns counts per MembershipStatus (single GROUP BY query). */
  Map<MembershipStatus, Long> countMembershipsByStatus();

  // ── Security ───────────────────────────────────────────────────────────────

  Optional<ActiveSigningKeyInfo> findActiveSigningKey();

  /** Returns counts per signing key status string (single GROUP BY query). */
  Map<String, Long> countSigningKeysByStatus();

  /** Returns counts per session status string (single GROUP BY query). */
  Map<String, Long> countSessionsByStatus();

  /** Returns counts per refresh token status string (single GROUP BY query). */
  Map<String, Long> countRefreshTokensByStatus();

  /** Returns counts per auth code status string (single GROUP BY query). */
  Map<String, Long> countAuthCodesByStatus();

  // ── Registration ───────────────────────────────────────────────────────────

  long countPendingEmailVerifications();

  long countExpiredPendingEmailVerifications();

  long countEmailVerificationsUsedAfter(Instant cutoff);

  // ── Rankings ───────────────────────────────────────────────────────────────

  List<TenantRankEntry> topTenantsByUserCount(int limit);

  List<AppRankEntry> topAppsByMembershipCount(int limit);

  // ── Recent activity ────────────────────────────────────────────────────────

  List<ActivityEntry> recentActivity(int limit, OffsetDateTime since);

  // ── Nested result types ────────────────────────────────────────────────────

  record ActiveSigningKeyInfo(String kid, String algorithm, Instant activatedAt) {}

  record TenantRankEntry(String tenantSlug, String tenantName, long count) {}

  record AppRankEntry(String clientId, String appName, String tenantSlug, long count) {}

  record ActivityEntry(String type, String label, OffsetDateTime occurredAt, String route) {}
}

