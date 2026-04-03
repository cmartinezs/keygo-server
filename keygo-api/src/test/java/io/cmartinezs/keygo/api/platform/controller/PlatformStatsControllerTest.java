package io.cmartinezs.keygo.api.platform.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.api.platform.response.PlatformStatsData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.platform.result.PlatformStatsResult;
import io.cmartinezs.keygo.app.platform.usecase.GetPlatformStatsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformStatsController")
class PlatformStatsControllerTest {

  @Mock
  private GetPlatformStatsUseCase getPlatformStatsUseCase;

  private PlatformStatsController controller;

  @BeforeEach
  void setUp() {
    controller = new PlatformStatsController(getPlatformStatsUseCase);
  }

  @Test
  @DisplayName("getStats: should return 200 with PLATFORM_STATS_RETRIEVED code")
  void getStats_returns200WithCorrectCode() {
    // Given
    when(getPlatformStatsUseCase.execute()).thenReturn(PlatformStatsResult.builder()
        .totalTenants(5).activeTenants(3).suspendedTenants(1).pendingTenants(1)
        .totalUsers(20).activeUsers(15).pendingUsers(3).suspendedUsers(2)
        .totalApps(8)
        .activeSigningKeys(2)
        .build());

    // When
    ResponseEntity<BaseResponse<PlatformStatsData>> response = controller.getStats();

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getSuccess().getCode())
        .isEqualTo(ResponseCode.PLATFORM_STATS_RETRIEVED.getCode());
  }

  @Test
  @DisplayName("getStats: should return correct tenant stats")
  void getStats_returnsCorrectTenantStats() {
    // Given
    when(getPlatformStatsUseCase.execute()).thenReturn(PlatformStatsResult.builder()
        .totalTenants(10).activeTenants(7).suspendedTenants(2).pendingTenants(1)
        .totalUsers(0).activeUsers(0).pendingUsers(0).suspendedUsers(0)
        .totalApps(0).activeSigningKeys(0)
        .build());

    // When
    PlatformStatsData data = controller.getStats().getBody().getData();

    // Then
    assertThat(data.getTenants()).isNotNull();
    assertThat(data.getTenants().getTotal()).isEqualTo(10);
    assertThat(data.getTenants().getActive()).isEqualTo(7);
    assertThat(data.getTenants().getSuspended()).isEqualTo(2);
    assertThat(data.getTenants().getPending()).isEqualTo(1);
  }

  @Test
  @DisplayName("getStats: should return correct user stats")
  void getStats_returnsCorrectUserStats() {
    // Given
    when(getPlatformStatsUseCase.execute()).thenReturn(PlatformStatsResult.builder()
        .totalTenants(0).activeTenants(0).suspendedTenants(0).pendingTenants(0)
        .totalUsers(50).activeUsers(40).pendingUsers(7).suspendedUsers(3)
        .totalApps(0).activeSigningKeys(0)
        .build());

    // When
    PlatformStatsData data = controller.getStats().getBody().getData();

    // Then
    assertThat(data.getUsers()).isNotNull();
    assertThat(data.getUsers().getTotal()).isEqualTo(50);
    assertThat(data.getUsers().getActive()).isEqualTo(40);
    assertThat(data.getUsers().getPending()).isEqualTo(7);
    assertThat(data.getUsers().getSuspended()).isEqualTo(3);
  }

  @Test
  @DisplayName("getStats: should return correct apps and signing keys stats")
  void getStats_returnsCorrectAppsAndSigningKeysStats() {
    // Given
    when(getPlatformStatsUseCase.execute()).thenReturn(PlatformStatsResult.builder()
        .totalTenants(0).activeTenants(0).suspendedTenants(0).pendingTenants(0)
        .totalUsers(0).activeUsers(0).pendingUsers(0).suspendedUsers(0)
        .totalApps(12).activeSigningKeys(4)
        .build());

    // When
    PlatformStatsData data = controller.getStats().getBody().getData();

    // Then
    assertThat(data.getApps()).isNotNull();
    assertThat(data.getApps().getTotal()).isEqualTo(12);
    assertThat(data.getSigningKeys()).isNotNull();
    assertThat(data.getSigningKeys().getActive()).isEqualTo(4);
  }
}
