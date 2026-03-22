package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.command.InitiateAuthorizationCommand;
import io.cmartinezs.keygo.app.auth.result.AuthorizationInitiatedResult;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.exception.InvalidRedirectUriException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;

/**
 * Caso de uso: Iniciar el flujo OAuth 2.0 Authorization Code.
 *
 * <p>Valida que:
 * <ul>
 *   <li>El tenant existe y está activo
 *   <li>La app cliente existe y pertenece al tenant
 *   <li>La URI de redirección está registrada en la app
 *   <li>Los scopes son válidos (opcional en esta fase)
 * </ul>
 *
 * <p>Resultado: confirmación de que el flujo puede proceder al login.
 */
public class InitiateAuthorizationUseCase {
  private final TenantRepositoryPort tenantRepository;
  private final ClientAppRepositoryPort clientAppRepository;

  public InitiateAuthorizationUseCase(
      TenantRepositoryPort tenantRepository, ClientAppRepositoryPort clientAppRepository) {
    this.tenantRepository = tenantRepository;
    this.clientAppRepository = clientAppRepository;
  }

  /**
   * Ejecuta la iniciación de autorización.
   *
   * @param command parámetros del comando
   * @return resultado con datos de la app cliente
   * @throws TenantNotFoundException si el tenant no existe
   * @throws TenantSuspendedException si el tenant está suspendido
   * @throws ClientAppNotFoundException si la app cliente no existe
   * @throws InvalidRedirectUriException si la URI de redirección no está registrada
   */
  public AuthorizationInitiatedResult execute(InitiateAuthorizationCommand command) {
    // Validar tenant
    Tenant tenant =
        tenantRepository
            .findBySlug(new TenantSlug(command.tenantSlug()))
            .orElseThrow(
                () -> new TenantNotFoundException("Tenant not found: " + command.tenantSlug()));

    if (tenant.isSuspended()) {
      throw new TenantSuspendedException("Tenant is suspended: " + command.tenantSlug());
    }

    // Validar app cliente
    ClientApp clientApp =
        clientAppRepository
            .findByClientIdAndTenantId(
                new ClientId(command.clientId()), tenant.getId())
            .orElseThrow(
                () ->
                    new ClientAppNotFoundException(
                        "Client app not found: " + command.clientId()));

    // Validar que la app está activa
    if (!clientApp.isActive()) {
      throw new InvalidRedirectUriException(
          "Client app is suspended: " + command.clientId());
    }

    // Validar redirect URI (utiliza el método validateRedirectUri que lanza excepción si no existe)
    clientApp.validateRedirectUri(command.redirectUri());

    return new AuthorizationInitiatedResult(
        clientApp.getClientId().value(),
        clientApp.getName(),
        command.redirectUri());
  }
}




