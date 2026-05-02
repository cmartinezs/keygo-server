package io.cmartinezs.keygo.api.platform.controller;

import io.cmartinezs.keygo.api.platform.response.PlatformRoleData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.membership.result.GetPlatformRolesCatalogResult;
import io.cmartinezs.keygo.app.membership.usecase.GetPlatformRolesCatalogUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the platform role catalog exposed to platform administrators.
 * <p>Controlador REST para el catalogo de roles de plataforma consumible por UI.
 */
@RestController
@RequestMapping("/api/v1/platform/roles")
@Tag(
    name = "Platform Roles",
    description = "Platform role catalog — requires KEYGO_ADMIN or KEYGO_ACCOUNT_ADMIN")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyRole('KEYGO_ADMIN','KEYGO_ACCOUNT_ADMIN')")
public class PlatformRoleController {

  private final GetPlatformRolesCatalogUseCase getPlatformRolesCatalogUseCase;

  public PlatformRoleController(GetPlatformRolesCatalogUseCase getPlatformRolesCatalogUseCase) {
    this.getPlatformRolesCatalogUseCase = getPlatformRolesCatalogUseCase;
  }

  /**
   * List available platform roles.
   * <p>Lista los roles de plataforma disponibles para asignacion.
   */
  @GetMapping
  @Operation(
      summary = "List platform role catalog",
      description =
          "Returns the platform roles available for assignment in the admin UI. "
              + "Requires KEYGO_ADMIN or KEYGO_ACCOUNT_ADMIN role.")
  @ApiResponse(
      responseCode = "200",
      description = "Platform role list retrieved successfully (code: PLATFORM_ROLE_LIST_RETRIEVED)")
  @ApiResponse(
      responseCode = "401",
      description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(
      responseCode = "403",
      description = "Insufficient role for platform role catalog access (code: FORBIDDEN)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<List<PlatformRoleData>>> getPlatformRoles() {
    List<GetPlatformRolesCatalogResult> roles = getPlatformRolesCatalogUseCase.execute();
    List<PlatformRoleData> data = roles.stream().map(PlatformRoleData::from).toList();

    BaseResponse<List<PlatformRoleData>> response =
        BaseResponse.<List<PlatformRoleData>>builder()
            .data(data)
            .success(ResponseHelper.message(ResponseCode.PLATFORM_ROLE_LIST_RETRIEVED))
            .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
