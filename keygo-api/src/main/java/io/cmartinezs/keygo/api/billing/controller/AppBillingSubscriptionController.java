package io.cmartinezs.keygo.api.billing.controller;

import io.cmartinezs.keygo.api.billing.response.AppInvoiceData;
import io.cmartinezs.keygo.api.billing.response.AppSubscriptionData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.billing.invoice.usecase.ListAppInvoicesUseCase;
import io.cmartinezs.keygo.app.billing.subscription.usecase.CancelAppSubscriptionUseCase;
import io.cmartinezs.keygo.app.billing.subscription.usecase.GetAppSubscriptionUseCase;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for subscription and invoice management within a client app.
 * Requires Bearer token with ADMIN_TENANT role.
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}/apps/{clientId}/billing")
@Tag(name = "Billing — Subscription", description = "Subscription & invoice management — requires Bearer JWT with ADMIN_TENANT role")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyRole('ADMIN','ADMIN_TENANT') and @tenantAuthorizationEvaluator.hasTenantAccess(authentication)")
public class AppBillingSubscriptionController {

  private final TenantRepositoryPort tenantRepo;
  private final ClientAppRepositoryPort clientAppRepo;
  private final GetAppSubscriptionUseCase getSubscriptionUseCase;
  private final CancelAppSubscriptionUseCase cancelSubscriptionUseCase;
  private final ListAppInvoicesUseCase listInvoicesUseCase;

  public AppBillingSubscriptionController(
      TenantRepositoryPort tenantRepo,
      ClientAppRepositoryPort clientAppRepo,
      GetAppSubscriptionUseCase getSubscriptionUseCase,
      CancelAppSubscriptionUseCase cancelSubscriptionUseCase,
      ListAppInvoicesUseCase listInvoicesUseCase) {
    this.tenantRepo = tenantRepo;
    this.clientAppRepo = clientAppRepo;
    this.getSubscriptionUseCase = getSubscriptionUseCase;
    this.cancelSubscriptionUseCase = cancelSubscriptionUseCase;
    this.listInvoicesUseCase = listInvoicesUseCase;
  }

  /** GET /billing/subscription — active subscription */
  @GetMapping("/subscription")
  @Operation(
      summary = "Get active subscription",
      description = "Returns the current active subscription for the client app. Requires ADMIN_TENANT role.")
  @ApiResponse(responseCode = "200", description = "Subscription retrieved",
      content = @Content(schema = @Schema(implementation = AppSubscriptionData.Response.class)))
  @ApiResponse(responseCode = "404", description = "No active subscription found",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  public ResponseEntity<BaseResponse<AppSubscriptionData>> getSubscription(
      @Parameter(description = "Tenant slug (suscriptor)") @PathVariable String tenantSlug,
      @Parameter(description = "Client app client_id (proveedor)") @PathVariable String clientId) {

    // {tenantSlug} = tenant del SUSCRIPTOR — identifica quién es el suscriptor
    // {clientId}   = clientId del PROVEEDOR — se resuelve globalmente (no pertenece al suscriptor)
    UUID tenantId = resolveTenantId(tenantSlug);
    UUID appId    = resolveAppIdGlobally(clientId);

    AppSubscription sub = getSubscriptionUseCase.execute(appId, SubscriberType.TENANT, tenantId);
    return ResponseEntity.ok(BaseResponse.<AppSubscriptionData>builder()
        .data(AppSubscriptionData.from(sub))
        .success(ResponseHelper.message(ResponseCode.APP_SUBSCRIPTION_RETRIEVED))
        .build());
  }

  /** POST /billing/subscription/cancel — schedule cancellation at period end */
  @PostMapping("/subscription/cancel")
  @Operation(
      summary = "Cancel subscription at period end",
      description = "Marks the active subscription for cancellation at the end of the current billing period. Requires ADMIN_TENANT role.")
  @ApiResponse(responseCode = "200", description = "Cancellation scheduled",
      content = @Content(schema = @Schema(implementation = AppSubscriptionData.Response.class)))
  @ApiResponse(responseCode = "404", description = "No active subscription found",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  public ResponseEntity<BaseResponse<AppSubscriptionData>> cancelSubscription(
      @Parameter(description = "Tenant slug (suscriptor)") @PathVariable String tenantSlug,
      @Parameter(description = "Client app client_id (proveedor)") @PathVariable String clientId) {

    UUID tenantId = resolveTenantId(tenantSlug);
    UUID appId    = resolveAppIdGlobally(clientId);

    AppSubscription sub = cancelSubscriptionUseCase.execute(appId, SubscriberType.TENANT, tenantId);
    return ResponseEntity.ok(BaseResponse.<AppSubscriptionData>builder()
        .data(AppSubscriptionData.from(sub))
        .success(ResponseHelper.message(ResponseCode.APP_SUBSCRIPTION_CANCELLED))
        .build());
  }

  /** GET /billing/invoices — list all invoices */
  @GetMapping("/invoices")
  @Operation(
      summary = "List invoices",
      description = "Returns all invoices associated with the client app subscription. Requires ADMIN_TENANT role.")
  @ApiResponse(responseCode = "200", description = "Invoices retrieved",
      content = @Content(schema = @Schema(implementation = AppInvoiceData.ListResponse.class)))
  @ApiResponse(responseCode = "404", description = "Tenant or client app not found",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  public ResponseEntity<BaseResponse<List<AppInvoiceData>>> listInvoices(
      @Parameter(description = "Tenant slug (suscriptor)") @PathVariable String tenantSlug,
      @Parameter(description = "Client app client_id (proveedor)") @PathVariable String clientId) {

    UUID tenantId = resolveTenantId(tenantSlug);
    UUID appId    = resolveAppIdGlobally(clientId);

    AppSubscription sub = getSubscriptionUseCase.execute(appId, SubscriberType.TENANT, tenantId);
    List<AppInvoiceData> invoices = listInvoicesUseCase.execute(sub.getId())
        .stream().map(AppInvoiceData::from).toList();

    return ResponseEntity.ok(BaseResponse.<List<AppInvoiceData>>builder()
        .data(invoices)
        .success(ResponseHelper.message(ResponseCode.APP_INVOICE_LIST_RETRIEVED))
        .build());
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────

  private UUID resolveTenantId(String tenantSlug) {
    return tenantRepo.findBySlug(TenantSlug.of(tenantSlug))
        .map(t -> t.getId().value())
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));
  }

  /**
   * Resuelve el UUID interno de la ClientApp por su clientId (OAuth2 client_id) globalmente.
   * El clientId es globalmente único y pertenece al PROVEEDOR, no al suscriptor.
   * Usar este método (no findByClientIdAndTenantId) para los endpoints de gestión de suscripción.
   */
  private UUID resolveAppIdGlobally(String clientId) {
    return clientAppRepo.findByClientId(ClientId.of(clientId))
        .map(app -> app.getId().value())
        .orElseThrow(() -> new ClientAppNotFoundException(clientId));
  }
}
