package io.cmartinezs.keygo.api.registration.controller;

import io.cmartinezs.keygo.api.registration.response.RegistrationInfoData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.user.usecase.GetPlatformRegistrationInfoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public REST controller for registration info lookup.
 * <p>Controlador REST público para buscar info de registro.
 * Path: /api/v1/registration (no tenant scoping needed, registration_id is globally unique)
 */
@RestController
@RequestMapping("/api/v1/tenants")
@Tag(name = "Registration", description = "Public registration info lookup endpoints")
public class RegistrationLookupController {

  private final GetPlatformRegistrationInfoUseCase getRegistrationInfoUseCase;

  public RegistrationLookupController(GetPlatformRegistrationInfoUseCase getRegistrationInfoUseCase) {
    this.getRegistrationInfoUseCase = getRegistrationInfoUseCase;
  }

  @GetMapping("/registration")
  @Operation(
      summary = "Get registration info by registration_id",
      description = "Retrieves registration info for a PENDING user. Used to display email before "
                   + "entering the verification code. Returns tenant + app info for the verification flow.")
  @ApiResponse(responseCode = "200", description = "Registration info retrieved (code: REGISTRATION_INFO)")
  @ApiResponse(responseCode = "404", description = "Registration not found (code: RESOURCE_NOT_FOUND)")
  public ResponseEntity<BaseResponse<RegistrationInfoData>> getRegistrationInfo(
      @Parameter(description = "Registration ID (UUID)") @RequestParam String registration_id,
      @Parameter(description = "Client App ID") @RequestParam String client_id) {

    var info = getRegistrationInfoUseCase.execute(registration_id, client_id);

    RegistrationInfoData data = new RegistrationInfoData(
        info.id().value(),
        info.email(),
        info.tenantSlug(),
        info.tenantName(),
        info.clientAppId(),
        info.clientAppName(),
        info.clientAppDescription(),
        info.firstName(),
        info.lastName(),
        info.username(),
        info.status());

    BaseResponse<RegistrationInfoData> response = BaseResponse.<RegistrationInfoData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.REGISTRATION_INFO))
        .build();

    return ResponseEntity.ok(response);
  }
}