package io.cmartinezs.keygo.api.clientapp.controller;

import io.cmartinezs.keygo.api.clientapp.request.CreateClientAppRequest;
import io.cmartinezs.keygo.api.clientapp.request.UpdateClientAppRequest;
import io.cmartinezs.keygo.api.clientapp.response.ClientAppData;
import io.cmartinezs.keygo.api.clientapp.response.ClientAppSecretData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.clientapp.command.CreateClientAppCommand;
import io.cmartinezs.keygo.app.clientapp.command.UpdateClientAppCommand;
import io.cmartinezs.keygo.app.clientapp.usecase.CreateClientAppResult;
import io.cmartinezs.keygo.app.clientapp.usecase.CreateClientAppUseCase;
import io.cmartinezs.keygo.app.clientapp.usecase.GetClientAppUseCase;
import io.cmartinezs.keygo.app.clientapp.usecase.ListClientAppsUseCase;
import io.cmartinezs.keygo.app.clientapp.usecase.RotateClientSecretUseCase;
import io.cmartinezs.keygo.app.clientapp.usecase.RotateSecretResult;
import io.cmartinezs.keygo.app.clientapp.usecase.UpdateClientAppUseCase;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedScope;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.RedirectUri;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for client application management within a tenant.
 * <p>Controlador REST para la gestión de aplicaciones cliente dentro de un tenant.
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}/apps")
public class TenantClientAppController {

  private final CreateClientAppUseCase createClientAppUseCase;
  private final ListClientAppsUseCase listClientAppsUseCase;
  private final GetClientAppUseCase getClientAppUseCase;
  private final UpdateClientAppUseCase updateClientAppUseCase;
  private final RotateClientSecretUseCase rotateClientSecretUseCase;

  public TenantClientAppController(
      CreateClientAppUseCase createClientAppUseCase,
      ListClientAppsUseCase listClientAppsUseCase,
      GetClientAppUseCase getClientAppUseCase,
      UpdateClientAppUseCase updateClientAppUseCase,
      RotateClientSecretUseCase rotateClientSecretUseCase) {
    this.createClientAppUseCase = createClientAppUseCase;
    this.listClientAppsUseCase = listClientAppsUseCase;
    this.getClientAppUseCase = getClientAppUseCase;
    this.updateClientAppUseCase = updateClientAppUseCase;
    this.rotateClientSecretUseCase = rotateClientSecretUseCase;
  }

  /**
   * Create a new client application under the given tenant.
   * <p>Crea una nueva aplicación cliente bajo el tenant dado.
   * @param tenantSlug the tenant slug
   * @param request the creation request
   * @return 201 Created with clientId and raw secret
   */
  @PostMapping
  public ResponseEntity<BaseResponse<ClientAppSecretData>> createClientApp(
      @PathVariable String tenantSlug,
      @Valid @RequestBody CreateClientAppRequest request) {

    CreateClientAppResult result = createClientAppUseCase.execute(
        new CreateClientAppCommand(
            tenantSlug,
            request.name(),
            request.description(),
            request.type(),
            request.redirectUris(),
            request.grants(),
            request.scopes()));

    ClientAppSecretData data = ClientAppSecretData.builder()
        .clientId(result.app().getClientId().value())
        .clientSecret(result.rawSecret())
        .build();

    BaseResponse<ClientAppSecretData> response = BaseResponse.<ClientAppSecretData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.CLIENT_APP_CREATED))
        .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * List all client applications of the given tenant.
   * <p>Lista todas las aplicaciones cliente del tenant dado.
   * @param tenantSlug the tenant slug
   * @return 200 OK with the list of client apps
   */
  @GetMapping
  public ResponseEntity<BaseResponse<List<ClientAppData>>> listClientApps(
      @PathVariable String tenantSlug) {

    List<ClientApp> apps = listClientAppsUseCase.execute(tenantSlug);

    List<ClientAppData> data = apps.stream()
        .map(this::toData)
        .toList();

    BaseResponse<List<ClientAppData>> response = BaseResponse.<List<ClientAppData>>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.CLIENT_APP_LIST_RETRIEVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  /**
   * Retrieve a specific client application by its clientId.
   * <p>Obtiene una aplicación cliente específica por su clientId.
   * @param tenantSlug the tenant slug
   * @param clientId the OAuth2 client_id
   * @return 200 OK with the client app data
   */
  @GetMapping("/{clientId}")
  public ResponseEntity<BaseResponse<ClientAppData>> getClientApp(
      @PathVariable String tenantSlug,
      @PathVariable String clientId) {

    ClientApp clientApp = getClientAppUseCase.execute(tenantSlug, clientId);

    BaseResponse<ClientAppData> response = BaseResponse.<ClientAppData>builder()
        .data(toData(clientApp))
        .success(ResponseHelper.message(ResponseCode.CLIENT_APP_RETRIEVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  /**
   * Update an existing client application.
   * <p>Actualiza una aplicación cliente existente.
   * @param tenantSlug the tenant slug
   * @param clientId the OAuth2 client_id
   * @param request the update request
   * @return 200 OK with the updated client app data
   */
  @PutMapping("/{clientId}")
  public ResponseEntity<BaseResponse<ClientAppData>> updateClientApp(
      @PathVariable String tenantSlug,
      @PathVariable String clientId,
      @Valid @RequestBody UpdateClientAppRequest request) {

    ClientApp clientApp = updateClientAppUseCase.execute(
        new UpdateClientAppCommand(
            tenantSlug,
            clientId,
            request.name(),
            request.description(),
            request.redirectUris(),
            request.grants(),
            request.scopes()));

    BaseResponse<ClientAppData> response = BaseResponse.<ClientAppData>builder()
        .data(toData(clientApp))
        .success(ResponseHelper.message(ResponseCode.CLIENT_APP_UPDATED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  /**
   * Rotate the client secret of a confidential client application.
   * <p>Rota el secret de cliente de una aplicación cliente confidencial.
   * @param tenantSlug the tenant slug
   * @param clientId the OAuth2 client_id
   * @return 200 OK with the new raw secret
   */
  @PostMapping("/{clientId}/rotate-secret")
  public ResponseEntity<BaseResponse<ClientAppSecretData>> rotateClientSecret(
      @PathVariable String tenantSlug,
      @PathVariable String clientId) {

    RotateSecretResult result = rotateClientSecretUseCase.execute(tenantSlug, clientId);

    ClientAppSecretData data = ClientAppSecretData.builder()
        .clientId(result.app().getClientId().value())
        .clientSecret(result.newRawSecret())
        .build();

    BaseResponse<ClientAppSecretData> response = BaseResponse.<ClientAppSecretData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.CLIENT_APP_SECRET_ROTATED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  // ─── Private helpers ──────────────────────────────────────────────────────

  private ClientAppData toData(ClientApp app) {
    return ClientAppData.builder()
        .id(app.getId().value().toString())
        .clientId(app.getClientId().value())
        .name(app.getName())
        .description(app.getDescription())
        .type(app.getType().name())
        .redirectUris(app.getRedirectUris().stream()
            .map(RedirectUri::value)
            .toList())
        .grants(app.getAccessPolicy().grants().stream()
            .map(Enum::name)
            .toList())
        .scopes(app.getAccessPolicy().scopes().stream()
            .map(AllowedScope::value)
            .toList())
        .status(app.getStatus().name())
        .build();
  }
}
