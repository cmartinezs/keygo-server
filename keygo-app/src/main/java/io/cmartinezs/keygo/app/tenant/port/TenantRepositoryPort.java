package io.cmartinezs.keygo.app.tenant.port;

import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.tenant.filter.TenantFilter;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;

import java.util.Optional;

/**
 * Port OUT — persistence contract for Tenant aggregate.
 * <p>Puerto de salida — contrato de persistencia para el agregado Tenant.
 * @author cmartinezs
 * @version 1.0
 */
public interface TenantRepositoryPort {

  /**
   * Persist or update a tenant.
   * <p>Persiste o actualiza un tenant.
   * @param tenant the tenant to save
   * @return the saved tenant (may include generated fields such as timestamps)
   */
  Tenant save(Tenant tenant);

  /**
   * Find a tenant by its unique slug.
   * <p>Busca un tenant por su slug único.
   * @param slug the slug to search by
   * @return an Optional containing the tenant if found, empty otherwise
   */
  Optional<Tenant> findBySlug(TenantSlug slug);

  /**
   * Find a tenant by its ID.
   * <p>Busca un tenant por su ID.
   * @param id the tenant ID
   * @return an Optional containing the tenant if found
   */
  Optional<Tenant> findById(TenantId id);

  /**
   * Check whether a tenant with the given slug already exists.
   * <p>Verifica si ya existe un tenant con el slug dado.
   * @param slug the slug to check
   * @return true if the slug is already taken
   */
  boolean existsBySlug(TenantSlug slug);

  /**
   * Find all tenants matching the given filter criteria, paginated.
   * <p>Busca todos los tenants que coincidan con los criterios de filtro, paginado.
   * @param filter the filter and pagination criteria / los criterios de filtro y paginación
   * @return a paginated result with matching tenants / resultado paginado con los tenants que coinciden
   */
  PagedResult<Tenant> findAll(TenantFilter filter);
}

