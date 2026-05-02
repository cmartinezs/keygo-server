package io.cmartinezs.keygo.app.membership.port;

import io.cmartinezs.keygo.domain.membership.model.TenantRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port OUT: repository operations for tenant-level roles.
 * <p>Puerto de salida: operaciones de repositorio para roles de nivel tenant.
 * @author cmartinezs
 * @version 1.0
 */
public interface TenantRoleRepositoryPort {

  TenantRole create(TenantRole tenantRole);

  Optional<TenantRole> findByTenantAndCode(UUID tenantId, String code);

  List<TenantRole> findByTenantId(UUID tenantId);

  List<TenantRole> findActiveByTenantId(UUID tenantId);

  TenantRole update(TenantRole tenantRole);

  void deleteById(UUID id);

  boolean existsByTenantAndCode(UUID tenantId, String code);
}
