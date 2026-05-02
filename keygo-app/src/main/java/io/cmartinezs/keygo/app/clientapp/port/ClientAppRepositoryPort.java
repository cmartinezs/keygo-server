package io.cmartinezs.keygo.app.clientapp.port;

import io.cmartinezs.keygo.app.clientapp.filter.ClientAppFilter;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * Port OUT — persistence contract for the ClientApp aggregate.
 * <p>Puerto de salida — contrato de persistencia para el agregado ClientApp.
 * @author cmartinezs
 * @version 1.0
 */
public interface ClientAppRepositoryPort {

  /**
   * Persist or update a client application.
   * <p>Persiste o actualiza una aplicación cliente.
   * @param clientApp the client app to save
   * @return the saved client app (may include generated fields)
   */
  ClientApp save(ClientApp clientApp);

  /**
   * Find a client application by its internal UUID.
   */
  Optional<ClientApp> findById(io.cmartinezs.keygo.domain.clientapp.model.ClientAppId id);

  /**
   * Find a client application by its globally unique OAuth2 client_id (string), across all tenants.
   * Useful when the caller is a subscriber (not the owner) of the app.
   */
  Optional<ClientApp> findByClientId(ClientId clientId);

  /**
   * Find a client application by its clientId and tenant.
   * <p>Busca una aplicacin cliente por su clientId y tenant.
   * @param clientId the OAuth2 client_id
   * @param tenantId the tenant identifier
   * @return an Optional containing the client app if found
   */
  Optional<ClientApp> findByClientIdAndTenantId(ClientId clientId, TenantId tenantId);

  /**
   * Find all client applications belonging to a tenant.
   * <p>Busca todas las aplicaciones cliente de un tenant.
   * @param tenantId the tenant identifier
   * @return list of client apps (may be empty)
   */
  List<ClientApp> findAllByTenantId(TenantId tenantId);

  /**
   * Check whether a client application with the given clientId already exists.
   * <p>Verifica si ya existe una aplicación cliente con el clientId dado.
   * @param clientId the clientId to check
   * @return true if the clientId is already in use
   */
  boolean existsByClientId(ClientId clientId);

  /**
   * Find all client applications with pagination, filtering, and sorting.
   * <p>Busca aplicaciones cliente con paginación, filtrado y ordenamiento.
   * @param tenantId the tenant scope
   * @param filter pagination, filtering, and sorting criteria
   * @return paginated result of client apps (may be empty)
   */
  PagedResult<ClientApp> findAllPaged(TenantId tenantId, ClientAppFilter filter);
}

