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
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for app billing plan catalog endpoints.
 * Public: GET /billing/catalog, GET /billing/catalog/{planCode}
 * Admin:  POST /billing/plans, GET /billing/plans
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}/apps/{clientId}")
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

  /** GET /billing/catalog — public catalog (optionally filtered by subscriberType) */
  @GetMapping("/billing/catalog")
  public ResponseEntity<BaseResponse<List<AppPlanData>>> getCatalog(
      @PathVariable String tenantSlug,
      @PathVariable String clientId,
      @RequestParam(required = false) SubscriberType subscriberType) {

    UUID appId = resolveClientAppId(tenantSlug, clientId);
    List<AppPlanData> data = getCatalogUseCase.execute(appId, subscriberType)
        .stream().map(r -> AppPlanData.from(r.plan(), r.versions(), r.entitlements())).toList();

    return ResponseEntity.ok(BaseResponse.<List<AppPlanData>>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.APP_PLAN_CATALOG_RETRIEVED))
        .build());
  }

  /** GET /billing/catalog/{planCode} — public plan detail */
  @GetMapping("/billing/catalog/{planCode}")
  public ResponseEntity<BaseResponse<AppPlanData>> getPlanPublic(
      @PathVariable String tenantSlug,
      @PathVariable String clientId,
      @PathVariable String planCode) {

    UUID appId = resolveClientAppId(tenantSlug, clientId);
    AppPlanResult result = getPlanUseCase.execute(appId, planCode);
    AppPlanData data = AppPlanData.from(result.plan(), result.versions(), result.entitlements());

    return ResponseEntity.ok(BaseResponse.<AppPlanData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.APP_PLAN_RETRIEVED))
        .build());
  }

  /** POST /billing/plans — create plan (ADMIN_TENANT) */
  @PostMapping("/billing/plans")
  public ResponseEntity<BaseResponse<AppPlanData>> createPlan(
      @PathVariable String tenantSlug,
      @PathVariable String clientId,
      @RequestBody CreateAppPlanRequest request) {

    UUID appId = resolveClientAppId(tenantSlug, clientId);

    CreateAppPlanCommand cmd = new CreateAppPlanCommand(
        appId,
        request.code(),
        request.name(),
        request.description(),
        request.subscriberType(),
        request.isPublic(),
        request.version(),
        request.billingPeriod(),
        request.basePrice(),
        request.currency(),
        request.trialDays(),
        request.effectiveFrom(),
        request.entitlements() == null ? List.of() :
            request.entitlements().stream().map(e ->
                new CreateAppPlanCommand.EntitlementDef(
                    e.metricCode(), e.metricType(), e.limitValue(),
                    e.periodType(), e.enforcementMode(), e.isEnabled()
                )).toList()
    );

    AppPlanResult result = createPlanUseCase.execute(cmd);
    AppPlanData data = AppPlanData.from(result.plan(), result.versions(), result.entitlements());

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


