package io.cmartinezs.keygo.app.membership.port;

import io.cmartinezs.keygo.domain.membership.model.PlatformUserRole;
import java.util.List;
import java.util.UUID;

/**
 * Port OUT: repository operations for platform-level user role assignments.
 * <p>Puerto de salida: operaciones de repositorio para asignaciones de roles de plataforma a usuarios.
 * NOTE: In the current model, "users" at the platform level are TenantUsers in the keygo tenant.
 * The userId parameter refers to a tenant_users.id value.
 * @author cmartinezs
 * @version 1.0
 */
public interface PlatformUserRoleRepositoryPort {

  /** Assign a platform role to a user (by roleCode). Returns the created assignment. */
  PlatformUserRole assign(UUID platformUserId, String roleCode);

  /** Revoke a platform role from a user (by roleCode). Deletes the assignment. */
  void revoke(UUID platformUserId, String roleCode);

  /** Retrieve all platform role assignments for a platform user. */
  List<PlatformUserRole> findByPlatformUserId(UUID platformUserId);

  /** Check if a user has a specific platform role. */
  boolean hasRole(UUID platformUserId, String roleCode);

  /** Retrieve the role codes (e.g. "keygo_admin") for all platform roles assigned to a user. */
  List<String> findRoleCodesByPlatformUserId(UUID platformUserId);
}
