package io.cmartinezs.keygo.supabase.auth.mapper;

import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyAlgorithm;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyId;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyStatus;
import io.cmartinezs.keygo.supabase.auth.entity.SigningKeyEntity;

/**
 * Mapper entre {@link SigningKeyEntity} (JPA) y {@link SigningKey} (dominio).
 */
public final class SigningKeyPersistenceMapper {

  private SigningKeyPersistenceMapper() {}

  /** Convierte una entidad JPA a su representación de dominio. */
  public static SigningKey toDomain(SigningKeyEntity entity) {
    return SigningKey.builder()
        .id(new SigningKeyId(entity.getId().toString()))
        .kid(entity.getKid())
        .algorithm(SigningKeyAlgorithm.valueOf(entity.getAlgorithm()))
        .status(SigningKeyStatus.valueOf(entity.getStatus()))
        .publicMaterial(entity.getPublicMaterial())
        .privateMaterial(entity.getPrivateMaterial())
        .activatedAt(entity.getActivatedAt())
        .retiredAt(entity.getRetiredAt())
        .build();
  }

  /** Convierte un objeto de dominio a entidad JPA (para nuevo registro). */
  public static SigningKeyEntity toEntity(SigningKey domain) {
    return SigningKeyEntity.builder()
        .kid(domain.getKid())
        .algorithm(domain.getAlgorithm().name())
        .status(domain.getStatus().name())
        .publicMaterial(domain.getPublicMaterial())
        .privateMaterial(domain.getPrivateMaterial())
        .activatedAt(domain.getActivatedAt())
        .retiredAt(domain.getRetiredAt())
        .build();
  }
}

