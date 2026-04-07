package io.cmartinezs.keygo.run.config;

import io.cmartinezs.keygo.run.aop.NotLog;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SpringDoc {@link ModelConverter} que renombra las propiedades de los schemas OpenAPI
 * de camelCase a snake_case, reflejando el {@code PropertyNamingStrategies.SNAKE_CASE}
 * configurado globalmente en Jackson 3 ({@code JsonMapperBuilderCustomizer}).
 *
 * <p>Aplica la misma regla que Google Guava / Jackson internamente:
 * inserta un guion bajo antes de cada letra mayúscula que no esté al inicio y
 * convierte todo a minúsculas. La conversión es idempotente: un nombre ya en
 * snake_case (p.ej. {@code access_token}) no cambia.
 *
 * <p>Se registra como {@code @Bean} en {@link OpenApiConfig} y SpringDoc lo
 * autodescubre a través de la interfaz {@link ModelConverter}.
 *
 * @author cmartinezs
 * @version 1.0
 * @see OpenApiConfig#snakeCaseModelConverter()
 */
@NotLog
public class SnakeCaseModelConverter implements ModelConverter {

  /**
   * Converts a camelCase or PascalCase string to snake_case.
   *
   * <p>Examples:
   * <ul>
   *   <li>{@code clientId} → {@code client_id}</li>
   *   <li>{@code redirectUris} → {@code redirect_uris}</li>
   *   <li>{@code createdAt} → {@code created_at}</li>
   *   <li>{@code profilePictureUrl} → {@code profile_picture_url}</li>
   *   <li>{@code access_token} → {@code access_token} (idempotent)</li>
   *   <li>{@code id} → {@code id} (single word, unchanged)</li>
   * </ul>
   *
   * @param name field name to convert (may be camelCase or already snake_case)
   * @return snake_case version of the name
   */
  static String toSnakeCase(String name) {
    if (name == null || name.isEmpty()) {
      return name;
    }
    // Insert underscore before any uppercase letter preceded by a lowercase letter or digit
    // Then lowercase the whole string
    return name
        .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
        .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
        .toLowerCase();
  }

  /**
   * Resolves the schema for the given type and renames all property keys from camelCase
   * to snake_case after the delegate chain has built the schema.
   *
   * <p>Fields that already carry a {@code @JsonProperty} with an explicit name (e.g. those
   * in {@code TokenData}) are already serialized with the annotated name by Jackson and
   * SpringDoc respects it; this converter only renames properties whose name is still in
   * camelCase (coming from the Java field name).
   *
   * @param type    annotated type being resolved
   * @param context SpringDoc converter context
   * @param chain   remaining converter chain
   * @return schema with property names in snake_case, or {@code null} if unresolvable
   */
  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
    if (!chain.hasNext()) {
      return null;
    }
    Schema schema = chain.next().resolve(type, context, chain);
    if (schema == null) {
      return null;
    }

    Map<String, Schema> properties = schema.getProperties();
    if (properties == null || properties.isEmpty()) {
      return schema;
    }

    // Rebuild properties map with snake_case keys, preserving insertion order
    Map<String, Schema> renamed = new LinkedHashMap<>();
    properties.forEach((key, value) -> renamed.put(toSnakeCase(key), value));

    // Only replace if at least one key actually changed (avoid unnecessary mutation)
    if (!renamed.equals(properties)) {
      schema.setProperties(renamed);
    }

    return schema;
  }
}

