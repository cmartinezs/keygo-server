package io.cmartinezs.keygo.api.membership.controller;

import io.cmartinezs.keygo.api.membership.request.AssignRoleParentRequest;
import io.cmartinezs.keygo.api.membership.request.CreateAppRoleRequest;
import io.cmartinezs.keygo.api.membership.response.AppRoleData;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.shared.response.PagedData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.app.membership.command.AssignRoleParentCommand;
import io.cmartinezs.keygo.app.membership.command.CreateAppRoleCommand;
import io.cmartinezs.keygo.app.membership.usecase.AssignRoleParentUseCase;
import io.cmartinezs.keygo.app.membership.usecase.CreateAppRoleUseCase;
import io.cmartinezs.keygo.app.membership.usecase.ListAppRolesUseCase;
import io.cmartinezs.keygo.app.membership.usecase.RemoveRoleParentUseCase;
import io.cmartinezs.keygo.app.role.filter.AppRoleFilter;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "App Roles", description = "Application-scoped roles — requires Bearer JWT")
@PreAuthorize("hasAnyRole('ADMIN','ADMIN_TENANT','KEYGO_ADMIN','KEYGO_TENANT_ADMIN') and @tenantAuthorizationEvaluator.hasTenantAccess(authentication)")
public class TenantAppRoleController {

  private final CreateAppRoleUseCase createAppRoleUseCase;
  private final ListAppRolesUseCase listAppRolesUseCase;
  private final AssignRoleParentUseCase assignRoleParentUseCase;
  private final RemoveRoleParentUseCase removeRoleParentUseCase;

  public TenantAppRoleController(
      CreateAppRoleUseCase createAppRoleUseCase,
      ListAppRolesUseCase listAppRolesUseCase,
      AssignRoleParentUseCase assignRoleParentUseCase,
      RemoveRoleParentUseCase removeRoleParentUseCase) {
    this.createAppRoleUseCase = createAppRoleUseCase;
    this.listAppRolesUseCase = listAppRolesUseCase;
    this.assignRoleParentUseCase = assignRoleParentUseCase;
    this.removeRoleParentUseCase = removeRoleParentUseCase;
  }

  @PostMapping
  @Operation(
      summary = "Create an app role",
      description = "Create a new role within a client application")
  @ApiResponse(responseCode = "201", description = "Role created (code: ROLE_CREATED)")
  @ApiResponse(responseCode = "400", description = "Request body validation failed (code: INVALID_INPUT). data.field_errors lists each invalid field.",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "403", description = "Tenant suspended or insufficient permissions (code: BUSINESS_RULE_VIOLATION / INSUFFICIENT_PERMISSIONS)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Tenant or client app not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "409", description = "Role code already exists in this app (code: DUPLICATE_RESOURCE)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<AppRoleData>> createAppRole(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Parameter(description = "Client app ID") @PathVariable UUID clientAppId,
      @Valid @RequestBody CreateAppRoleRequest request) {

    CreateAppRoleCommand command = new CreateAppRoleCommand(
        tenantSlug,
        clientAppId,
        request.code(),
        request.displayName(),
        request.description(),
        request.isDefault());

    AppRole role = createAppRoleUseCase.execute(command);

    AppRoleData data = AppRoleData.builder()
        .id(role.getId().value())
        .clientAppId(role.getClientAppId().value())
        .code(role.getCode().value())
        .displayName(role.getDisplayName())
        .description(role.getDescription())
        .isDefault(role.isDefault())
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
      description = "List roles defined for a client application with optional pagination, filtering, and sorting")
  @ApiResponse(responseCode = "200", description = "Roles retrieved (code: ROLE_LIST_RETRIEVED)")
  @ApiResponse(responseCode = "400", description = "Invalid pagination parameters (code: INVALID_INPUT). data.field_errors lists each invalid field.",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Tenant or client app not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<PagedData<AppRoleData>>> listAppRoles(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Parameter(description = "Client app ID") @PathVariable UUID clientAppId,
      @Parameter(description = "Partial match on role name (case-insensitive)")
      @RequestParam(name = "name_like", required = false) String nameLike,
      @Parameter(description = "Zero-based page number", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size (1–200)", example = "20")
      @RequestParam(defaultValue = "20") int size,
      @Parameter(description = "Sort field (name, createdAt)")
      @RequestParam(required = false) String sort,
      @Parameter(description = "Sort order (ASC, DESC)", example = "ASC")
      @RequestParam(required = false) String order) {

    AppRoleFilter filter = AppRoleFilter.of(nameLike, page, size, sort, order);
    PagedResult<AppRole> result = listAppRolesUseCase.execute(tenantSlug, clientAppId, filter);

    List<AppRoleData> data = result.getContent().stream()
        .map(r -> AppRoleData.builder()
            .id(r.getId().value())
            .clientAppId(r.getClientAppId().value())
            .code(r.getCode().value())
            .displayName(r.getDisplayName())
            .description(r.getDescription())
            .isDefault(r.isDefault())
            .build())
        .toList();

    PagedData<AppRoleData> pagedData = PagedData.<AppRoleData>builder()
        .content(data)
        .page(result.getPage())
        .size(result.getSize())
        .totalElements(result.getTotalElements())
        .totalPages(result.getTotalPages())
        .last(result.isLast())
        .build();

    BaseResponse<PagedData<AppRoleData>> response = BaseResponse.<PagedData<AppRoleData>>builder()
        .data(pagedData)
        .success(ResponseHelper.message(ResponseCode.ROLE_LIST_RETRIEVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PostMapping("/{roleCode}/parent")
  @Operation(
      summary = "Assign parent role",
      description = "Set a parent role for the given child role. Replaces any existing parent. "
          + "Enforces cycle-free assignment and max depth of 5 levels.")
  @ApiResponse(responseCode = "200", description = "Parent assigned (code: ROLE_PARENT_ASSIGNED)")
  @ApiResponse(responseCode = "400", description = "Cycle detected or depth exceeded (code: INVALID_INPUT)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Tenant, app or role not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<Void>> assignParent(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Parameter(description = "Client app ID") @PathVariable UUID clientAppId,
      @Parameter(description = "Child role code") @PathVariable String roleCode,
      @Valid @RequestBody AssignRoleParentRequest request) {

    assignRoleParentUseCase.execute(
        new AssignRoleParentCommand(tenantSlug, clientAppId, roleCode, request.parentRoleCode()));

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .success(ResponseHelper.message(ResponseCode.ROLE_PARENT_ASSIGNED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @DeleteMapping("/{roleCode}/parent")
  @Operation(
      summary = "Remove parent role",
      description = "Remove the parent assignment from the given role. No-op if it has no parent.")
  @ApiResponse(responseCode = "200", description = "Parent removed (code: ROLE_PARENT_REMOVED)")
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Tenant, app or role not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<Void>> removeParent(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Parameter(description = "Client app ID") @PathVariable UUID clientAppId,
      @Parameter(description = "Role code") @PathVariable String roleCode) {

    removeRoleParentUseCase.execute(tenantSlug, clientAppId, roleCode);

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .success(ResponseHelper.message(ResponseCode.ROLE_PARENT_REMOVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
