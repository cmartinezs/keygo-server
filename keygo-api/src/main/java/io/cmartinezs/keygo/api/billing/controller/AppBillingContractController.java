package io.cmartinezs.keygo.api.billing.controller;

import io.cmartinezs.keygo.api.billing.request.CreateAppContractRequest;
import io.cmartinezs.keygo.api.billing.request.VerifyContractEmailRequest;
import io.cmartinezs.keygo.api.billing.response.AppContractData;
import io.cmartinezs.keygo.api.billing.response.AppContractResumeData;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.app.billing.contracting.command.CreateAppContractCommand;
import io.cmartinezs.keygo.app.billing.contracting.usecase.ActivateAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.CreateAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.GetAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.MockApprovePaymentUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.ResendContractVerificationUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.ResumeContractOnboardingUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.VerifyContractEmailUseCase;
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
 * <p>Billing model v2: contracts are standalone entities — no tenantSlug/clientId in the path.
 * The target client app is identified by {@code clientAppId} in the create request body.
 *
 * @author cmartinezs
 * @version 2.0
 */
@RestController
@RequestMapping("/api/v1/billing/contracts")
@Tag(name = "Billing — Contracts", description = "Self-service contracting flow — no auth required (email & payment verification built into the flow)")
public class AppBillingContractController {

  private final CreateAppContractUseCase createContractUseCase;
  private final GetAppContractUseCase getContractUseCase;
  private final MockApprovePaymentUseCase mockApprovePaymentUseCase;
  private final ActivateAppContractUseCase activateContractUseCase;
  private final VerifyContractEmailUseCase verifyContractEmailUseCase;
  private final ResumeContractOnboardingUseCase resumeContractOnboardingUseCase;
  private final ResendContractVerificationUseCase resendContractVerificationUseCase;

  public AppBillingContractController(
      CreateAppContractUseCase createContractUseCase,
      GetAppContractUseCase getContractUseCase,
      MockApprovePaymentUseCase mockApprovePaymentUseCase,
      ActivateAppContractUseCase activateContractUseCase,
      VerifyContractEmailUseCase verifyContractEmailUseCase,
      ResumeContractOnboardingUseCase resumeContractOnboardingUseCase,
      ResendContractVerificationUseCase resendContractVerificationUseCase) {
    this.createContractUseCase = createContractUseCase;
    this.getContractUseCase = getContractUseCase;
    this.mockApprovePaymentUseCase = mockApprovePaymentUseCase;
    this.activateContractUseCase = activateContractUseCase;
    this.verifyContractEmailUseCase = verifyContractEmailUseCase;
    this.resumeContractOnboardingUseCase = resumeContractOnboardingUseCase;
    this.resendContractVerificationUseCase = resendContractVerificationUseCase;
  }

  /** POST /billing/contracts — initiate a new contract */
  @PostMapping
  @Operation(
      summary = "Initiate a subscription contract",
      description = "Starts the contracting flow: creates a contract in PENDING_EMAIL_VERIFICATION status. "
                  + "The target client app is identified by clientAppId in the request body.")
  @ApiResponse(responseCode = "201", description = "Contract initiated (code: APP_CONTRACT_CREATED)")
  @ApiResponse(responseCode = "404", description = "Plan version not found (code: PLAN_VERSION_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "409", description = "A contractor with this email already exists (code: CONTRACTOR_EMAIL_ALREADY_EXISTS)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<AppContractData>> createContract(
      @RequestBody CreateAppContractRequest request) {

    CreateAppContractCommand cmd = new CreateAppContractCommand(
        UUID.fromString(request.clientAppId()),
        UUID.fromString(request.planVersionId()),
        request.billingPeriod(),
        request.contractorEmail(),
        request.contractorFirstName(),
        request.contractorLastName(),
        request.companyName(),
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
  @ApiResponse(responseCode = "200", description = "Contract retrieved (code: APP_CONTRACT_RETRIEVED)")
  @ApiResponse(responseCode = "404", description = "Contract not found (code: CONTRACT_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<AppContractData>> getContract(
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
  @ApiResponse(responseCode = "200", description = "Payment approved (code: APP_CONTRACT_PAYMENT_APPROVED)")
  @ApiResponse(responseCode = "404", description = "Contract not found or mock disabled (code: CONTRACT_NOT_FOUND / RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "422", description = "Contract already ACTIVE or CANCELLED, cannot approve (code: CONTRACT_INVALID_STATE)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<AppContractData>> mockApprovePayment(
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
      description = "Activates a READY_TO_ACTIVATE contract: creates the subscription and first invoice.")
  @ApiResponse(responseCode = "200", description = "Contract activated (code: APP_CONTRACT_ACTIVATED)")
  @ApiResponse(responseCode = "404", description = "Contract or plan version not found (code: CONTRACT_NOT_FOUND / PLAN_VERSION_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "422", description = "Contract is not in READY_TO_ACTIVATE state, payment not verified, or no contractor linked (code: CONTRACT_INVALID_STATE)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<AppContractData>> activateContract(
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
  @ApiResponse(responseCode = "200", description = "Email verified — contract now in PENDING_PAYMENT (code: APP_CONTRACT_EMAIL_VERIFIED)")
  @ApiResponse(responseCode = "400", description = "Verification code is invalid or already used (code: INVALID_INPUT)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Contract or provider app not found (code: CONTRACT_NOT_FOUND / PROVIDER_APP_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "422", description = "Verification code has expired — resend a new one (code: EMAIL_VERIFICATION_EXPIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<AppContractData>> verifyEmail(
      @Parameter(description = "Contract UUID") @PathVariable UUID contractId,
      @RequestBody VerifyContractEmailRequest request) {

    var result = verifyContractEmailUseCase.execute(contractId, request.code());
    return ResponseEntity.ok(BaseResponse.<AppContractData>builder()
        .data(AppContractData.from(result.contract()))
        .success(ResponseHelper.message(ResponseCode.APP_CONTRACT_EMAIL_VERIFIED))
        .build());
  }

  /** GET /billing/contracts/{contractId}/resume — restore onboarding state */
  @GetMapping("/{contractId}/resume")
  @Operation(
      summary = "Resume contract onboarding",
      description = "Returns the current onboarding state of a contract with UI-oriented hints "
                  + "(`nextAction`, `verificationCodeExpired`). "
                  + "Use this endpoint to restore the frontend flow when the user returns after closing the page. "
                  + "Returns 400 if the contract is in a terminal state (EXPIRED, CANCELLED, FAILED, SUPERSEDED, FINALIZED).")
  @ApiResponse(responseCode = "200", description = "Onboarding state retrieved successfully (code: APP_CONTRACT_ONBOARDING_RESUMED)")
  @ApiResponse(responseCode = "404", description = "Contract not found (code: CONTRACT_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "422", description = "Contract is in a terminal state (EXPIRED, CANCELLED, FAILED, SUPERSEDED, FINALIZED) and cannot be resumed (code: CONTRACT_INVALID_STATE)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<AppContractResumeData>> resumeOnboarding(
      @Parameter(description = "Contract UUID") @PathVariable UUID contractId) {

    var result = resumeContractOnboardingUseCase.execute(contractId);
    return ResponseEntity.ok(BaseResponse.<AppContractResumeData>builder()
        .data(AppContractResumeData.from(result.contract()))
        .success(ResponseHelper.message(ResponseCode.APP_CONTRACT_ONBOARDING_RESUMED))
        .build());
  }

  /** POST /billing/contracts/{contractId}/resend-verification — resend email verification code */
  @PostMapping("/{contractId}/resend-verification")
  @Operation(
      summary = "Resend contract verification code",
      description = "Resends the 6-digit verification code to the contractor's email. "
                  + "If the current code is still valid, the **same code** is resent (no regeneration). "
                  + "If the code has already expired, a **new code** is generated and the contract updated. "
                  + "Only valid when the contract is in `PENDING_EMAIL_VERIFICATION` status.")
  @ApiResponse(responseCode = "200", description = "Verification code resent successfully (code: APP_CONTRACT_VERIFICATION_RESENT)")
  @ApiResponse(responseCode = "404", description = "Contract not found (code: CONTRACT_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "422", description = "Contract is not in PENDING_EMAIL_VERIFICATION state (code: CONTRACT_INVALID_STATE)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<AppContractData>> resendVerification(
      @Parameter(description = "Contract UUID") @PathVariable UUID contractId) {

    var result = resendContractVerificationUseCase.execute(contractId);
    return ResponseEntity.ok(BaseResponse.<AppContractData>builder()
        .data(AppContractData.from(result.contract()))
        .success(ResponseHelper.message(ResponseCode.APP_CONTRACT_VERIFICATION_RESENT))
        .build());
  }
}
