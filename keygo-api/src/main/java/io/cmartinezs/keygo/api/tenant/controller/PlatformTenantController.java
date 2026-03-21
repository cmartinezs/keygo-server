package io.cmartinezs.keygo.api.tenant.controller;

import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.tenant.request.CreateTenantRequest;
import io.cmartinezs.keygo.api.tenant.response.TenantData;
import io.cmartinezs.keygo.app.tenant.command.CreateTenantCommand;
import io.cmartinezs.keygo.app.tenant.usecase.CreateTenantUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.GetTenantBySlugUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.SuspendTenantUseCase;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for platform-level tenant management operations.
 * Controlador REST para operaciones de gestión de tenants a nivel de plataforma.
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/tenants")
public class PlatformTenantController {

  private final CreateTenantUseCase createTenantUseCase;
  private final GetTenantBySlugUseCase getTenantBySlugUseCase;
  private final SuspendTenantUseCase suspendTenantUseCase;

  public PlatformTenantController(
      CreateTenantUseCase createTenantUseCase,
      GetTenantBySlugUseCase getTenantBySlugUseCase,
      SuspendTenantUseCase suspendTenantUseCase) {
    this.createTenantUseCase = createTenantUseCase;
    this.getTenantBySlugUseCase = getTenantBySlugUseCase;
    this.suspendTenantUseCase = suspendTenantUseCase;
  }

  /**
   * Create a new tenant.
   * Crear un nuevo tenant.
   *
   * @param request the creation request / la solicitud de creación
   * @return 201 Created with the tenant data / 201 Created con los datos del tenant
   */
  @PostMapping
  public ResponseEntity<BaseResponse<TenantData>> createTenant(
      @Valid @RequestBody CreateTenantRequest request) {

    Tenant tenant = createTenantUseCase.execute(
        new CreateTenantCommand(request.name(), request.slug(), request.ownerEmail()));

    BaseResponse<TenantData> response = BaseResponse.<TenantData>builder()
        .data(toData(tenant))
        .success(ResponseHelper.message(ResponseCode.TENANT_CREATED))
        .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Retrieve a tenant by its slug.
   * Obtener un tenant por su slug.
   *
   * @param slug the tenant slug / el slug del tenant
   * @return 200 OK with the tenant data / 200 OK con los datos del tenant
   */
  @GetMapping("/{slug}")
  public ResponseEntity<BaseResponse<TenantData>> getTenantBySlug(@PathVariable String slug) {

    Tenant tenant = getTenantBySlugUseCase.execute(slug);

    BaseResponse<TenantData> response = BaseResponse.<TenantData>builder()
        .data(toData(tenant))
        .success(ResponseHelper.message(ResponseCode.TENANT_RETRIEVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  /**
   * Suspend an existing tenant.
   * Suspender un tenant existente.
   *
   * @param slug the tenant slug / el slug del tenant
   * @return 200 OK with the suspended tenant data / 200 OK con los datos del tenant suspendido
   */
  @PutMapping("/{slug}/suspend")
  public ResponseEntity<BaseResponse<TenantData>> suspendTenant(@PathVariable String slug) {

    Tenant tenant = suspendTenantUseCase.execute(slug);

    BaseResponse<TenantData> response = BaseResponse.<TenantData>builder()
        .data(toData(tenant))
        .success(ResponseHelper.message(ResponseCode.TENANT_SUSPENDED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  // ─── Private helpers ──────────────────────────────────────────────────────

  private TenantData toData(Tenant tenant) {
    return TenantData.builder()
        .id(tenant.getId().toString())
        .name(tenant.getName())
        .slug(tenant.getSlug().value())
        .ownerEmail(tenant.getOwnerEmail())
        .status(tenant.getStatus().name())
        .build();
  }
}

