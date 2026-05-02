package io.cmartinezs.keygo.app.user.port;

import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.user.filter.UserFilter;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.Username;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * Port OUT — persistence contract for the User aggregate.
 * <p>Puerto de salida — contrato de persistencia para el agregado User.
 * @author cmartinezs
 * @version 1.0
 */
public interface UserRepositoryPort {

  /**
   * Persist or update a user.
   * <p>Persiste o actualiza un usuario.
   * @param user the user to save
   * @return the saved user (may include generated fields such as timestamps)
   */
  User save(User user);

  /**
   * Find a user by its UUID and tenant.
   * <p>Busca un usuario por su UUID y tenant.
   * @param userId   the user identifier
   * @param tenantId the tenant scope
   * @return an Optional containing the user if found
   */
  Optional<User> findByIdAndTenantId(UserId userId, TenantId tenantId);

  /**
   * Find a user by its UUID (without tenant scope, for platform-level lookups).
   * <p>Busca un usuario por su UUID (sin scope de tenant, para búsquedas a nivel plataforma).
   * @param userId the user identifier
   * @return an Optional containing the user if found
   */
  Optional<User> findById(UserId userId);

  /**
   * Find a user by email within a tenant.
   * <p>Busca un usuario por email dentro de un tenant.
   * @param tenantId the tenant scope
   * @param email    the email address to search by
   * @return an Optional containing the user if found
   */
  Optional<User> findByTenantIdAndEmail(TenantId tenantId, EmailAddress email);

  /**
   * Find a user by username within a tenant.
   * <p>Busca un usuario por username dentro de un tenant.
   * @param tenantId the tenant scope
   * @param username the username to search by
   * @return an Optional containing the user if found
   */
  Optional<User> findByTenantIdAndUsername(TenantId tenantId, Username username);

  /**
   * Find a tenant-scoped user by the linked global platform identity.
   *
   * @param tenantId the tenant scope
   * @param platformUserId the global platform user identifier
   * @return an Optional containing the tenant-scoped user if found
   */
  Optional<User> findByTenantIdAndPlatformUserId(TenantId tenantId, UserId platformUserId);

  /**
   * Check whether a user with the given email already exists within a tenant.
   * <p>Verifica si ya existe un usuario con el email dado dentro de un tenant.
   * @param tenantId the tenant scope
   * @param email    the email to check
   * @return true if a user with that email exists in the tenant
   */
  boolean existsByTenantIdAndEmail(TenantId tenantId, EmailAddress email);

  /**
   * Check whether a user with the given username already exists within a tenant.
   * <p>Verifica si ya existe un usuario con el username dado dentro de un tenant.
   * @param tenantId the tenant scope
   * @param username the username to check
   * @return true if a user with that username exists in the tenant
   */
  boolean existsByTenantIdAndUsername(TenantId tenantId, Username username);

  /**
   * Find all usernames that start with the given prefix within a tenant.
   * <p>Busca todos los usernames que empiezan con el prefijo dado dentro de un tenant.
   * @param tenantId the tenant scope
   * @param prefix  the username prefix to search for
   * @return list of usernames matching the prefix (may be empty)
   */
  List<Username> findUsernamesByPrefix(TenantId tenantId, String prefix);

  /**
   * Find all users belonging to a tenant.
   * <p>Busca todos los usuarios de un tenant.
   * @param tenantId the tenant scope
   * @return list of users (may be empty)
   */
  List<User> findAllByTenantId(TenantId tenantId);

  /**
   * Find all users with pagination, filtering, and sorting.
   * <p>Busca usuarios con paginación, filtrado y ordenamiento.
   * @param tenantId the tenant scope
   * @param filter pagination, filtering, and sorting criteria
   * @return paginated result of users (may be empty)
   */
  PagedResult<User> findAllPaged(TenantId tenantId, UserFilter filter);
}
