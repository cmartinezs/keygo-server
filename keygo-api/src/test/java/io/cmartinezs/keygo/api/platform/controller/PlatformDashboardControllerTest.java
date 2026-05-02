package io.cmartinezs.keygo.api.platform.controller;

import io.cmartinezs.keygo.api.platform.response.PlatformDashboardData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.platform.port.PlatformDashboardPort.ActiveSigningKeyInfo;
import io.cmartinezs.keygo.app.platform.result.PlatformDashboardResult;
import io.cmartinezs.keygo.app.platform.result.PlatformDashboardResult.DashboardAlert;
import io.cmartinezs.keygo.app.platform.result.PlatformDashboardResult.PendingAction;
import io.cmartinezs.keygo.app.platform.result.PlatformDashboardResult.QuickAction;
import io.cmartinezs.keygo.app.platform.usecase.GetPlatformDashboardUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformDashboardControllerTest {

  @Mock
  private GetPlatformDashboardUseCase getDashboardUseCase;

  private PlatformDashboardController controller;

  @BeforeEach
  void setUp() {
    controller = new PlatformDashboardController(getDashboardUseCase);
  }

  private PlatformDashboardResult buildFullResult() {
    Instant activatedAt = Instant.now().minusSeconds(3600);
    return PlatformDashboardResult.builder()
        // service
        .serviceTitle("KeyGo Server")
        .serviceName("keygo-server")
        .serviceVersion("1.0-SNAPSHOT")
        .serviceEnvironment("local")
        .serviceStatus("UP")
        // tenants
        .totalTenants(10L).activeTenants(8L).pendingTenants(1L).suspendedTenants(1L).recentlyCreatedTenants(2L)
        // users
        .totalUsers(20L).activeUsers(18L).pendingUsers(1L).suspendedUsers(1L).recentlyCreatedUsers(3L)
        .usersWithoutMembership(2L)
        // apps
        .totalApps(15L).activeApps(13L).pendingApps(1L).suspendedApps(1L)
        .publicApps(8L).confidentialApps(7L).appsWithoutRedirectUris(1L)
        // memberships
        .totalMemberships(30L).activeMemberships(27L).pendingMemberships(2L).suspendedMemberships(1L)
        // security
        .activeSigningKey(new ActiveSigningKeyInfo("kid-1", "RS256", activatedAt))
        .activeSigningKeys(1L).retiredSigningKeys(2L).revokedSigningKeys(0L)
        .activeSessions(50L).expiredSessions(5L).terminatedSessions(2L)
        .activeRefreshTokens(45L).usedRefreshTokens(100L).expiredRefreshTokens(3L).revokedRefreshTokens(1L)
        .pendingAuthCodes(2L).usedAuthCodes(80L).expiredAuthCodes(3L).revokedAuthCodes(0L)
        .alerts(List.of())
        // registration
        .pendingEmailVerifications(3L).expiredPendingVerifications(1L)
        .recentRegistrations(3L).recentVerifications(5L)
        // topology
        .avgUsersPerTenant(2.0).avgAppsPerTenant(1.5).avgMembershipsPerApp(2.0)
        .tenantsWithoutApps(0L).tenantsWithoutUsers(0L)
        // rankings
        .topTenantsByUsers(List.of())
        .topAppsByMemberships(List.of())
        // actions
        .pendingActions(List.of(new PendingAction("TENANT_APPROVAL", 1L, "/tenants?status=PENDING")))
        .recentActivity(List.of())
        .quickActions(List.of(
            new QuickAction("CREATE_TENANT", "Crear tenant", "/tenants/new"),
            new QuickAction("CREATE_APP", "Registrar app", "/apps/new"),
            new QuickAction("INVITE_USER", "Invitar usuario", "/users/new")))
        .build();
  }

  @Test
  void getDashboard_shouldReturn200() {
    // Given
    when(getDashboardUseCase.execute()).thenReturn(buildFullResult());

    // When
    ResponseEntity<BaseResponse<PlatformDashboardData>> response = controller.getDashboard();

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void getDashboard_shouldReturnSuccessResponseCode() {
    // Given
    when(getDashboardUseCase.execute()).thenReturn(buildFullResult());

    // When
    ResponseEntity<BaseResponse<PlatformDashboardData>> response = controller.getDashboard();

    // Then
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getSuccess()).isNotNull();
    assertThat(response.getBody().getSuccess().getCode())
        .isEqualTo(ResponseCode.PLATFORM_DASHBOARD_RETRIEVED.getCode());
  }

  @Test
  void getDashboard_shouldMapServiceSummaryCorrectly() {
    // Given
    when(getDashboardUseCase.execute()).thenReturn(buildFullResult());

    // When
    ResponseEntity<BaseResponse<PlatformDashboardData>> response = controller.getDashboard();

    // Then
    PlatformDashboardData data = response.getBody().getData();
    assertThat(data.getService().getTitle()).isEqualTo("KeyGo Server");
    assertThat(data.getService().getEnvironment()).isEqualTo("local");
    assertThat(data.getService().getStatus()).isEqualTo("UP");
  }

  @Test
  void getDashboard_shouldMapSecuritySummaryCorrectly() {
    // Given
    when(getDashboardUseCase.execute()).thenReturn(buildFullResult());

    // When
    ResponseEntity<BaseResponse<PlatformDashboardData>> response = controller.getDashboard();

    // Then
    PlatformDashboardData data = response.getBody().getData();
    assertThat(data.getSecurity().getActiveSigningKey()).isNotNull();
    assertThat(data.getSecurity().getActiveSigningKey().getKid()).isEqualTo("kid-1");
    assertThat(data.getSecurity().getActiveSigningKey().getAlgorithm()).isEqualTo("RS256");
    assertThat(data.getSecurity().getCounts().getActiveSessions()).isEqualTo(50L);
    assertThat(data.getSecurity().getAlerts()).isEmpty();
  }

  @Test
  void getDashboard_shouldMapTenantCountsCorrectly() {
    // Given
    when(getDashboardUseCase.execute()).thenReturn(buildFullResult());

    // When
    ResponseEntity<BaseResponse<PlatformDashboardData>> response = controller.getDashboard();

    // Then
    PlatformDashboardData data = response.getBody().getData();
    assertThat(data.getTenants().getTotal()).isEqualTo(10L);
    assertThat(data.getTenants().getActive()).isEqualTo(8L);
    assertThat(data.getTenants().getPending()).isEqualTo(1L);
    assertThat(data.getTenants().getRecentlyCreated()).isEqualTo(2L);
  }

  @Test
  void getDashboard_shouldMapTopologyCorrectly() {
    // Given
    when(getDashboardUseCase.execute()).thenReturn(buildFullResult());

    // When
    ResponseEntity<BaseResponse<PlatformDashboardData>> response = controller.getDashboard();

    // Then
    PlatformDashboardData data = response.getBody().getData();
    assertThat(data.getTopology().getAvgUsersPerTenant()).isEqualTo(2.0);
    assertThat(data.getTopology().getAvgAppsPerTenant()).isEqualTo(1.5);
  }

  @Test
  void getDashboard_shouldMapPendingActionsCorrectly() {
    // Given
    when(getDashboardUseCase.execute()).thenReturn(buildFullResult());

    // When
    ResponseEntity<BaseResponse<PlatformDashboardData>> response = controller.getDashboard();

    // Then
    PlatformDashboardData data = response.getBody().getData();
    assertThat(data.getPendingActions()).hasSize(1);
    assertThat(data.getPendingActions().get(0).getType()).isEqualTo("TENANT_APPROVAL");
    assertThat(data.getPendingActions().get(0).getCount()).isEqualTo(1L);
  }

  @Test
  void getDashboard_shouldMapQuickActionsCorrectly() {
    // Given
    when(getDashboardUseCase.execute()).thenReturn(buildFullResult());

    // When
    ResponseEntity<BaseResponse<PlatformDashboardData>> response = controller.getDashboard();

    // Then
    PlatformDashboardData data = response.getBody().getData();
    assertThat(data.getQuickActions()).hasSize(3);
    assertThat(data.getQuickActions())
        .extracting(PlatformDashboardData.QuickActionItem::getCode)
        .containsExactly("CREATE_TENANT", "CREATE_APP", "INVITE_USER");
  }

  @Test
  void getDashboard_withNullActiveSigningKey_shouldMapNullSummary() {
    // Given — result with no active signing key
    PlatformDashboardResult result = PlatformDashboardResult.builder()
        .serviceTitle("KeyGo Server").serviceName("keygo-server")
        .serviceVersion("1.0-SNAPSHOT").serviceEnvironment("local").serviceStatus("UP")
        .totalTenants(10L).activeTenants(8L).pendingTenants(1L).suspendedTenants(1L).recentlyCreatedTenants(2L)
        .totalUsers(20L).activeUsers(18L).pendingUsers(1L).suspendedUsers(1L).recentlyCreatedUsers(3L)
        .usersWithoutMembership(2L)
        .totalApps(15L).activeApps(13L).pendingApps(1L).suspendedApps(1L)
        .publicApps(8L).confidentialApps(7L).appsWithoutRedirectUris(1L)
        .totalMemberships(30L).activeMemberships(27L).pendingMemberships(2L).suspendedMemberships(1L)
        .activeSigningKey(null)  // ← null: no active key
        .activeSigningKeys(0L).retiredSigningKeys(0L).revokedSigningKeys(0L)
        .activeSessions(0L).expiredSessions(0L).terminatedSessions(0L)
        .activeRefreshTokens(0L).usedRefreshTokens(0L).expiredRefreshTokens(0L).revokedRefreshTokens(0L)
        .pendingAuthCodes(0L).usedAuthCodes(0L).expiredAuthCodes(0L).revokedAuthCodes(0L)
        .alerts(List.of())
        .pendingEmailVerifications(0L).expiredPendingVerifications(0L)
        .recentRegistrations(0L).recentVerifications(0L)
        .avgUsersPerTenant(0.0).avgAppsPerTenant(0.0).avgMembershipsPerApp(0.0)
        .tenantsWithoutApps(0L).tenantsWithoutUsers(0L)
        .topTenantsByUsers(List.of()).topAppsByMemberships(List.of())
        .pendingActions(List.of()).recentActivity(List.of()).quickActions(List.of())
        .build();
    when(getDashboardUseCase.execute()).thenReturn(result);

    // When
    ResponseEntity<BaseResponse<PlatformDashboardData>> response = controller.getDashboard();

    // Then
    assertThat(response.getBody().getData().getSecurity().getActiveSigningKey()).isNull();
  }

  @Test
  void getDashboard_shouldMapAlertsCorrectly() {
    // Given — result with one alert
    PlatformDashboardResult result = PlatformDashboardResult.builder()
        .serviceTitle("KeyGo Server").serviceName("keygo-server")
        .serviceVersion("1.0-SNAPSHOT").serviceEnvironment("local").serviceStatus("UP")
        .totalTenants(10L).activeTenants(8L).pendingTenants(1L).suspendedTenants(1L).recentlyCreatedTenants(2L)
        .totalUsers(20L).activeUsers(18L).pendingUsers(1L).suspendedUsers(1L).recentlyCreatedUsers(3L)
        .usersWithoutMembership(2L)
        .totalApps(15L).activeApps(13L).pendingApps(1L).suspendedApps(1L)
        .publicApps(8L).confidentialApps(7L).appsWithoutRedirectUris(1L)
        .totalMemberships(30L).activeMemberships(27L).pendingMemberships(2L).suspendedMemberships(1L)
        .activeSigningKey(new ActiveSigningKeyInfo("kid-old", "RS256",
            Instant.now().minusSeconds(3600 * 24 * 45)))
        .activeSigningKeys(1L).retiredSigningKeys(0L).revokedSigningKeys(0L)
        .activeSessions(0L).expiredSessions(0L).terminatedSessions(0L)
        .activeRefreshTokens(0L).usedRefreshTokens(0L).expiredRefreshTokens(0L).revokedRefreshTokens(0L)
        .pendingAuthCodes(0L).usedAuthCodes(0L).expiredAuthCodes(0L).revokedAuthCodes(0L)
        .alerts(List.of(new DashboardAlert("warning", "SIGNING_KEY_AGE_HIGH", "Key is old")))
        .pendingEmailVerifications(0L).expiredPendingVerifications(0L)
        .recentRegistrations(0L).recentVerifications(0L)
        .avgUsersPerTenant(0.0).avgAppsPerTenant(0.0).avgMembershipsPerApp(0.0)
        .tenantsWithoutApps(0L).tenantsWithoutUsers(0L)
        .topTenantsByUsers(List.of()).topAppsByMemberships(List.of())
        .pendingActions(List.of()).recentActivity(List.of()).quickActions(List.of())
        .build();
    when(getDashboardUseCase.execute()).thenReturn(result);

    // When
    ResponseEntity<BaseResponse<PlatformDashboardData>> response = controller.getDashboard();

    // Then
    PlatformDashboardData data = response.getBody().getData();
    assertThat(data.getSecurity().getAlerts()).hasSize(1);
    assertThat(data.getSecurity().getAlerts().get(0).getCode()).isEqualTo("SIGNING_KEY_AGE_HIGH");
    assertThat(data.getSecurity().getAlerts().get(0).getLevel()).isEqualTo("warning");
  }
}


