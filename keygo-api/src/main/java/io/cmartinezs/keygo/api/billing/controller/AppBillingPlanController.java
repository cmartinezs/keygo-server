package io.cmartinezs.keygo.api.billing.controller;

import io.cmartinezs.keygo.api.billing.request.CreateAppPlanRequest;
import io.cmartinezs.keygo.api.billing.response.AppPlanData;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.app.billing.catalog.command.CreateAppPlanCommand;
import io.cmartinezs.keygo.app.billing.catalog.result.AppPlanResult;
import io.cmartinezs.keygo.app.billing.catalog.usecase.CreateAppPlanUseCase;
import io.cmartinezs.keygo.app.billing.catalog.usecase.GetAppPlanCatalogUseCase;
import io.cmartinezs.keygo.app.billing.catalog.usecase.GetAppPlanUseCase;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
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
import java.util.UUID;

/**
 * REST controller for app billing plan catalog endpoints.
 * <p>Controlador REST para el catálogo de planes de billing de una app.
 * Public endpoints: GET /billing/catalog, GET /billing/catalog/{planCode} — no authentication required.
 * <p>Endpoints públicos: GET /billing/catalog, GET /billing/catalog/{planCode} — sin autenticación.
 * Protected endpoint: POST /billing/plans — requires Bearer JWT with ADMIN_TENANT role.
 * <p>Endpoint protegido: POST /billing/plans — requiere Bearer JWT con rol ADMIN_TENANT.
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}/apps/{clientId}")
@Tag(name = "Billing — Catalog", description = "App plan catalog — public read, admin write")
public class AppBillingPlanController {

  private final TenantRepositoryPort tenantRepo;
  private final ClientAppRepositoryPort clientAppRepo;
  private final GetAppPlanCatalogUseCase getCatalogUseCase;
  private final GetAppPlanUseCase getPlanUseCase;
  private final CreateAppPlanUseCase createPlanUseCase;

  public AppBillingPlanController(
      TenantRepositoryPort tenantRepo,
      ClientAppRepositoryPort clientAppRepo,
      GetAppPlanCatalogUseCase getCatalogUseCase,
      GetAppPlanUseCase getPlanUseCase,
      CreateAppPlanUseCase createPlanUseCase) {
    this.tenantRepo = tenantRepo;
    this.clientAppRepo = clientAppRepo;
    this.getCatalogUseCase = getCatalogUseCase;
    this.getPlanUseCase = getPlanUseCase;
    this.createPlanUseCase = createPlanUseCase;
  }

  /**
   * Returns all active public plans available for a given client app.
   * <p>Retorna todos los planes públicos activos disponibles para una app cliente.
   * No authentication required.
   * <p>No requiere autenticación.
   */
  @GetMapping("/billing/catalog")
  @Operation(
      summary = "Get public plan catalog",
      description = "Returns all active public plans for the specified client app. "
                    + "No authentication required. Each entry includes the plan metadata, "
                    + "its active version (price, billing period, trial days) and the associated entitlements.")
  @ApiResponse(responseCode = "200", description = "Plan catalog retrieved successfully (code: APP_PLAN_CATALOG_RETRIEVED)")
  @ApiResponse(responseCode = "404", description = "Tenant or client app not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<List<AppPlanData>>> getCatalog(
      @Parameter(description = "Tenant slug", example = "keygo") @PathVariable String tenantSlug,
      @Parameter(description = "Client app client_id", example = "keygo-ui") @PathVariable String clientId) {

    UUID appId = resolveClientAppId(tenantSlug, clientId);
    List<AppPlanData> data = getCatalogUseCase.execute(appId)
        .stream()
        .map(r -> AppPlanData.from(r.plan(), r.versions(), r.billingOptionsByVersion(), r.entitlements()))
        .toList();

    return ResponseEntity.ok(BaseResponse.<List<AppPlanData>>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.APP_PLAN_CATALOG_RETRIEVED))
        .build());
  }

  /**
   * Returns a single public plan with its active version and entitlements.
   * <p>Retorna un plan público individual con su versión activa y entitlements.
   * No authentication required.
   * <p>No requiere autenticación.
   */
  @GetMapping("/billing/catalog/{planCode}")
  @Operation(
      summary = "Get public plan detail",
      description = "Returns a single active public plan identified by its code, including the "
                    + "active version (price, billing period, trial days) and its full entitlements list. "
                    + "No authentication required.")
  @ApiResponse(responseCode = "200", description = "Plan detail retrieved successfully (code: APP_PLAN_RETRIEVED)")
  @ApiResponse(responseCode = "404", description = "Tenant, client app or plan not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<AppPlanData>> getPlanPublic(
      @Parameter(description = "Tenant slug", example = "keygo") @PathVariable String tenantSlug,
      @Parameter(description = "Client app client_id", example = "keygo-ui") @PathVariable String clientId,
      @Parameter(description = "Plan code (e.g. FREE, STARTER, BUSINESS)", example = "FREE") @PathVariable String planCode) {

    UUID appId = resolveClientAppId(tenantSlug, clientId);
    AppPlanResult result = getPlanUseCase.execute(appId, planCode);
    AppPlanData data = AppPlanData.from(result.plan(), result.versions(), result.billingOptionsByVersion(), result.entitlements());

    return ResponseEntity.ok(BaseResponse.<AppPlanData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.APP_PLAN_RETRIEVED))
        .build());
  }

  /**
   * Creates a new billing plan with its initial version and entitlements.
   * <p>Crea un nuevo plan de billing con su versión inicial y entitlements.
   * Requires Bearer JWT with ADMIN_TENANT role scoped to the tenant in the path.
   * <p>Requiere Bearer JWT con rol ADMIN_TENANT con alcance al tenant del path.
   */
  @PostMapping("/billing/plans")
  @SecurityRequirement(name = "BearerAuth")
  @PreAuthorize("hasAnyRole('ADMIN','ADMIN_TENANT','KEYGO_ADMIN','KEYGO_ACCOUNT_ADMIN') and @tenantAuthorizationEvaluator.hasTenantAccess(authentication)")
  @Operation(
      summary = "Create a billing plan",
      description = "Creates a new plan with its initial version (price, currency, billing period, trial days) "
                    + "and an optional list of entitlements (feature flags and usage quotas). "
                    + "The plan code must be unique within the client app. "
                    + "Requires Bearer JWT with ADMIN_TENANT role.")
  @ApiResponse(responseCode = "201", description = "Plan created successfully (code: APP_PLAN_CREATED)")
  @ApiResponse(responseCode = "400", description = "Request body validation failed (code: INVALID_INPUT). data.field_errors lists each invalid field.",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Tenant or client app not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "409", description = "A plan with the same code already exists for this client app (code: DUPLICATE_RESOURCE)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<AppPlanData>> createPlan(
      @Parameter(description = "Tenant slug", example = "keygo") @PathVariable String tenantSlug,
      @Parameter(description = "Client app client_id", example = "keygo-ui") @PathVariable String clientId,
      @Valid @RequestBody CreateAppPlanRequest request) {

    UUID appId = resolveClientAppId(tenantSlug, clientId);

    CreateAppPlanCommand cmd = new CreateAppPlanCommand(
        appId,
        request.code(),
        request.name(),
        request.description(),
        request.isPublic(),
        request.sortOrder(),
        request.version(),
        request.currency(),
        request.trialDays(),
        request.effectiveFrom(),
        request.billingOptions() == null ? List.of() :
            request.billingOptions().stream().map(o ->
                new CreateAppPlanCommand.BillingOptionDef(
                    o.billingPeriod(), o.basePrice(), o.discountPct(), o.isDefault()
                )).toList(),
        request.entitlements() == null ? List.of() :
            request.entitlements().stream().map(e ->
                new CreateAppPlanCommand.EntitlementDef(
                    e.metricCode(), e.metricType(), e.limitValue(),
                    e.periodType(), e.enforcementMode(), e.isEnabled()
                )).toList()
    );

    AppPlanResult result = createPlanUseCase.execute(cmd);
    AppPlanData data = AppPlanData.from(result.plan(), result.versions(), result.billingOptionsByVersion(), result.entitlements());

    return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.<AppPlanData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.APP_PLAN_CREATED))
        .build());
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────

  private UUID resolveClientAppId(String tenantSlug, String clientId) {
    var tenant = tenantRepo.findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));
    return clientAppRepo.findByClientIdAndTenantId(
            ClientId.of(clientId), tenant.getId())
        .map(app -> app.getId().value())
        .orElseThrow(() -> new ClientAppNotFoundException(clientId));
  }
}
