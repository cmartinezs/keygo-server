package io.cmartinezs.keygo.domain.auth.model;

/**
 * Value object: identificador único de una clave de firma.
 *
 * @param value UUID en formato String
 */
public record SigningKeyId(String value) {

  public SigningKeyId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("SigningKeyId value must not be null or blank");
    }
  }
}

