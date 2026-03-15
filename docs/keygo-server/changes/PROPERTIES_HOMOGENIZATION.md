# Configuration Properties Homogenization / Homologación de Propiedades de Configuración

**Date / Fecha**: 2026-02-11  
**Version**: 1.0  
**Author / Autor**: cmartinezs

## Summary / Resumen

Homogenized all configuration property prefixes in KeyGo Server to use a consistent `keygo.*` prefix instead of mixed formats.

Se homologaron todos los prefijos de propiedades de configuración en KeyGo Server para usar un prefijo consistente `keygo.*` en lugar de formatos mixtos.

## Changes / Cambios

### Before / Antes

The project had inconsistent property prefixes:
El proyecto tenía prefijos inconsistentes:

```yaml
# Mixed format - Formato mixto
key-go-server:
  info:
    title: "..."
    name: "..."
    version: "..."

keygo:
  bootstrap:
    enabled: true
    admin-key: "..."
```

### After / Después

All properties now use the `keygo.*` prefix:
Todas las propiedades ahora usan el prefijo `keygo.*`:

```yaml
# Consistent format - Formato consistente
keygo:
  info:
    title: "..."
    name: "..."
    version: "..."
  bootstrap:
    enabled: true
    admin-key: "..."
```

## Modified Files / Archivos Modificados

### 1. Source Code / Código Fuente

#### `ServiceInfoProperties.java`
**Path / Ruta**: `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/properties/ServiceInfoProperties.java`

```java
// Before / Antes
@ConfigurationProperties(prefix = "key-go-server.info")

// After / Después
@ConfigurationProperties(prefix = "keygo.info")
```

### 2. Configuration / Configuración

#### `application.yml`
**Path / Ruta**: `keygo-run/src/main/resources/application.yml`

```yaml
# Before / Antes
key-go-server:
  info:
    title: "@project.parent.name@"
    name: "@project.parent.artifactId@"
    version: "@project.parent.version@"

spring:
  application:
    name: "${key-go-server.info.name}-${key-go-server.info.version}"

server:
  servlet:
    context-path: "/${key-go-server.info.name}"

# After / Después
keygo:
  info:
    title: "@project.parent.name@"
    name: "@project.parent.artifactId@"
    version: "@project.parent.version@"
  bootstrap:
    enabled: true
    admin-key: "${KEYGO_ADMIN_KEY:changeMe}"

spring:
  application:
    name: "${keygo.info.name}-${keygo.info.version}"

server:
  servlet:
    context-path: "/${keygo.info.name}"
```

### 3. Documentation / Documentación

Updated the following documentation files:
Se actualizaron los siguientes archivos de documentación:

- `docs/SERVICE_INFO_ENDPOINT.md`
- `docs/changes/MAVEN_RESOURCE_FILTERING.md`
- `docs/BOOTSTRAP_PROPERTIES.md`

## Property Naming Convention / Convención de Nombres de Propiedades

### Standard / Estándar

All KeyGo configuration properties follow this pattern:
Todas las propiedades de configuración de KeyGo siguen este patrón:

```
keygo.<module>.<property>
```

### Examples / Ejemplos

| Category / Categoría | Prefix / Prefijo | Properties / Propiedades |
|---------------------|------------------|--------------------------|
| Service Information | `keygo.info` | `title`, `name`, `version` |
| Bootstrap Configuration | `keygo.bootstrap` | `enabled`, `admin-key` |

### Benefits / Beneficios

1. **Consistency / Consistencia**: All properties use the same naming pattern
   Todas las propiedades usan el mismo patrón de nombres

2. **Clarity / Claridad**: Easy to identify KeyGo-specific properties
   Fácil identificar propiedades específicas de KeyGo

3. **Organization / Organización**: Properties are grouped logically under `keygo.*`
   Las propiedades están agrupadas lógicamente bajo `keygo.*`

4. **Maintainability / Mantenibilidad**: Easier to manage and extend
   Más fácil de mantener y extender

## Testing / Pruebas

All tests pass successfully after the changes:
Todas las pruebas pasan exitosamente después de los cambios:

```
Tests run: 23, Failures: 0, Errors: 0, Skipped: 0
```

### Test Coverage / Cobertura de Pruebas

- `ServiceInfoPropertiesTest`: 8 tests ✓
- `KeyGoBootstrapPropertiesTest`: 11 tests ✓
- `ApplicationConfigTest`: 4 tests ✓

## Migration Guide / Guía de Migración

If you have existing configurations using the old prefix, update them as follows:
Si tienes configuraciones existentes usando el prefijo antiguo, actualízalas así:

### In application.yml / En application.yml

```yaml
# Replace / Reemplazar
key-go-server.info.*

# With / Con
keygo.info.*
```

### In Property References / En Referencias de Propiedades

```yaml
# Replace / Reemplazar
${key-go-server.info.name}

# With / Con
${keygo.info.name}
```

## Backwards Compatibility / Compatibilidad Hacia Atrás

⚠️ **Breaking Change / Cambio Incompatible**

This is a breaking change. Any external configurations or scripts using the old `key-go-server.info.*` prefix must be updated to use `keygo.info.*`.

Este es un cambio incompatible. Cualquier configuración externa o script que use el antiguo prefijo `key-go-server.info.*` debe actualizarse para usar `keygo.info.*`.

## Verification / Verificación

To verify the changes work correctly:
Para verificar que los cambios funcionan correctamente:

```bash
# Build and test
./mvnw clean test -pl keygo-run

# Check processed configuration
cat keygo-run/target/classes/application.yml
```

## Related Documentation / Documentación Relacionada

- [Bootstrap Properties Documentation](BOOTSTRAP_PROPERTIES.md)
- [Service Info Endpoint](SERVICE_INFO_ENDPOINT.md)
- [Maven Resource Filtering](changes/MAVEN_RESOURCE_FILTERING.md)

