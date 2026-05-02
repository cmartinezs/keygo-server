package io.cmartinezs.keygo.domain.tenant.model;

import java.util.UUID;

/**
 * Value object representing a unique tenant identifier.
 * <p>Objeto de valor que representa un identificador único de tenant.
 * @author cmartinezs
 * @version 1.0
 */
public record TenantId(UUID value) {

  public TenantId {
    if (value == null) {
      throw new IllegalArgumentException("TenantId value cannot be null");
    }
  }

  /**
   * Generate a new random TenantId.
   * <p>Genera un nuevo TenantId aleatorio.
   * @return a new TenantId
   */
  public static TenantId generate() {
    return new TenantId(UUID.randomUUID());
  }

  /**
   * Create a TenantId from an existing UUID.
   * <p>Crea un TenantId a partir de un UUID existente.
   * @param value the UUID value
   * @return a TenantId wrapping the given UUID
   */
  public static TenantId of(UUID value) {
    return new TenantId(value);
  }

  /**
   * Create a TenantId from a string UUID.
   * <p>Crea un TenantId a partir de un UUID en formato string.
   * @param value the UUID string
   * @return a TenantId wrapping the parsed UUID
   * @throws IllegalArgumentException if the string is not a valid UUID
   */
  public static TenantId of(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("TenantId string value cannot be null or blank");
    }
    return new TenantId(UUID.fromString(value));
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public String toString() {
    return value.toString();
  }
}

