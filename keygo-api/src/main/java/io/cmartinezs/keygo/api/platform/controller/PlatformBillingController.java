package io.cmartinezs.keygo.api.platform.controller;

import io.cmartinezs.keygo.api.billing.response.AppInvoiceData;
import io.cmartinezs.keygo.api.billing.response.AppPlanData;
import io.cmartinezs.keygo.api.billing.response.AppSubscriptionData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.billing.platform.usecase.CancelPlatformSubscriptionUseCase;
import io.cmartinezs.keygo.app.billing.platform.usecase.GetPlatformPlanCatalogUseCase;
import io.cmartinezs.keygo.app.billing.platform.usecase.GetPlatformPlanUseCase;
import io.cmartinezs.keygo.app.billing.platform.usecase.GetPlatformSubscriptionUseCase;
import io.cmartinezs.keygo.app.billing.platform.usecase.ListPlatformInvoicesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for platform-level billing: catalog, subscription and invoices.
 * <p>Controlador REST para billing a nivel de plataforma: catálogo, suscripción y facturas.
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/platform/billing")
@Tag(name = "Platform Billing", description = "Platform-level billing: catalog, subscription, invoices")
public class PlatformBillingController {

  private final GetPlatformPlanCatalogUseCase getPlanCatalogUseCase;
  private final GetPlatformPlanUseCase getPlanUseCase;
  private final GetPlatformSubscriptionUseCase getSubscriptionUseCase;
  private final CancelPlatformSubscriptionUseCase cancelSubscriptionUseCase;
  private final ListPlatformInvoicesUseCase listInvoicesUseCase;

  public PlatformBillingController(
      GetPlatformPlanCatalogUseCase getPlanCatalogUseCase,
      GetPlatformPlanUseCase getPlanUseCase,
      GetPlatformSubscriptionUseCase getSubscriptionUseCase,
      CancelPlatformSubscriptionUseCase cancelSubscriptionUseCase,
      ListPlatformInvoicesUseCase listInvoicesUseCase) {
    this.getPlanCatalogUseCase = getPlanCatalogUseCase;
    this.getPlanUseCase = getPlanUseCase;
    this.getSubscriptionUseCase = getSubscriptionUseCase;
    this.cancelSubscriptionUseCase = cancelSubscriptionUseCase;
    this.listInvoicesUseCase = listInvoicesUseCase;
  }

  /** GET /platform/billing/catalog — public platform plan catalog */
  @GetMapping("/catalog")
  @Operation(
      summary = "Get platform plan catalog",
      description = "Returns all active public platform-level plans (clientAppId IS NULL) with versions, billing options and entitlements. No authentication required.")
  @ApiResponse(responseCode = "200", description = "Platform plan catalog retrieved (code: PLATFORM_PLAN_CATALOG_RETRIEVED)")
  public ResponseEntity<BaseResponse<List<AppPlanData>>> getCatalog() {
    List<AppPlanData> data = getPlanCatalogUseCase.execute().stream()
        .map(r -> AppPlanData.from(r.plan(), r.versions(), r.billingOptionsByVersion(), r.entitlements()))
        .toList();

    return ResponseEntity.ok(BaseResponse.<List<AppPlanData>>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.PLATFORM_PLAN_CATALOG_RETRIEVED))
        .build());
  }

  /** GET /platform/billing/catalog/{planCode} — public platform plan detail */
  @GetMapping("/catalog/{planCode}")
  @Operation(
      summary = "Get platform plan detail",
      description = "Returns a single platform-level plan by code. No authentication required.")
  @ApiResponse(responseCode = "200", description = "Platform plan retrieved (code: PLATFORM_PLAN_RETRIEVED)")
  @ApiResponse(responseCode = "404", description = "Plan not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<AppPlanData>> getPlan(
      @Parameter(description = "Plan code (e.g. FREE, PERSONAL, TEAM)") @PathVariable String planCode) {

    var result = getPlanUseCase.execute(planCode);
    AppPlanData data = AppPlanData.from(result.plan(), result.versions(), result.billingOptionsByVersion(), result.entitlements());

    return ResponseEntity.ok(BaseResponse.<AppPlanData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.PLATFORM_PLAN_RETRIEVED))
        .build());
  }

  /** GET /platform/billing/subscription — authenticated, KEYGO_ADMIN or KEYGO_ACCOUNT_ADMIN */
  @GetMapping("/subscription")
  @SecurityRequirement(name = "BearerAuth")
  @PreAuthorize("hasAnyRole('KEYGO_ADMIN','KEYGO_ACCOUNT_ADMIN')")
  @Operation(
      summary = "Get platform subscription",
      description = "Returns the active platform subscription for the authenticated user's contractor.")
  @ApiResponse(responseCode = "200", description = "Platform subscription retrieved (code: PLATFORM_SUBSCRIPTION_RETRIEVED)")
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Contractor or subscription not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<AppSubscriptionData>> getSubscription(Authentication authentication) {
    UUID platformUserId = extractPlatformUserId(authentication);
    var subscription = getSubscriptionUseCase.execute(platformUserId);

    return ResponseEntity.ok(BaseResponse.<AppSubscriptionData>builder()
        .data(AppSubscriptionData.from(subscription))
        .success(ResponseHelper.message(ResponseCode.PLATFORM_SUBSCRIPTION_RETRIEVED))
        .build());
  }

  /** POST /platform/billing/subscription/cancel — authenticated, KEYGO_ADMIN or KEYGO_ACCOUNT_ADMIN */
  @PostMapping("/subscription/cancel")
  @SecurityRequirement(name = "BearerAuth")
  @PreAuthorize("hasAnyRole('KEYGO_ADMIN','KEYGO_ACCOUNT_ADMIN')")
  @Operation(
      summary = "Cancel platform subscription",
      description = "Marks the platform subscription for cancellation at the end of the current billing period.")
  @ApiResponse(responseCode = "200", description = "Subscription marked for cancellation (code: PLATFORM_SUBSCRIPTION_CANCELLED)")
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Contractor or subscription not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "422", description = "Subscription not active (code: SUBSCRIPTION_INVALID_STATE)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<AppSubscriptionData>> cancelSubscription(Authentication authentication) {
    UUID platformUserId = extractPlatformUserId(authentication);
    var subscription = cancelSubscriptionUseCase.execute(platformUserId);

    return ResponseEntity.ok(BaseResponse.<AppSubscriptionData>builder()
        .data(AppSubscriptionData.from(subscription))
        .success(ResponseHelper.message(ResponseCode.PLATFORM_SUBSCRIPTION_CANCELLED))
        .build());
  }

  /** GET /platform/billing/invoices — authenticated, KEYGO_ADMIN or KEYGO_ACCOUNT_ADMIN */
  @GetMapping("/invoices")
  @SecurityRequirement(name = "BearerAuth")
  @PreAuthorize("hasAnyRole('KEYGO_ADMIN','KEYGO_ACCOUNT_ADMIN')")
  @Operation(
      summary = "List platform invoices",
      description = "Returns all invoices for the platform subscription of the authenticated user's contractor.")
  @ApiResponse(responseCode = "200", description = "Platform invoices retrieved (code: PLATFORM_INVOICES_RETRIEVED)")
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Contractor or subscription not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<List<AppInvoiceData>>> listInvoices(Authentication authentication) {
    UUID platformUserId = extractPlatformUserId(authentication);
    List<AppInvoiceData> data = listInvoicesUseCase.execute(platformUserId).stream()
        .map(AppInvoiceData::from)
        .toList();

    return ResponseEntity.ok(BaseResponse.<List<AppInvoiceData>>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.PLATFORM_INVOICES_RETRIEVED))
        .build());
  }

  private UUID extractPlatformUserId(Authentication auth) {
    return UUID.fromString(auth.getName());
  }
}
