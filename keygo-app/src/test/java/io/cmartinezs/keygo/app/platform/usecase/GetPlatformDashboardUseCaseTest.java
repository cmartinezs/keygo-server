package io.cmartinezs.keygo.app.platform.usecase;

import io.cmartinezs.keygo.app.platform.port.PlatformDashboardPort;
import io.cmartinezs.keygo.app.platform.port.PlatformDashboardPort.ActiveSigningKeyInfo;
import io.cmartinezs.keygo.app.platform.port.PlatformDashboardPort.ActivityEntry;
import io.cmartinezs.keygo.app.platform.port.PlatformDashboardPort.AppRankEntry;
import io.cmartinezs.keygo.app.platform.port.PlatformDashboardPort.TenantRankEntry;
import io.cmartinezs.keygo.app.platform.port.ServiceInfoProvider;
import io.cmartinezs.keygo.app.platform.result.PlatformDashboardResult;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetPlatformDashboardUseCaseTest {

  @Mock private PlatformDashboardPort dashboardPort;
  @Mock private ServiceInfoProvider serviceInfoProvider;

  private GetPlatformDashboardUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetPlatformDashboardUseCase(dashboardPort, serviceInfoProvider);
    stubDefaults();
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private void stubDefaults() {
    // service info
    when(serviceInfoProvider.getTitle()).thenReturn("KeyGo Server");
    when(serviceInfoProvider.getName()).thenReturn("keygo-server");
    when(serviceInfoProvider.getVersion()).thenReturn("1.0-SNAPSHOT");
    when(serviceInfoProvider.getEnvironment()).thenReturn("local");
    when(serviceInfoProvider.getStatus()).thenReturn("UP");

    // tenants
    when(dashboardPort.countTenants()).thenReturn(10L);
    when(dashboardPort.countTenantsByStatus()).thenReturn(Map.of(
        TenantStatus.ACTIVE, 8L,
        TenantStatus.PENDING, 1L,
        TenantStatus.SUSPENDED, 1L));
    when(dashboardPort.countTenantsCreatedAfter(any())).thenReturn(2L);
    when(dashboardPort.countTenantsWithoutApps()).thenReturn(0L);
    when(dashboardPort.countTenantsWithoutUsers()).thenReturn(0L);

    // users
    when(dashboardPort.countUsers()).thenReturn(20L);
    when(dashboardPort.countUsersByStatus()).thenReturn(Map.of(
        UserStatus.ACTIVE, 18L,
        UserStatus.PENDING, 1L,
        UserStatus.SUSPENDED, 1L));
    when(dashboardPort.countUsersCreatedAfter(any())).thenReturn(3L);
    when(dashboardPort.countUsersWithoutMembership()).thenReturn(2L);

    // apps
    when(dashboardPort.countApps()).thenReturn(15L);
    when(dashboardPort.countAppsByStatus()).thenReturn(Map.of(
        ClientAppStatus.ACTIVE, 13L,
        ClientAppStatus.PENDING, 1L,
        ClientAppStatus.SUSPENDED, 1L));
    when(dashboardPort.countAppsByType()).thenReturn(Map.of(
        ClientType.PUBLIC, 8L,
        ClientType.CONFIDENTIAL, 7L));
    when(dashboardPort.countAppsWithoutRedirectUris()).thenReturn(1L);
    when(dashboardPort.countAppsCreatedAfter(any())).thenReturn(2L);

    // memberships
    when(dashboardPort.countMemberships()).thenReturn(30L);
    when(dashboardPort.countMembershipsByStatus()).thenReturn(Map.of(
        MembershipStatus.ACTIVE, 27L,
        MembershipStatus.PENDING, 2L,
        MembershipStatus.SUSPENDED, 1L));

    // security — empty maps → getOrDefault returns 0L
    when(dashboardPort.findActiveSigningKey()).thenReturn(Optional.empty());
    when(dashboardPort.countSigningKeysByStatus()).thenReturn(Map.of());
    when(dashboardPort.countSessionsByStatus()).thenReturn(Map.of());
    when(dashboardPort.countRefreshTokensByStatus()).thenReturn(Map.of());
    when(dashboardPort.countAuthCodesByStatus()).thenReturn(Map.of());

    // registration
    when(dashboardPort.countPendingEmailVerifications()).thenReturn(3L);
    when(dashboardPort.countExpiredPendingEmailVerifications()).thenReturn(1L);
    when(dashboardPort.countEmailVerificationsUsedAfter(any())).thenReturn(5L);

    // rankings
    when(dashboardPort.topTenantsByUserCount(anyInt())).thenReturn(List.of(
        new TenantRankEntry("keygo", "KeyGo", 10L)));
    when(dashboardPort.topAppsByMembershipCount(anyInt())).thenReturn(List.of(
        new AppRankEntry("keygo-ui", "KeyGo UI", "keygo", 25L)));

    // activity
    when(dashboardPort.recentActivity(anyInt(), any())).thenReturn(List.of());
  }

  // ── Tests ─────────────────────────────────────────────────────────────────

  @Test
  void execute_shouldPropagateServiceInfo() {
    // When
    PlatformDashboardResult result = useCase.execute();

    // Then
    assertThat(result.getServiceTitle()).isEqualTo("KeyGo Server");
    assertThat(result.getServiceName()).isEqualTo("keygo-server");
    assertThat(result.getServiceEnvironment()).isEqualTo("local");
    assertThat(result.getServiceStatus()).isEqualTo("UP");
  }

  @Test
  void execute_shouldPropagateCounters() {
    // When
    PlatformDashboardResult result = useCase.execute();

    // Then — tenants
    assertThat(result.getTotalTenants()).isEqualTo(10L);
    assertThat(result.getActiveTenants()).isEqualTo(8L);
    assertThat(result.getPendingTenants()).isEqualTo(1L);

    // Then — users
    assertThat(result.getTotalUsers()).isEqualTo(20L);
    assertThat(result.getUsersWithoutMembership()).isEqualTo(2L);

    // Then — apps
    assertThat(result.getPublicApps()).isEqualTo(8L);
    assertThat(result.getConfidentialApps()).isEqualTo(7L);
    assertThat(result.getAppsWithoutRedirectUris()).isEqualTo(1L);

    // Then — memberships
    assertThat(result.getTotalMemberships()).isEqualTo(30L);
    assertThat(result.getActiveMemberships()).isEqualTo(27L);
  }

  @Test
  void execute_withNoActiveSigningKey_shouldGenerateErrorAlert() {
    // Given — findActiveSigningKey returns empty (default stub)

    // When
    PlatformDashboardResult result = useCase.execute();

    // Then
    assertThat(result.getAlerts()).hasSize(1);
    assertThat(result.getAlerts().get(0).code()).isEqualTo("NO_ACTIVE_SIGNING_KEY");
    assertThat(result.getAlerts().get(0).level()).isEqualTo("error");
  }

  @Test
  void execute_withRecentActiveSigningKey_shouldHaveNoAlerts() {
    // Given — key activated 5 days ago (within the 30-day threshold)
    Instant fiveDaysAgo = Instant.now().minus(5, ChronoUnit.DAYS);
    when(dashboardPort.findActiveSigningKey())
        .thenReturn(Optional.of(new ActiveSigningKeyInfo("kid-1", "RS256", fiveDaysAgo)));

    // When
    PlatformDashboardResult result = useCase.execute();

    // Then
    assertThat(result.getAlerts()).isEmpty();
    assertThat(result.getActiveSigningKey()).isNotNull();
    assertThat(result.getActiveSigningKey().kid()).isEqualTo("kid-1");
  }

  @Test
  void execute_withOldActiveSigningKey_shouldGenerateWarningAlert() {
    // Given — key activated 45 days ago (exceeds 30-day threshold)
    Instant oldActivatedAt = Instant.now().minus(45, ChronoUnit.DAYS);
    when(dashboardPort.findActiveSigningKey())
        .thenReturn(Optional.of(new ActiveSigningKeyInfo("kid-old", "RS256", oldActivatedAt)));

    // When
    PlatformDashboardResult result = useCase.execute();

    // Then
    assertThat(result.getAlerts()).hasSize(1);
    assertThat(result.getAlerts().get(0).code()).isEqualTo("SIGNING_KEY_AGE_HIGH");
    assertThat(result.getAlerts().get(0).level()).isEqualTo("warning");
  }

  @Test
  void execute_shouldCalculateTopologyAvoidingDivisionByZero() {
    // Given — zero tenants and zero apps
    when(dashboardPort.countTenants()).thenReturn(0L);
    when(dashboardPort.countApps()).thenReturn(0L);

    // When
    PlatformDashboardResult result = useCase.execute();

    // Then — no division by zero
    assertThat(result.getAvgUsersPerTenant()).isEqualTo(0.0);
    assertThat(result.getAvgAppsPerTenant()).isEqualTo(0.0);
    assertThat(result.getAvgMembershipsPerApp()).isEqualTo(0.0);
  }

  @Test
  void execute_shouldCalculateTopologyCorrectly() {
    // Given
    when(dashboardPort.countTenants()).thenReturn(10L);
    when(dashboardPort.countUsers()).thenReturn(20L);
    when(dashboardPort.countApps()).thenReturn(5L);
    when(dashboardPort.countMemberships()).thenReturn(15L);

    // When
    PlatformDashboardResult result = useCase.execute();

    // Then
    assertThat(result.getAvgUsersPerTenant()).isEqualTo(2.0);
    assertThat(result.getAvgAppsPerTenant()).isEqualTo(0.5);
    assertThat(result.getAvgMembershipsPerApp()).isEqualTo(3.0);
  }

  @Test
  void execute_shouldGeneratePendingActionsForNonZeroValues() {
    // Given — override maps to inject specific pending counts
    when(dashboardPort.countTenantsByStatus()).thenReturn(Map.of(
        TenantStatus.ACTIVE, 5L,
        TenantStatus.PENDING, 3L,
        TenantStatus.SUSPENDED, 0L));
    when(dashboardPort.countPendingEmailVerifications()).thenReturn(5L);
    when(dashboardPort.countUsersWithoutMembership()).thenReturn(2L);

    // When
    PlatformDashboardResult result = useCase.execute();

    // Then
    assertThat(result.getPendingActions()).hasSize(3);
    assertThat(result.getPendingActions())
        .extracting(a -> a.type())
        .containsExactlyInAnyOrder("TENANT_APPROVAL", "EMAIL_VERIFICATION", "USER_WITHOUT_MEMBERSHIP");
  }

  @Test
  void execute_shouldNotGeneratePendingActionsWhenAllZero() {
    // Given — override to zero pending counts (PENDING absent → getOrDefault = 0)
    when(dashboardPort.countTenantsByStatus()).thenReturn(Map.of(
        TenantStatus.ACTIVE, 8L,
        TenantStatus.SUSPENDED, 1L));
    when(dashboardPort.countPendingEmailVerifications()).thenReturn(0L);
    when(dashboardPort.countUsersWithoutMembership()).thenReturn(0L);

    // When
    PlatformDashboardResult result = useCase.execute();

    // Then
    assertThat(result.getPendingActions()).isEmpty();
  }

  @Test
  void execute_shouldAlwaysReturnThreeQuickActions() {
    // When
    PlatformDashboardResult result = useCase.execute();

    // Then
    assertThat(result.getQuickActions()).hasSize(3);
    assertThat(result.getQuickActions())
        .extracting(q -> q.code())
        .containsExactly("CREATE_TENANT", "CREATE_APP", "INVITE_USER");
  }

  @Test
  void execute_shouldPropagateRankings() {
    // When
    PlatformDashboardResult result = useCase.execute();

    // Then
    assertThat(result.getTopTenantsByUsers()).hasSize(1);
    assertThat(result.getTopTenantsByUsers().get(0).tenantSlug()).isEqualTo("keygo");
    assertThat(result.getTopAppsByMemberships()).hasSize(1);
    assertThat(result.getTopAppsByMemberships().get(0).clientId()).isEqualTo("keygo-ui");
  }
}

