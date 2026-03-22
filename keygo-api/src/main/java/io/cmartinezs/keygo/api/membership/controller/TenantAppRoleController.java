package io.cmartinezs.keygo.api.membership.controller;

import io.cmartinezs.keygo.api.membership.request.CreateAppRoleRequest;
import io.cmartinezs.keygo.api.membership.response.AppRoleData;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.app.membership.usecase.ListAppRolesUseCase;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.membership.model.AppRoleId;
import io.cmartinezs.keygo.domain.membership.model.RoleCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for app role management within a tenant.
 * <p>Controlador REST para gestión de roles de app dentro de un tenant.
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}/apps/{clientAppId}/roles")
@SecurityRequirement(name = "AdminKeyAuth")
@Tag(name = "6-roles", description = "Application-scoped roles")
public class TenantAppRoleController {

  private final ListAppRolesUseCase listAppRolesUseCase;

  public TenantAppRoleController(ListAppRolesUseCase listAppRolesUseCase) {
    this.listAppRolesUseCase = listAppRolesUseCase;
  }

  @PostMapping
  @Operation(
      summary = "Create an app role",
      description = "Create a new role within a client application")
  @ApiResponse(responseCode = "201", description = "Role created",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "400", description = "Invalid input or duplicate role code")
  @ApiResponse(responseCode = "404", description = "App or tenant not found")
  public ResponseEntity<BaseResponse<AppRoleData>> createAppRole(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Parameter(description = "Client app ID") @PathVariable UUID clientAppId,
      @Valid @RequestBody CreateAppRoleRequest request) {

    AppRole role = AppRole.builder()
        .id(AppRoleId.generate())
        .clientAppId(io.cmartinezs.keygo.domain.clientapp.model.ClientAppId.of(clientAppId))
        .code(RoleCode.of(request.code()))
        .displayName(request.displayName())
        .description(request.description())
        .build();

    AppRoleData data = AppRoleData.builder()
        .id(role.getId().value())
        .clientAppId(role.getClientAppId().value())
        .code(role.getCode().value())
        .displayName(role.getDisplayName())
        .description(role.getDescription())
        .build();

    BaseResponse<AppRoleData> response = BaseResponse.<AppRoleData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.ROLE_CREATED))
        .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  @Operation(
      summary = "List app roles",
      description = "List all roles defined for a client application")
  @ApiResponse(responseCode = "200", description = "Roles retrieved",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "404", description = "App or tenant not found")
  public ResponseEntity<BaseResponse<List<AppRoleData>>> listAppRoles(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Parameter(description = "Client app ID") @PathVariable UUID clientAppId) {

    List<AppRole> roles = listAppRolesUseCase.execute(clientAppId);

    List<AppRoleData> data = roles.stream()
        .map(r -> AppRoleData.builder()
            .id(r.getId().value())
            .clientAppId(r.getClientAppId().value())
            .code(r.getCode().value())
            .displayName(r.getDisplayName())
            .description(r.getDescription())
            .build())
        .toList();

    BaseResponse<List<AppRoleData>> response = BaseResponse.<List<AppRoleData>>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.ROLE_LIST_RETRIEVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}


