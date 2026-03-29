package io.cmartinezs.keygo.api.billing.controller;

import io.cmartinezs.keygo.api.billing.response.AppSubscriptionData;
import io.cmartinezs.keygo.api.billing.response.AppInvoiceData;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for billing subscription and invoice endpoints.
 * All endpoints require Bearer ADMIN_TENANT.
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}/apps/{clientId}/billing")
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

  /** GET /billing/subscription — active subscription for this tenant toward the app */
  @GetMapping("/subscription")
  public ResponseEntity<BaseResponse<AppSubscriptionData>> getSubscription(
      @PathVariable String tenantSlug,
      @PathVariable String clientId) {

    UUID tenantId = resolveTenantId(tenantSlug);
    UUID appId = resolveClientAppId(tenantSlug, clientId);

    AppSubscription sub = getSubscriptionUseCase.execute(appId, SubscriberType.TENANT, tenantId);
    return ResponseEntity.ok(BaseResponse.<AppSubscriptionData>builder()
        .data(AppSubscriptionData.from(sub))
        .success(ResponseHelper.message(ResponseCode.APP_SUBSCRIPTION_RETRIEVED))
        .build());
  }

  /** POST /billing/subscription/cancel — mark subscription for cancellation at period end */
  @PostMapping("/subscription/cancel")
  public ResponseEntity<BaseResponse<AppSubscriptionData>> cancelSubscription(
      @PathVariable String tenantSlug,
      @PathVariable String clientId) {

    UUID tenantId = resolveTenantId(tenantSlug);
    UUID appId = resolveClientAppId(tenantSlug, clientId);

    AppSubscription sub = cancelSubscriptionUseCase.execute(appId, SubscriberType.TENANT, tenantId);
    return ResponseEntity.ok(BaseResponse.<AppSubscriptionData>builder()
        .data(AppSubscriptionData.from(sub))
        .success(ResponseHelper.message(ResponseCode.APP_SUBSCRIPTION_CANCELLED))
        .build());
  }

  /** GET /billing/invoices — list invoices for this tenant's subscription */
  @GetMapping("/invoices")
  public ResponseEntity<BaseResponse<List<AppInvoiceData>>> listInvoices(
      @PathVariable String tenantSlug,
      @PathVariable String clientId) {

    UUID tenantId = resolveTenantId(tenantSlug);
    UUID appId = resolveClientAppId(tenantSlug, clientId);

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

  private UUID resolveClientAppId(String tenantSlug, String clientId) {
    var tenant = tenantRepo.findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));
    return clientAppRepo.findByClientIdAndTenantId(ClientId.of(clientId), tenant.getId())
        .map(app -> app.getId().value())
        .orElseThrow(() -> new ClientAppNotFoundException(clientId));
  }
}


