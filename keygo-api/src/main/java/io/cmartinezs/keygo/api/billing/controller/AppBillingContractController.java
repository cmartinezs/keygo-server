package io.cmartinezs.keygo.api.billing.controller;

import io.cmartinezs.keygo.api.billing.request.CreateAppContractRequest;
import io.cmartinezs.keygo.api.billing.request.VerifyContractEmailRequest;
import io.cmartinezs.keygo.api.billing.response.AppContractData;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.app.billing.contracting.command.CreateAppContractCommand;
import io.cmartinezs.keygo.app.billing.contracting.usecase.ActivateAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.CreateAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.GetAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.MockApprovePaymentUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.VerifyContractEmailUseCase;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for app billing contract (contracting flow) endpoints.
 * All endpoints are public (email verification + payment guard the flow).
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}/apps/{clientId}/billing/contracts")
@Tag(name = "Billing — Contracts", description = "Self-service contracting flow — no auth required (email & payment verification built into the flow)")
public class AppBillingContractController {

  private final TenantRepositoryPort tenantRepo;
  private final ClientAppRepositoryPort clientAppRepo;
  private final CreateAppContractUseCase createContractUseCase;
  private final GetAppContractUseCase getContractUseCase;
  private final MockApprovePaymentUseCase mockApprovePaymentUseCase;
  private final ActivateAppContractUseCase activateContractUseCase;
  private final VerifyContractEmailUseCase verifyContractEmailUseCase;

  public AppBillingContractController(
      TenantRepositoryPort tenantRepo,
      ClientAppRepositoryPort clientAppRepo,
      CreateAppContractUseCase createContractUseCase,
      GetAppContractUseCase getContractUseCase,
      MockApprovePaymentUseCase mockApprovePaymentUseCase,
      ActivateAppContractUseCase activateContractUseCase,
      VerifyContractEmailUseCase verifyContractEmailUseCase) {
    this.tenantRepo = tenantRepo;
    this.clientAppRepo = clientAppRepo;
    this.createContractUseCase = createContractUseCase;
    this.getContractUseCase = getContractUseCase;
    this.mockApprovePaymentUseCase = mockApprovePaymentUseCase;
    this.activateContractUseCase = activateContractUseCase;
    this.verifyContractEmailUseCase = verifyContractEmailUseCase;
  }

  /** POST /billing/contracts — initiate a new contract */
  @PostMapping
  @Operation(
      summary = "Initiate a subscription contract",
      description = "Starts the contracting flow: creates a contract in PENDING_EMAIL_VERIFICATION status. "
                  + "The subscriber must verify their email before payment is collected.")
  @ApiResponse(responseCode = "201", description = "Contract initiated",
      content = @Content(schema = @Schema(implementation = AppContractData.Response.class)))
  @ApiResponse(responseCode = "400", description = "Invalid plan version or duplicate company slug",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "404", description = "Tenant or client app not found",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  public ResponseEntity<BaseResponse<AppContractData>> createContract(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Parameter(description = "Client app client_id") @PathVariable String clientId,
      @RequestBody CreateAppContractRequest request) {

    UUID appId = resolveClientAppId(tenantSlug, clientId);

    CreateAppContractCommand cmd = new CreateAppContractCommand(
        appId,
        UUID.fromString(request.planVersionId()),
        request.billingPeriod(),
        request.contractorEmail(),
        request.contractorFirstName(),
        request.contractorLastName(),
        request.companyName(),
        request.companySlug(),
        request.companyTaxId(),
        request.companyAddress()
    );

    var result = createContractUseCase.execute(cmd);
    return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.<AppContractData>builder()
        .data(AppContractData.from(result.contract()))
        .success(ResponseHelper.message(ResponseCode.APP_CONTRACT_CREATED))
        .build());
  }

  /** GET /billing/contracts/{contractId} — retrieve contract status */
  @GetMapping("/{contractId}")
  @Operation(
      summary = "Get contract status",
      description = "Returns the current status and details of a subscription contract.")
  @ApiResponse(responseCode = "200", description = "Contract retrieved",
      content = @Content(schema = @Schema(implementation = AppContractData.Response.class)))
  @ApiResponse(responseCode = "404", description = "Contract not found",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  public ResponseEntity<BaseResponse<AppContractData>> getContract(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Parameter(description = "Client app client_id") @PathVariable String clientId,
      @Parameter(description = "Contract UUID") @PathVariable UUID contractId) {

    var result = getContractUseCase.execute(contractId);
    return ResponseEntity.ok(BaseResponse.<AppContractData>builder()
        .data(AppContractData.from(result.contract()))
        .success(ResponseHelper.message(ResponseCode.APP_CONTRACT_RETRIEVED))
        .build());
  }

  /** POST /billing/contracts/{contractId}/mock-approve-payment — dev only */
  @PostMapping("/{contractId}/mock-approve-payment")
  @Operation(
      summary = "[DEV] Mock approve payment",
      description = "Simulates a successful payment for a contract in PENDING_PAYMENT status. "
                  + "Only available when `keygo.billing.mock-payment-enabled=true`.")
  @ApiResponse(responseCode = "200", description = "Payment approved (mock)",
      content = @Content(schema = @Schema(implementation = AppContractData.Response.class)))
  @ApiResponse(responseCode = "404", description = "Contract not found or mock disabled",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  public ResponseEntity<BaseResponse<AppContractData>> mockApprovePayment(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Parameter(description = "Client app client_id") @PathVariable String clientId,
      @Parameter(description = "Contract UUID") @PathVariable UUID contractId) {

    if (!mockApprovePaymentUseCase.isMockEnabled()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(BaseResponse.<AppContractData>builder()
          .failure(ResponseHelper.message(ResponseCode.RESOURCE_NOT_FOUND))
          .build());
    }

    var result = mockApprovePaymentUseCase.execute(contractId);
    return ResponseEntity.ok(BaseResponse.<AppContractData>builder()
        .data(AppContractData.from(result.contract()))
        .success(ResponseHelper.message(ResponseCode.APP_CONTRACT_PAYMENT_APPROVED))
        .build());
  }

  /** POST /billing/contracts/{contractId}/activate — activate the contract */
  @PostMapping("/{contractId}/activate")
  @Operation(
      summary = "Activate contract",
      description = "Activates a READY_TO_ACTIVATE contract: creates the tenant/user, subscription and first invoice.")
  @ApiResponse(responseCode = "200", description = "Contract activated",
      content = @Content(schema = @Schema(implementation = AppContractData.Response.class)))
  @ApiResponse(responseCode = "400", description = "Contract not in READY_TO_ACTIVATE status",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "404", description = "Contract not found",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  public ResponseEntity<BaseResponse<AppContractData>> activateContract(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Parameter(description = "Client app client_id") @PathVariable String clientId,
      @Parameter(description = "Contract UUID") @PathVariable UUID contractId) {

    var result = activateContractUseCase.execute(contractId);
    return ResponseEntity.ok(BaseResponse.<AppContractData>builder()
        .data(AppContractData.from(result.contract()))
        .success(ResponseHelper.message(ResponseCode.APP_CONTRACT_ACTIVATED))
        .build());
  }

  /** POST /billing/contracts/{contractId}/verify-email — verify email code */
  @PostMapping("/{contractId}/verify-email")
  @Operation(
      summary = "Verify contract email",
      description = "Validates the 6-digit code sent to the contractor's email. "
                  + "Advances contract status from PENDING_EMAIL_VERIFICATION → PENDING_PAYMENT.")
  @ApiResponse(responseCode = "200", description = "Email verified — contract now in PENDING_PAYMENT",
      content = @Content(schema = @Schema(implementation = AppContractData.Response.class)))
  @ApiResponse(responseCode = "400", description = "Invalid or expired verification code",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "404", description = "Contract not found",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  public ResponseEntity<BaseResponse<AppContractData>> verifyEmail(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Parameter(description = "Client app client_id") @PathVariable String clientId,
      @Parameter(description = "Contract UUID") @PathVariable UUID contractId,
      @RequestBody VerifyContractEmailRequest request) {

    var result = verifyContractEmailUseCase.execute(contractId, request.code());
    return ResponseEntity.ok(BaseResponse.<AppContractData>builder()
        .data(AppContractData.from(result.contract()))
        .success(ResponseHelper.message(ResponseCode.APP_CONTRACT_EMAIL_VERIFIED))
        .build());
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────

  private UUID resolveClientAppId(String tenantSlug, String clientId) {
    var tenant = tenantRepo.findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));
    return clientAppRepo.findByClientIdAndTenantId(ClientId.of(clientId), tenant.getId())
        .map(app -> app.getId().value())
        .orElseThrow(() -> new ClientAppNotFoundException(clientId));
  }
}
