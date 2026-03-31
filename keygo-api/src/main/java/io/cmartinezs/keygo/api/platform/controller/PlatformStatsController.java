package io.cmartinezs.keygo.api.platform.controller;

import io.cmartinezs.keygo.api.platform.response.PlatformStatsData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.platform.result.PlatformStatsResult;
import io.cmartinezs.keygo.app.platform.usecase.GetPlatformStatsUseCase;
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

/**
 * REST controller for platform-level statistics.
 * <p>Controlador REST para estadísticas a nivel de plataforma.
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/platform")
@Tag(name = "Platform", description = "Public platform endpoints — no authentication required")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class PlatformStatsController {

  private final GetPlatformStatsUseCase getPlatformStatsUseCase;

  public PlatformStatsController(GetPlatformStatsUseCase getPlatformStatsUseCase) {
    this.getPlatformStatsUseCase = getPlatformStatsUseCase;
  }

  /**
   * Get aggregated platform statistics.
   * <p>Obtiene estadísticas agregadas de la plataforma.
   *
   * @return 200 OK with platform stats
   */
  @GetMapping("/stats")
  @Operation(
      summary = "Get platform statistics",
      description = "Returns aggregated counts of tenants (by status), users (by status), "
                    + "client apps and active signing keys. Requires ADMIN role.")
  @ApiResponse(responseCode = "200", description = "Platform statistics retrieved successfully")
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<PlatformStatsData>> getStats() {
    PlatformStatsResult result = getPlatformStatsUseCase.execute();

    PlatformStatsData data = PlatformStatsData.builder()
        .tenants(PlatformStatsData.TenantStats.builder()
            .total(result.getTotalTenants())
            .active(result.getActiveTenants())
            .suspended(result.getSuspendedTenants())
            .pending(result.getPendingTenants())
            .build())
        .users(PlatformStatsData.UserStats.builder()
            .total(result.getTotalUsers())
            .active(result.getActiveUsers())
            .pending(result.getPendingUsers())
            .suspended(result.getSuspendedUsers())
            .build())
        .apps(PlatformStatsData.AppStats.builder()
            .total(result.getTotalApps())
            .build())
        .signingKeys(PlatformStatsData.SigningKeyStats.builder()
            .active(result.getActiveSigningKeys())
            .build())
        .build();

    BaseResponse<PlatformStatsData> response = BaseResponse.<PlatformStatsData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.PLATFORM_STATS_RETRIEVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}

