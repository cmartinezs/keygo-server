package io.cmartinezs.keygo.supabase.auth.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private SigningKeyPersistenceMapper() {}

  public static SigningKey toDomain(SigningKeyEntity entity) {
    TenantId tenantId = entity.getTenant() != null ? new TenantId(entity.getTenant().getId()) : null;
    return SigningKey.builder()
        .id(new SigningKeyId(entity.getId().toString()))
        .kid(entity.getKid())
        .algorithm(SigningKeyAlgorithm.valueOf(entity.getAlgorithm()))
        .status(SigningKeyStatus.valueOf(entity.getStatus()))
        .tenantId(tenantId)
        .publicMaterial(readJsonString(entity.getPublicMaterial()))
        .privateMaterial(entity.getPrivateMaterial())
        .activatedAt(entity.getActivatedAt())
        .retiredAt(entity.getRetiredAt())
        .build();
  }

  public static SigningKeyEntity toEntity(SigningKey domain, TenantEntity tenantEntity) {
    return SigningKeyEntity.builder()
        .kid(domain.getKid())
        .algorithm(domain.getAlgorithm().name())
        .status(domain.getStatus().name())
        .tenant(tenantEntity)
        .publicMaterial(writeJsonString(domain.getPublicMaterial()))
        .privateMaterial(domain.getPrivateMaterial())
        .activatedAt(domain.getActivatedAt())
        .retiredAt(domain.getRetiredAt())
        .build();
  }

  private static String writeJsonString(String value) {
    if (value == null) {
      return null;
    }
    try {
      return OBJECT_MAPPER.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize signing key public material", e);
    }
  }

  private static String readJsonString(String value) {
    if (value == null || value.isBlank()) {
      return value;
    }
    try {
      JsonNode node = OBJECT_MAPPER.readTree(value);
      return node.isTextual() ? node.asText() : value;
    } catch (JsonProcessingException e) {
      return value;
    }
  }
}
