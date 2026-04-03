package io.cmartinezs.keygo.app.membership.port;

import io.cmartinezs.keygo.app.role.filter.AppRoleFilter;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.membership.model.AppRoleId;
import io.cmartinezs.keygo.domain.membership.model.RoleCode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port OUT for app role persistence operations.
 * <p>Puerto OUT para operaciones de persistencia de roles de app.
 * Implementations are responsible for retrieving and storing AppRole aggregates.
 * <p>Las implementaciones son responsables de recuperar y guardar agregados de AppRole.
 * @author cmartinezs
 * @version 1.0
 */
public interface AppRoleRepositoryPort {

  /**
   * Find an app role by ID.
   * <p>Encuentra un rol de app por ID.
   * @param roleId the role ID
   * @return the role, or empty if not found
   */
  Optional<AppRole> findById(AppRoleId roleId);

  /**
   * Find an app role by client app ID and role code.
   * <p>Encuentra un rol de app por ID de app de cliente y código de rol.
   * @param clientAppId the client app ID
   * @param roleCode the role code
   * @return the role, or empty if not found
   */
  Optional<AppRole> findByClientAppAndCode(UUID clientAppId, RoleCode roleCode);

  /**
   * List all roles for a given client app.
   * <p>Lista todos los roles de una app de cliente.
   * @param clientAppId the client app ID
   * @return list of roles
   */
  List<AppRole> findByClientAppId(UUID clientAppId);

  /**
   * Check if a role code already exists for the client app.
   * <p>Verifica si un código de rol ya existe para la app de cliente.
   * @param clientAppId the client app ID
   * @param roleCode the role code
   * @return true if the role exists
   */
  boolean existsByClientAppAndCode(UUID clientAppId, RoleCode roleCode);

  /**
   * Persist a new app role.
   * <p>Persiste un nuevo rol de app.
   * @param role the role to save
   * @return the persisted role
   */
  AppRole save(AppRole role);

  /**
   * Update an existing app role.
   * <p>Actualiza un rol de app existente.
   * @param role the role to update
   * @return the updated role
   */
  AppRole update(AppRole role);

  /**
   * Delete an app role by ID.
   * <p>Elimina un rol de app por ID.
   * @param roleId the ID of the role to delete
   */
  void deleteById(AppRoleId roleId);

  /**
   * Find all app roles with pagination, filtering, and sorting.
   * <p>Busca roles de app con paginación, filtrado y ordenamiento.
   * @param clientAppId the client app scope
   * @param filter pagination, filtering, and sorting criteria
   * @return paginated result of roles
   */
  PagedResult<AppRole> findAllPaged(UUID clientAppId, AppRoleFilter filter);
}

