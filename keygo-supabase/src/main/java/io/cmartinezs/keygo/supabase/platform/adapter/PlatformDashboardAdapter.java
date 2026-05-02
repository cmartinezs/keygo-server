package io.cmartinezs.keygo.supabase.platform.adapter;

import io.cmartinezs.keygo.app.platform.port.PlatformDashboardPort;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.supabase.auth.repository.AuthorizationCodeJpaRepository;
import io.cmartinezs.keygo.supabase.auth.repository.RefreshTokenJpaRepository;
import io.cmartinezs.keygo.supabase.auth.repository.SessionJpaRepository;
import io.cmartinezs.keygo.supabase.auth.repository.SigningKeyJpaRepository;
import io.cmartinezs.keygo.supabase.clientapp.repository.ClientAppJpaRepository;
import io.cmartinezs.keygo.supabase.membership.repository.MembershipJpaRepository;
import io.cmartinezs.keygo.supabase.tenant.repository.TenantJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.VerificationCodeJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA adapter for PlatformDashboardPort.
 * <p>Adaptador JPA para PlatformDashboardPort.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Component
public class PlatformDashboardAdapter implements PlatformDashboardPort {

  private final TenantJpaRepository tenantRepo;
  private final TenantUserJpaRepository userRepo;
  private final ClientAppJpaRepository appRepo;
  private final MembershipJpaRepository membershipRepo;
  private final SigningKeyJpaRepository signingKeyRepo;
  private final SessionJpaRepository sessionRepo;
  private final RefreshTokenJpaRepository refreshTokenRepo;
  private final AuthorizationCodeJpaRepository authCodeRepo;
  private final VerificationCodeJpaRepository verificationCodeRepo;

  public PlatformDashboardAdapter(
      TenantJpaRepository tenantRepo,
      TenantUserJpaRepository userRepo,
      ClientAppJpaRepository appRepo,
      MembershipJpaRepository membershipRepo,
      SigningKeyJpaRepository signingKeyRepo,
      SessionJpaRepository sessionRepo,
      RefreshTokenJpaRepository refreshTokenRepo,
      AuthorizationCodeJpaRepository authCodeRepo,
      VerificationCodeJpaRepository verificationCodeRepo) {
    this.tenantRepo = tenantRepo;
    this.userRepo = userRepo;
    this.appRepo = appRepo;
    this.membershipRepo = membershipRepo;
    this.signingKeyRepo = signingKeyRepo;
    this.sessionRepo = sessionRepo;
    this.refreshTokenRepo = refreshTokenRepo;
    this.authCodeRepo = authCodeRepo;
    this.verificationCodeRepo = verificationCodeRepo;
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  /**
   * Convierte el resultado de una query GROUP BY en un Map tipado.
   * Cada fila debe ser [key (enum/object), count (Number)].
   */
  @SuppressWarnings("unchecked")
  private <K> Map<K, Long> toCountMap(List<Object[]> rows) {
    return rows.stream().collect(Collectors.toMap(
        row -> (K) row[0],
        row -> ((Number) row[1]).longValue()
    ));
  }

  /**
   * Convierte el resultado de una query GROUP BY en un Map<String, Long>.
   * Útil para entidades cuyo status es String (sessions, tokens, auth codes, signing keys).
   */
  private Map<String, Long> toStringCountMap(List<Object[]> rows) {
    return rows.stream().collect(Collectors.toMap(
        row -> row[0].toString(),
        row -> ((Number) row[1]).longValue()
    ));
  }

  // ── Tenants ────────────────────────────────────────────────────────────────

  @Override
  public long countTenants() {
    return tenantRepo.count();
  }

  @Override
  public Map<TenantStatus, Long> countTenantsByStatus() {
    return toCountMap(tenantRepo.countGroupByStatus());
  }

  @Override
  public long countTenantsCreatedAfter(OffsetDateTime cutoff) {
    return tenantRepo.countByCreatedAtAfter(cutoff);
  }

  @Override
  public long countTenantsWithoutApps() {
    return tenantRepo.countTenantsWithoutApps();
  }

  @Override
  public long countTenantsWithoutUsers() {
    return tenantRepo.countTenantsWithoutUsers();
  }

  // ── Users ──────────────────────────────────────────────────────────────────

  @Override
  public long countUsers() {
    return userRepo.count();
  }

  @Override
  public Map<UserStatus, Long> countUsersByStatus() {
    return toCountMap(userRepo.countGroupByStatus());
  }

  @Override
  public long countUsersCreatedAfter(OffsetDateTime cutoff) {
    return userRepo.countByCreatedAtAfter(cutoff);
  }

  @Override
  public long countUsersWithoutMembership() {
    return userRepo.countUsersWithoutMembership();
  }

  // ── Apps ───────────────────────────────────────────────────────────────────

  @Override
  public long countApps() {
    return appRepo.count();
  }

  @Override
  public Map<ClientAppStatus, Long> countAppsByStatus() {
    return toCountMap(appRepo.countGroupByStatus());
  }

  @Override
  public Map<ClientType, Long> countAppsByType() {
    return toCountMap(appRepo.countGroupByType());
  }

  @Override
  public long countAppsWithoutRedirectUris() {
    return appRepo.countAppsWithoutRedirectUris();
  }

  @Override
  public long countAppsCreatedAfter(OffsetDateTime cutoff) {
    return appRepo.countByCreatedAtAfter(cutoff);
  }

  // ── Memberships ────────────────────────────────────────────────────────────

  @Override
  public long countMemberships() {
    return membershipRepo.count();
  }

  @Override
  public Map<MembershipStatus, Long> countMembershipsByStatus() {
    return toCountMap(membershipRepo.countGroupByStatus());
  }

  // ── Security ───────────────────────────────────────────────────────────────

  @Override
  public Optional<ActiveSigningKeyInfo> findActiveSigningKey() {
    // Busca primero clave global activa (tenant IS NULL) para el dashboard de plataforma
    return signingKeyRepo.findFirstByTenantIsNullAndStatus("ACTIVE")
        .map(k -> new ActiveSigningKeyInfo(k.getKid(), k.getAlgorithm(), k.getActivatedAt()));
  }

  @Override
  public Map<String, Long> countSigningKeysByStatus() {
    return toStringCountMap(signingKeyRepo.countGroupByStatus());
  }

  @Override
  public Map<String, Long> countSessionsByStatus() {
    return toStringCountMap(sessionRepo.countGroupByStatus());
  }

  @Override
  public Map<String, Long> countRefreshTokensByStatus() {
    return toStringCountMap(refreshTokenRepo.countGroupByStatus());
  }

  @Override
  public Map<String, Long> countAuthCodesByStatus() {
    return toStringCountMap(authCodeRepo.countGroupByStatus());
  }

  // ── Registration ───────────────────────────────────────────────────────────

  @Override
  public long countPendingEmailVerifications() {
    return verificationCodeRepo.countPendingByPurpose("EMAIL_VERIFICATION", Instant.now());
  }

  @Override
  public long countExpiredPendingEmailVerifications() {
    return verificationCodeRepo.countExpiredPendingByPurpose("EMAIL_VERIFICATION", Instant.now());
  }

  @Override
  public long countEmailVerificationsUsedAfter(Instant cutoff) {
    return verificationCodeRepo.countByUsedAtAfter(cutoff);
  }

  // ── Rankings ───────────────────────────────────────────────────────────────

  @Override
  public List<TenantRankEntry> topTenantsByUserCount(int limit) {
    return tenantRepo.findTopTenantsByUserCount(limit).stream()
        .map(row -> new TenantRankEntry(
            (String) row[0],
            (String) row[1],
            ((Number) row[2]).longValue()))
        .toList();
  }

  @Override
  public List<AppRankEntry> topAppsByMembershipCount(int limit) {
    return appRepo.findTopAppsByMembershipCount(limit).stream()
        .map(row -> new AppRankEntry(
            (String) row[0],
            (String) row[1],
            (String) row[2],
            ((Number) row[3]).longValue()))
        .toList();
  }

  // ── Recent activity ────────────────────────────────────────────────────────

  @Override
  public List<ActivityEntry> recentActivity(int limit, OffsetDateTime since) {
    List<ActivityEntry> entries = new ArrayList<>();

    // Recent tenants created
    tenantRepo.findAll().stream()
        .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(since))
        .forEach(t -> entries.add(new ActivityEntry(
            "TENANT_CREATED",
            "Tenant " + t.getName() + " created",
            t.getCreatedAt(),
            "/tenants/" + t.getSlug())));

    // Recent apps created (via client_apps.created_at)
    appRepo.findAll().stream()
        .filter(a -> a.getCreatedAt() != null && a.getCreatedAt().isAfter(since))
        .forEach(a -> entries.add(new ActivityEntry(
            "CLIENT_APP_CREATED",
            "App " + a.getName() + " created",
            a.getCreatedAt(),
            "/apps/" + a.getClientId())));

    return entries.stream()
        .sorted(Comparator.comparing(ActivityEntry::occurredAt).reversed())
        .limit(limit)
        .toList();
  }
}

