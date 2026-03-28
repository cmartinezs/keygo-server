package io.cmartinezs.keygo.supabase.user.repository;

import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for tenant-scoped user persistence.
 * <p>Repositorio JPA para persistencia de usuarios con alcance de tenant.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public interface TenantUserJpaRepository extends JpaRepository<TenantUserEntity, UUID> {

  /**
   * Find a user by its UUID and tenant ID.
   * <p>Busca un usuario por su UUID y tenant ID.
   */
  Optional<TenantUserEntity> findByIdAndTenantId(UUID id, UUID tenantId);

  /**
   * Find a user by email within a tenant.
   * <p>Busca un usuario por email dentro de un tenant.
   */
  Optional<TenantUserEntity> findByTenantIdAndEmail(UUID tenantId, String email);

  /**
   * Find a user by username within a tenant.
   * <p>Busca un usuario por username dentro de un tenant.
   */
  Optional<TenantUserEntity> findByTenantIdAndUsername(UUID tenantId, String username);

  /**
   * Check whether a user with the given email exists within a tenant.
   * <p>Verifica si existe un usuario con el email dado dentro de un tenant.
   */
  boolean existsByTenantIdAndEmail(UUID tenantId, String email);

  /**
   * Check whether a user with the given username exists within a tenant.
   * <p>Verifica si existe un usuario con el username dado dentro de un tenant.
   */
  boolean existsByTenantIdAndUsername(UUID tenantId, String username);

  /**
   * Find all users belonging to a tenant.
   * <p>Busca todos los usuarios de un tenant.
   */
  List<TenantUserEntity> findAllByTenantId(UUID tenantId);

  /**
   * Count users with the given status across all tenants.
   * <p>Cuenta usuarios con el estado dado en todos los tenants.
   */
  long countByStatus(UserStatus status);
}

