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
 * URL pattern: /tenants/{tenantSlug}/apps/{clientId}/billing
 *   {tenantSlug} = slug del tenant creado por el Contractor (tiene contractor_id en DB)
 *   {clientId}   = client_id global del proveedor
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}/apps/{clientId}/billing")
@Tag(name = "Billing — Subscription", description = "Subscription & invoice management — requires Bearer JWT with ADMIN_TENANT role")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyRole('ADMIN','ADMIN_TENANT','KEYGO_ADMIN','KEYGO_TENANT_ADMIN') and @tenantAuthorizationEvaluator.hasTenantAccess(authentication)")
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
      description = "Returns the current active subscription for the client app. "
                  + "{tenantSlug} is the tenant created by the Contractor (has contractor_id in DB). "
                  + "Requires ADMIN_TENANT role.")
  @ApiResponse(responseCode = "200", description = "Subscription retrieved (code: APP_SUBSCRIPTION_RETRIEVED)")
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "No subscription found for this contractor (code: SUBSCRIPTION_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<AppSubscriptionData>> getSubscription(
      @Parameter(description = "Tenant slug (creado por el Contractor)") @PathVariable String tenantSlug,
      @Parameter(description = "Client app client_id (proveedor)") @PathVariable String clientId) {

    UUID contractorId = resolveContractorIdFromTenant(tenantSlug);
    UUID appId = resolveAppIdGlobally(clientId);

    AppSubscription sub = getSubscriptionUseCase.executeForContractor(appId, contractorId);
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
  @ApiResponse(responseCode = "200", description = "Cancellation scheduled (code: APP_SUBSCRIPTION_CANCELLED)")
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "No subscription found for this contractor (code: SUBSCRIPTION_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "422", description = "Subscription is not active and cannot be cancelled (code: SUBSCRIPTION_INVALID_STATE)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<AppSubscriptionData>> cancelSubscription(
      @Parameter(description = "Tenant slug (creado por el Contractor)") @PathVariable String tenantSlug,
      @Parameter(description = "Client app client_id (proveedor)") @PathVariable String clientId) {

    UUID contractorId = resolveContractorIdFromTenant(tenantSlug);
    UUID appId = resolveAppIdGlobally(clientId);

    AppSubscription sub = cancelSubscriptionUseCase.executeForContractor(appId, contractorId);
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
  @ApiResponse(responseCode = "200", description = "Invoices retrieved")
  @ApiResponse(responseCode = "404", description = "Tenant or client app not found",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<List<AppInvoiceData>>> listInvoices(
      @Parameter(description = "Tenant slug (creado por el Contractor)") @PathVariable String tenantSlug,
      @Parameter(description = "Client app client_id (proveedor)") @PathVariable String clientId) {

    UUID contractorId = resolveContractorIdFromTenant(tenantSlug);
    UUID appId = resolveAppIdGlobally(clientId);

    AppSubscription sub = getSubscriptionUseCase.executeForContractor(appId, contractorId);
    List<AppInvoiceData> invoices = listInvoicesUseCase.execute(sub.getId())
        .stream().map(AppInvoiceData::from).toList();

    return ResponseEntity.ok(BaseResponse.<List<AppInvoiceData>>builder()
        .data(invoices)
        .success(ResponseHelper.message(ResponseCode.APP_INVOICE_LIST_RETRIEVED))
        .build());
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────

  /**
   * Resuelve el contractorId a partir del tenant creado por el Contractor.
   * El tenant tiene contractor_id != NULL cuando fue creado por un Contractor.
   */
  private UUID resolveContractorIdFromTenant(String tenantSlug) {
    return tenantRepo.findBySlug(TenantSlug.of(tenantSlug))
        .map(t -> {
          if (t.getContractorId() == null) {
            throw new IllegalArgumentException(
                "El tenant '" + tenantSlug + "' no está asociado a ningún Contractor");
          }
          return t.getContractorId();
        })
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));
  }

  /**
   * Resuelve el UUID interno de la ClientApp por su clientId globalmente.
   */
  private UUID resolveAppIdGlobally(String clientId) {
    return clientAppRepo.findByClientId(ClientId.of(clientId))
        .map(app -> app.getId().value())
        .orElseThrow(() -> new ClientAppNotFoundException(clientId));
  }
}
