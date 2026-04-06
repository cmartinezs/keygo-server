package io.cmartinezs.keygo.supabase.auth.mapper;

import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyAlgorithm;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyId;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyStatus;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.supabase.auth.entity.SigningKeyEntity;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;

/**
 * Mapper entre {@link SigningKeyEntity} (JPA) y {@link SigningKey} (dominio).
 */
public final class SigningKeyPersistenceMapper {

  private SigningKeyPersistenceMapper() {}

  /** Convierte una entidad JPA a su representación de dominio. */
  public static SigningKey toDomain(SigningKeyEntity entity) {
    TenantId tenantId = entity.getTenant() != null
        ? new TenantId(entity.getTenant().getId())
        : null;
    return SigningKey.builder()
        .id(new SigningKeyId(entity.getId().toString()))
        .kid(entity.getKid())
        .algorithm(SigningKeyAlgorithm.valueOf(entity.getAlgorithm()))
        .status(SigningKeyStatus.valueOf(entity.getStatus()))
        .tenantId(tenantId)
        .publicMaterial(entity.getPublicMaterial())
        .privateMaterial(entity.getPrivateMaterial())
        .activatedAt(entity.getActivatedAt())
        .retiredAt(entity.getRetiredAt())
        .build();
  }

  /**
   * Convierte un objeto de dominio a entidad JPA.
   *
   * @param domain      modelo de dominio
   * @param tenantEntity entidad JPA del tenant (puede ser null para claves globales)
   */
  public static SigningKeyEntity toEntity(SigningKey domain, TenantEntity tenantEntity) {
    return SigningKeyEntity.builder()
        .kid(domain.getKid())
        .algorithm(domain.getAlgorithm().name())
        .status(domain.getStatus().name())
        .tenant(tenantEntity)
        .publicMaterial(domain.getPublicMaterial())
        .privateMaterial(domain.getPrivateMaterial())
        .activatedAt(domain.getActivatedAt())
        .retiredAt(domain.getRetiredAt())
        .build();
  }
}
