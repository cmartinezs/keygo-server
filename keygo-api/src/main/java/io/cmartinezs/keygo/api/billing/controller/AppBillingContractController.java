package io.cmartinezs.keygo.api.billing.controller;

import io.cmartinezs.keygo.api.billing.request.CreateAppContractRequest;
import io.cmartinezs.keygo.api.billing.response.AppContractData;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.app.billing.contracting.command.CreateAppContractCommand;
import io.cmartinezs.keygo.app.billing.contracting.usecase.ActivateAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.CreateAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.GetAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.MockApprovePaymentUseCase;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
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
public class AppBillingContractController {

  private final TenantRepositoryPort tenantRepo;
  private final ClientAppRepositoryPort clientAppRepo;
  private final CreateAppContractUseCase createContractUseCase;
  private final GetAppContractUseCase getContractUseCase;
  private final MockApprovePaymentUseCase mockApprovePaymentUseCase;
  private final ActivateAppContractUseCase activateContractUseCase;

  public AppBillingContractController(
      TenantRepositoryPort tenantRepo,
      ClientAppRepositoryPort clientAppRepo,
      CreateAppContractUseCase createContractUseCase,
      GetAppContractUseCase getContractUseCase,
      MockApprovePaymentUseCase mockApprovePaymentUseCase,
      ActivateAppContractUseCase activateContractUseCase) {
    this.tenantRepo = tenantRepo;
    this.clientAppRepo = clientAppRepo;
    this.createContractUseCase = createContractUseCase;
    this.getContractUseCase = getContractUseCase;
    this.mockApprovePaymentUseCase = mockApprovePaymentUseCase;
    this.activateContractUseCase = activateContractUseCase;
  }

  /** POST /billing/contracts — initiate a new contract */
  @PostMapping
  public ResponseEntity<BaseResponse<AppContractData>> createContract(
      @PathVariable String tenantSlug,
      @PathVariable String clientId,
      @RequestBody CreateAppContractRequest request) {

    UUID appId = resolveClientAppId(tenantSlug, clientId);

    CreateAppContractCommand cmd = new CreateAppContractCommand(
        appId,
        UUID.fromString(request.planVersionId()),
        request.billingPeriod(),
        request.subscriberType(),
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
  public ResponseEntity<BaseResponse<AppContractData>> getContract(
      @PathVariable String tenantSlug,
      @PathVariable String clientId,
      @PathVariable UUID contractId) {

    var result = getContractUseCase.execute(contractId);
    return ResponseEntity.ok(BaseResponse.<AppContractData>builder()
        .data(AppContractData.from(result.contract()))
        .success(ResponseHelper.message(ResponseCode.APP_CONTRACT_RETRIEVED))
        .build());
  }

  /** POST /billing/contracts/{contractId}/mock-approve-payment — dev only */
  @PostMapping("/{contractId}/mock-approve-payment")
  public ResponseEntity<BaseResponse<AppContractData>> mockApprovePayment(
      @PathVariable String tenantSlug,
      @PathVariable String clientId,
      @PathVariable UUID contractId) {

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
  public ResponseEntity<BaseResponse<AppContractData>> activateContract(
      @PathVariable String tenantSlug,
      @PathVariable String clientId,
      @PathVariable UUID contractId) {

    var result = activateContractUseCase.execute(contractId);
    return ResponseEntity.ok(BaseResponse.<AppContractData>builder()
        .data(AppContractData.from(result.contract()))
        .success(ResponseHelper.message(ResponseCode.APP_CONTRACT_ACTIVATED))
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


