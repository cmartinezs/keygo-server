package io.cmartinezs.keygo.app.platform.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.platform.port.PlatformStatsPort;
import io.cmartinezs.keygo.app.platform.result.PlatformStatsResult;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for GetPlatformStatsUseCase.
 * Pruebas unitarias para GetPlatformStatsUseCase.
 */
@ExtendWith(MockitoExtension.class)
class GetPlatformStatsUseCaseTest {

  @Mock private PlatformStatsPort platformStatsPort;

  private GetPlatformStatsUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetPlatformStatsUseCase(platformStatsPort);
  }

  @Test
  void execute_shouldReturnAllCountersFromPort() {
    // Given
    when(platformStatsPort.countTotalTenants()).thenReturn(10L);
    when(platformStatsPort.countTenantsByStatus(TenantStatus.ACTIVE)).thenReturn(7L);
    when(platformStatsPort.countTenantsByStatus(TenantStatus.SUSPENDED)).thenReturn(2L);
    when(platformStatsPort.countTenantsByStatus(TenantStatus.PENDING)).thenReturn(1L);
    when(platformStatsPort.countTotalUsers()).thenReturn(100L);
    when(platformStatsPort.countUsersByStatus(UserStatus.ACTIVE)).thenReturn(80L);
    when(platformStatsPort.countUsersByStatus(UserStatus.PENDING)).thenReturn(15L);
    when(platformStatsPort.countUsersByStatus(UserStatus.SUSPENDED)).thenReturn(5L);
    when(platformStatsPort.countTotalApps()).thenReturn(25L);
    when(platformStatsPort.countActiveSigningKeys()).thenReturn(2L);

    // When
    PlatformStatsResult result = useCase.execute();

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getTotalTenants()).isEqualTo(10L);
    assertThat(result.getActiveTenants()).isEqualTo(7L);
    assertThat(result.getSuspendedTenants()).isEqualTo(2L);
    assertThat(result.getPendingTenants()).isEqualTo(1L);
    assertThat(result.getTotalUsers()).isEqualTo(100L);
    assertThat(result.getActiveUsers()).isEqualTo(80L);
    assertThat(result.getPendingUsers()).isEqualTo(15L);
    assertThat(result.getSuspendedUsers()).isEqualTo(5L);
    assertThat(result.getTotalApps()).isEqualTo(25L);
    assertThat(result.getActiveSigningKeys()).isEqualTo(2L);
  }

  @Test
  void execute_shouldReturnZerosWhenAllCountsAreZero() {
    // Given
    when(platformStatsPort.countTotalTenants()).thenReturn(0L);
    when(platformStatsPort.countTenantsByStatus(TenantStatus.ACTIVE)).thenReturn(0L);
    when(platformStatsPort.countTenantsByStatus(TenantStatus.SUSPENDED)).thenReturn(0L);
    when(platformStatsPort.countTenantsByStatus(TenantStatus.PENDING)).thenReturn(0L);
    when(platformStatsPort.countTotalUsers()).thenReturn(0L);
    when(platformStatsPort.countUsersByStatus(UserStatus.ACTIVE)).thenReturn(0L);
    when(platformStatsPort.countUsersByStatus(UserStatus.PENDING)).thenReturn(0L);
    when(platformStatsPort.countUsersByStatus(UserStatus.SUSPENDED)).thenReturn(0L);
    when(platformStatsPort.countTotalApps()).thenReturn(0L);
    when(platformStatsPort.countActiveSigningKeys()).thenReturn(0L);

    // When
    PlatformStatsResult result = useCase.execute();

    // Then
    assertThat(result.getTotalTenants()).isZero();
    assertThat(result.getActiveTenants()).isZero();
    assertThat(result.getTotalUsers()).isZero();
    assertThat(result.getTotalApps()).isZero();
    assertThat(result.getActiveSigningKeys()).isZero();
  }
}

