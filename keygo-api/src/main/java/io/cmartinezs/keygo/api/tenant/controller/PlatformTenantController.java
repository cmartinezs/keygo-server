package io.cmartinezs.keygo.api.tenant.controller;

import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.shared.response.PagedData;
import io.cmartinezs.keygo.api.tenant.request.CreateTenantRequest;
import io.cmartinezs.keygo.api.tenant.response.TenantData;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.tenant.command.CreateTenantCommand;
import io.cmartinezs.keygo.app.tenant.filter.TenantFilter;
import io.cmartinezs.keygo.app.tenant.usecase.CreateTenantUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.GetTenantBySlugUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.ListTenantsUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.SuspendTenantUseCase;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for platform-level tenant management operations.
 * Controlador REST para operaciones de gestión de tenants a nivel de plataforma.
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/tenants")
@Tag(name = "Tenants", description = "Tenant lifecycle management — requires Bearer JWT")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class PlatformTenantController {

  private final CreateTenantUseCase createTenantUseCase;
  private final GetTenantBySlugUseCase getTenantBySlugUseCase;
  private final SuspendTenantUseCase suspendTenantUseCase;
  private final ListTenantsUseCase listTenantsUseCase;

  public PlatformTenantController(
      CreateTenantUseCase createTenantUseCase,
      GetTenantBySlugUseCase getTenantBySlugUseCase,
      SuspendTenantUseCase suspendTenantUseCase,
      ListTenantsUseCase listTenantsUseCase) {
    this.createTenantUseCase = createTenantUseCase;
    this.getTenantBySlugUseCase = getTenantBySlugUseCase;
    this.suspendTenantUseCase = suspendTenantUseCase;
    this.listTenantsUseCase = listTenantsUseCase;
  }

  /**
   * List all tenants with optional filtering and pagination.
   * Listar todos los tenants con filtrado opcional y paginación.
   *
   * @param status   optional status filter (ACTIVE, SUSPENDED, PENDING)
   * @param nameLike optional partial match on tenant name (case-insensitive)
   * @param page     zero-based page number (default 0)
   * @param size     page size 1–200 (default 20)
   * @return 200 OK with paginated tenant list
   */
  @GetMapping
  @Operation(
      summary = "List all tenants",
      description = "Returns a paginated list of all tenants. Supports optional filtering by "
                    + "status and partial name match. Requires ADMIN role.")
  @ApiResponse(responseCode = "200", description = "Tenant list retrieved successfully",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "400", description = "Invalid query parameters",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  public ResponseEntity<BaseResponse<PagedData<TenantData>>> listTenants(
      @Parameter(description = "Filter by tenant status (ACTIVE, SUSPENDED, PENDING)")
      @RequestParam(required = false) TenantStatus status,
      @Parameter(description = "Partial match on tenant name (case-insensitive)")
      @RequestParam(required = false) String nameLike,
      @Parameter(description = "Zero-based page number", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size (1–200)", example = "20")
      @RequestParam(defaultValue = "20") int size) {

    TenantFilter filter = TenantFilter.of(status, nameLike, page, size);
    PagedResult<Tenant> result = listTenantsUseCase.execute(filter);

    List<TenantData> content = result.getContent().stream()
        .map(this::toData)
        .toList();

    PagedData<TenantData> pagedData = PagedData.<TenantData>builder()
        .content(content)
        .page(result.getPage())
        .size(result.getSize())
        .totalElements(result.getTotalElements())
        .totalPages(result.getTotalPages())
        .last(result.isLast())
        .build();

    BaseResponse<PagedData<TenantData>> response = BaseResponse.<PagedData<TenantData>>builder()
        .data(pagedData)
        .success(ResponseHelper.message(ResponseCode.TENANT_LIST_RETRIEVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  /**
   * Create a new tenant.
   * Crear un nuevo tenant.
   *
   * @param request the creation request / la solicitud de creación
   * @return 201 Created with the tenant data / 201 Created con los datos del tenant
   */
  @PostMapping
  @Operation(
      summary = "Create a new tenant",
      description = "Creates a new tenant with the given name and owner email. "
                    + "A URL-friendly slug is automatically generated from the name.")
  @ApiResponse(responseCode = "201", description = "Tenant created successfully",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "400", description = "Invalid request body",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid admin key",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  public ResponseEntity<BaseResponse<TenantData>> createTenant(
      @Valid @RequestBody CreateTenantRequest request) {

    Tenant tenant = createTenantUseCase.execute(
        new CreateTenantCommand(request.name(), request.ownerEmail()));

    BaseResponse<TenantData> response = BaseResponse.<TenantData>builder()
        .data(toData(tenant))
        .success(ResponseHelper.message(ResponseCode.TENANT_CREATED))
        .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Retrieve a tenant by its slug.
   * Obtener un tenant por su slug.
   *
   * @param slug the tenant slug / el slug del tenant
   * @return 200 OK with the tenant data / 200 OK con los datos del tenant
   */
  @GetMapping("/{slug}")
  @Operation(
      summary = "Get tenant by slug",
      description = "Retrieves tenant details by its unique slug identifier.")
  @ApiResponse(responseCode = "200", description = "Tenant retrieved successfully",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid admin key",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "404", description = "Tenant not found",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  public ResponseEntity<BaseResponse<TenantData>> getTenantBySlug(
      @Parameter(description = "Unique slug identifier of the tenant", example = "my-company")
      @PathVariable String slug) {

    Tenant tenant = getTenantBySlugUseCase.execute(slug);

    BaseResponse<TenantData> response = BaseResponse.<TenantData>builder()
        .data(toData(tenant))
        .success(ResponseHelper.message(ResponseCode.TENANT_RETRIEVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  /**
   * Suspend an existing tenant.
   * Suspender un tenant existente.
   *
   * @param slug the tenant slug / el slug del tenant
   * @return 200 OK with the suspended tenant data / 200 OK con los datos del tenant suspendido
   */
  @PutMapping("/{slug}/suspend")
  @Operation(
      summary = "Suspend a tenant",
      description = "Suspends an active tenant. A suspended tenant cannot be used for authentication.")
  @ApiResponse(responseCode = "200", description = "Tenant suspended successfully",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid admin key",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "403", description = "Tenant is already suspended",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "404", description = "Tenant not found",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  public ResponseEntity<BaseResponse<TenantData>> suspendTenant(
      @Parameter(description = "Unique slug identifier of the tenant", example = "my-company")
      @PathVariable String slug) {

    Tenant tenant = suspendTenantUseCase.execute(slug);

    BaseResponse<TenantData> response = BaseResponse.<TenantData>builder()
        .data(toData(tenant))
        .success(ResponseHelper.message(ResponseCode.TENANT_SUSPENDED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  // ─── Private helpers ──────────────────────────────────────────────────────

  private TenantData toData(Tenant tenant) {
    return TenantData.builder()
        .id(tenant.getId().toString())
        .name(tenant.getName())
        .slug(tenant.getSlug().value())
        .ownerEmail(tenant.getOwnerEmail())
        .status(tenant.getStatus().name())
        .build();
  }
}

