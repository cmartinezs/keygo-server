package io.cmartinezs.keygo.api.platform.controller;

import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.platform.response.ServiceInfoData;
import io.cmartinezs.keygo.app.platform.port.ServiceInfoProvider;
import io.cmartinezs.keygo.app.platform.usecase.GetServiceInfoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for service information
 * Controlador REST para información del servicio
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/service")
@Tag(name = "Platform", description = "Public platform endpoints — no authentication required")
public class ServiceInfoController {

  private final GetServiceInfoUseCase getServiceInfoUseCase;

  public ServiceInfoController(GetServiceInfoUseCase getServiceInfoUseCase) {
    this.getServiceInfoUseCase = getServiceInfoUseCase;
  }

  /**
   * Get public service information
   * Obtener información pública del servicio
   *
   * @return ResponseEntity with BaseResponse containing service information
   */
  @GetMapping("/info")
  @Operation(
      summary = "Get service information",
      description = "Returns public metadata about the running service: title, name, and version. "
                    + "This endpoint does not require authentication.")
  @ApiResponse(
      responseCode = "200",
      description = "Service information retrieved successfully",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  public ResponseEntity<BaseResponse<ServiceInfoData>> getServiceInfo() {
    ServiceInfoProvider info = getServiceInfoUseCase.execute();

    ServiceInfoData data = ServiceInfoData.builder()
        .title(info.getTitle())
        .name(info.getName())
        .version(info.getVersion())
        .environment(info.getEnvironment())
        .status(info.getStatus())
        .build();

    BaseResponse<ServiceInfoData> response = BaseResponse.<ServiceInfoData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.SERVICE_INFO_RETRIEVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}

