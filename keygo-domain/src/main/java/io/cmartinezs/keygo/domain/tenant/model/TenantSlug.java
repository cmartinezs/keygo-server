package io.cmartinezs.keygo.domain.tenant.model;

import io.cmartinezs.keygo.domain.shared.util.SlugUtils;
import java.util.regex.Pattern;

/**
 * Value object representing a URL-friendly tenant slug.
 * Rules: lowercase alphanumeric with hyphens, minimum 3 characters,
 * cannot start or end with a hyphen.
 * <p>Objeto de valor que representa el slug amigable de un tenant.
 * Reglas: alfanumérico en minúsculas con guiones, mínimo 3 caracteres,
 * no puede comenzar ni terminar con guion.
 * @author cmartinezs
 * @version 1.0
 */
public record TenantSlug(String value) {

  private static final int MIN_LENGTH = 3;
  private static final int MAX_LENGTH = 100;
  private static final Pattern VALID_SLUG =
      Pattern.compile("^[a-z0-9]([a-z0-9\\-]*[a-z0-9])?$");

  public TenantSlug {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("TenantSlug cannot be null or blank");
    }
    if (value.length() < MIN_LENGTH) {
      throw new IllegalArgumentException(
          "TenantSlug must be at least " + MIN_LENGTH + " characters long: " + value);
    }
    if (value.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "TenantSlug must be at most " + MAX_LENGTH + " characters long: " + value);
    }
    if (!VALID_SLUG.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "TenantSlug must be lowercase alphanumeric with hyphens and cannot start/end with hyphen: "
              + value);
    }
  }

  /**
   * Create a TenantSlug from a string value.
   * <p>Crea un TenantSlug a partir de un valor string.
   * @param value the slug string
   * @return a validated TenantSlug
   */
  public static TenantSlug of(String value) {
    return new TenantSlug(value);
  }

  /**
   * Generate a TenantSlug from a human-readable name.
   * Uses {@link SlugUtils#toSlug(String)} to produce a URL-friendly value, then validates it.
   * <p>Genera un TenantSlug a partir de un nombre legible por humanos.
   * Usa {@link SlugUtils#toSlug(String)} para producir un valor amigable para URLs y lo valida.
   * @param name the tenant name (e.g. "My Organization")
   * @return a validated TenantSlug derived from the name
   * @throws IllegalArgumentException if the name is null, blank, or produces an invalid slug
   */
  public static TenantSlug fromName(String name) {
    return new TenantSlug(SlugUtils.toSlug(name));
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public String toString() {
    return value;
  }
}

