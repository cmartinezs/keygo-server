package io.cmartinezs.keygo.api.discovery.controller;

import io.cmartinezs.keygo.api.discovery.response.ClientAppPublicData;
import io.cmartinezs.keygo.api.discovery.response.TenantPublicData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.shared.response.PagedData;
import io.cmartinezs.keygo.app.clientapp.filter.ClientAppFilter;
import io.cmartinezs.keygo.app.clientapp.usecase.ListClientAppsUseCase;
import java.util.EnumSet;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.tenant.filter.TenantFilter;
import io.cmartinezs.keygo.app.tenant.usecase.GetTenantBySlugUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.ListTenantsUseCase;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.clientapp.model.RegistrationPolicy;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public REST controller for tenant and app discovery — used by the UI before the
 * self-registration flow to let the user select a tenant and application.
 * <p>Controlador REST público para descubrimiento de tenants y apps — usado por la UI
 * antes del flujo de auto-registro para que el usuario seleccione tenant y aplicación.
 * All endpoints are PUBLIC — no Authorization header is required.
 * <p>Todos los endpoints son PÚBLICOS — no se requiere header Authorization.
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/tenants")
@Tag(name = "Public Discovery", description = "Unauthenticated endpoints for tenant and app discovery prior to self-registration")
public class PublicDiscoveryController {

  private final ListTenantsUseCase listTenantsUseCase;
  private final GetTenantBySlugUseCase getTenantBySlugUseCase;
  private final ListClientAppsUseCase listClientAppsUseCase;

  public PublicDiscoveryController(
      ListTenantsUseCase listTenantsUseCase,
      GetTenantBySlugUseCase getTenantBySlugUseCase,
      ListClientAppsUseCase listClientAppsUseCase) {
    this.listTenantsUseCase = listTenantsUseCase;
    this.getTenantBySlugUseCase = getTenantBySlugUseCase;
    this.listClientAppsUseCase = listClientAppsUseCase;
  }

  /**
   * List tenants that are active and allow open registration.
   * <p>Lista tenants que están activos y permiten registro abierto.
   * Only ACTIVE tenants with at least one publicly-accessible app appear here.
   */
  @GetMapping("/public")
  @Operation(
      summary = "List public tenants",
      description = "Returns a paginated list of active tenants available for self-registration. "
                    + "No authentication required. Supports optional name search and sorting.")
  @ApiResponse(responseCode = "200", description = "Tenant list retrieved successfully (code: TENANT_LIST_RETRIEVED)")
  @ApiResponse(responseCode = "400", description = "Invalid pagination parameters (code: INVALID_INPUT)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<PagedData<TenantPublicData>>> listPublicTenants(
      @Parameter(description = "Partial match on tenant name (case-insensitive)")
      @RequestParam(name = "name_like", required = false) String nameLike,
      @Parameter(description = "Zero-based page number", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size (1–200)", example = "50")
      @RequestParam(defaultValue = "50") int size,
      @Parameter(description = "Sort field (name, status, createdAt)")
      @RequestParam(required = false) String sort,
      @Parameter(description = "Sort order (ASC, DESC)", example = "ASC")
      @RequestParam(required = false) String order) {

    // Force status=ACTIVE and onlyWithPublicRegistration — exclude tenants where no app allows self-registration
    TenantFilter filter = TenantFilter.forPublicDiscovery(TenantStatus.ACTIVE, nameLike, page, size, sort, order);
    PagedResult<Tenant> result = listTenantsUseCase.execute(filter);

    List<TenantPublicData> content = result.getContent().stream()
        .map(this::toPublicData)
        .toList();

    PagedData<TenantPublicData> pagedData = PagedData.<TenantPublicData>builder()
        .content(content)
        .page(result.getPage())
        .size(result.getSize())
        .totalElements(result.getTotalElements())
        .totalPages(result.getTotalPages())
        .last(result.isLast())
        .build();

    BaseResponse<PagedData<TenantPublicData>> response = BaseResponse.<PagedData<TenantPublicData>>builder()
        .data(pagedData)
        .success(ResponseHelper.message(ResponseCode.TENANT_LIST_RETRIEVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  /**
   * List public client applications for a given tenant.
   * <p>Lista las aplicaciones cliente públicas de un tenant dado.
   * Only PUBLIC-type apps with status ACTIVE and registration_policy != INVITE_ONLY appear.
   *
   * @param tenantSlug the tenant slug
   * @return paginated list of public apps, or 404 if tenant is not found or not active
   */
  @GetMapping("/{tenantSlug}/apps/public")
  @Operation(
      summary = "List public client apps for a tenant",
      description = "Returns a paginated list of PUBLIC-type, active client applications under the "
                    + "specified tenant that allow open self-registration (registration_policy != INVITE_ONLY). "
                    + "No authentication required.")
  @ApiResponse(responseCode = "200", description = "Client app list retrieved successfully (code: CLIENT_APP_LIST_RETRIEVED)")
  @ApiResponse(responseCode = "400", description = "Invalid pagination parameters (code: INVALID_INPUT)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Tenant not found or not active (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<PagedData<ClientAppPublicData>>> listPublicClientApps(
      @Parameter(description = "Tenant slug", example = "my-company") @PathVariable String tenantSlug,
      @Parameter(description = "Zero-based page number", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size (1–200)", example = "50")
      @RequestParam(defaultValue = "50") int size,
      @Parameter(description = "Sort field (name, status, createdAt)")
      @RequestParam(required = false) String sort,
      @Parameter(description = "Sort order (ASC, DESC)", example = "ASC")
      @RequestParam(required = false) String order) {

    // Verify tenant exists and is active; throws TenantNotFoundException (→ 404) otherwise
    Tenant tenant = getTenantBySlugUseCase.execute(tenantSlug);
    if (!tenant.isActive()) {
      throw new TenantNotFoundException(tenantSlug);
    }

    // Force filters: status=ACTIVE, type=PUBLIC, exclude non-self-service policies
    // INVITE_ONLY: registration is disabled. OPEN_NO_MEMBERSHIP: no app membership is created,
    // so showing these apps in the selection step would be misleading.
    ClientAppFilter filter = ClientAppFilter.of(
        io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus.ACTIVE,
        null,
        ClientType.PUBLIC,
        EnumSet.of(RegistrationPolicy.INVITE_ONLY, RegistrationPolicy.OPEN_NO_MEMBERSHIP),
        page, size, sort, order);

    PagedResult<ClientApp> result = listClientAppsUseCase.execute(tenantSlug, filter);

    List<ClientAppPublicData> content = result.getContent().stream()
        .map(this::toPublicData)
        .toList();

    PagedData<ClientAppPublicData> pagedData = PagedData.<ClientAppPublicData>builder()
        .content(content)
        .page(result.getPage())
        .size(result.getSize())
        .totalElements(result.getTotalElements())
        .totalPages(result.getTotalPages())
        .last(result.isLast())
        .build();

    BaseResponse<PagedData<ClientAppPublicData>> response = BaseResponse.<PagedData<ClientAppPublicData>>builder()
        .data(pagedData)
        .success(ResponseHelper.message(ResponseCode.CLIENT_APP_LIST_RETRIEVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  // ─── Private mappers ──────────────────────────────────────────────────────

  private TenantPublicData toPublicData(Tenant tenant) {
    return TenantPublicData.builder()
        .id(tenant.getId().toString())
        .name(tenant.getName())
        .slug(tenant.getSlug().value())
        .build();
  }

  private ClientAppPublicData toPublicData(ClientApp app) {
    return ClientAppPublicData.builder()
        .id(app.getId().value().toString())
        .clientId(app.getClientId().value())
        .name(app.getName())
        .description(app.getDescription())
        .type(app.getType().name())
        .registrationPolicy(app.getRegistrationPolicy().name())
        .isActive(app.isActive())
        .build();
  }
}
