package io.cmartinezs.keygo.supabase.tenant.mapper;

import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;

/**
 * Mapper between Tenant domain model and TenantEntity JPA model.
 * Mapper entre el modelo de dominio Tenant y el modelo JPA TenantEntity.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class TenantPersistenceMapper {

  /**
   * Convert a domain Tenant to a JPA TenantEntity.
   * Convierte un Tenant de dominio a un TenantEntity JPA.
   *
   * @param tenant the domain tenant
   * @return the corresponding JPA entity
   */
  public TenantEntity toEntity(Tenant tenant) {
    var builder = TenantEntity.builder()
        .slug(tenant.getSlug().value())
        .name(tenant.getName())
        .ownerEmail(tenant.getOwnerEmail())
        .status(tenant.getStatus());
    if (tenant.getId() != null) {
      builder.id(tenant.getId().value());
    }
    return builder.build();
  }

  /**
   * Convert a JPA TenantEntity to a domain Tenant.
   * Convierte un TenantEntity JPA a un Tenant de dominio.
   *
   * @param entity the JPA entity
   * @return the corresponding domain tenant
   */
  public Tenant toDomain(TenantEntity entity) {
    return Tenant.builder()
        .id(TenantId.of(entity.getId()))
        .slug(TenantSlug.of(entity.getSlug()))
        .name(entity.getName())
        .ownerEmail(entity.getOwnerEmail())
        .status(entity.getStatus())
        .contractorId(entity.getContractor() != null ? entity.getContractor().getId() : null)
        .build();
  }
}

