package io.cmartinezs.keygo.app.membership.port;

import io.cmartinezs.keygo.domain.membership.model.TenantUserRole;
import java.util.List;
import java.util.UUID;

/**
 * Port OUT: repository operations for tenant-level user role assignments.
 * <p>Puerto de salida: operaciones de repositorio para asignaciones de roles de tenant a usuarios.
 * @author cmartinezs
 * @version 1.0
 */
public interface TenantUserRoleRepositoryPort {

  /** Assign a tenant role to a tenant user. Returns the created assignment. */
  TenantUserRole assign(UUID tenantUserId, UUID tenantRoleId);

  /**
   * Revoke (soft-delete) a tenant role from a tenant user.
   * Sets removed_at timestamp on the assignment.
   */
  void revoke(UUID tenantUserId, UUID tenantRoleId);

  /** Retrieve all ACTIVE role assignments for a tenant user. */
  List<TenantUserRole> findActiveByTenantUserId(UUID tenantUserId);

  /** Retrieve ALL role assignments (active and revoked) for a tenant user. */
  List<TenantUserRole> findAllByTenantUserId(UUID tenantUserId);

  /** Check if a tenant user has an active assignment for a specific role. */
  boolean hasActiveRole(UUID tenantUserId, UUID tenantRoleId);
}
