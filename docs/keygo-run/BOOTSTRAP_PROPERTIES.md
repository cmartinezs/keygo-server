# KeyGo Bootstrap Properties

## Overview / Descripción General

The `KeyGoBootstrapProperties` class provides configuration properties for KeyGo bootstrap settings. It uses Spring Boot's `@ConfigurationProperties` with validation to ensure secure configuration.

La clase `KeyGoBootstrapProperties` proporciona propiedades de configuración para los ajustes de arranque de KeyGo. Utiliza `@ConfigurationProperties` de Spring Boot con validación para asegurar una configuración segura.

## Location / Ubicación

- **Class / Clase**: `io.cmartinezs.keygo.run.config.properties.KeyGoBootstrapProperties`
- **Module / Módulo**: `keygo-run`
- **Package**: `io.cmartinezs.keygo.run.config.properties`

## Configuration / Configuración

### Properties / Propiedades

| Property | Type | Default | Description (EN) | Descripción (ES) |
|----------|------|---------|------------------|------------------|
| `keygo.bootstrap.enabled` | `boolean` | `true` | Enable/disable bootstrap functionality | Habilitar/deshabilitar funcionalidad de arranque |
| `keygo.bootstrap.admin-key` | `String` | - | Admin key for bootstrap operations | Clave de administrador para operaciones de arranque |

**Note / Nota**: All KeyGo configuration properties use the `keygo.*` prefix for consistency. / Todas las propiedades de configuración de KeyGo usan el prefijo `keygo.*` para consistencia.

- `keygo.info.*` - Service information properties / Propiedades de información del servicio
- `keygo.bootstrap.*` - Bootstrap configuration properties / Propiedades de configuración de arranque

### Validation / Validación

The class includes custom validation logic:
La clase incluye lógica de validación personalizada:

- **When `enabled=true`**: `adminKey` must not be null, empty, or blank
- **Cuando `enabled=true`**: `adminKey` no debe ser null, vacía o en blanco

- **When `enabled=false`**: No validation on `adminKey`
- **Cuando `enabled=false`**: Sin validación en `adminKey`

## Usage Example / Ejemplo de Uso

### application.yml

```yaml
keygo:
  bootstrap:
    enabled: true
    admin-key: "${KEYGO_ADMIN_KEY:changeMe}"
```

### Environment Variable / Variable de Entorno

```bash
export KEYGO_ADMIN_KEY="mySecureAdminKey123"
```

### Injection in Spring Component / Inyección en Componente Spring

```java
@Service
public class BootstrapService {
    
    private final KeyGoBootstrapProperties bootstrapProperties;
    
    @Autowired
    public BootstrapService(KeyGoBootstrapProperties bootstrapProperties) {
        this.bootstrapProperties = bootstrapProperties;
    }
    
    public void performBootstrap() {
        if (bootstrapProperties.isEnabled()) {
            String adminKey = bootstrapProperties.getAdminKey();
            // Use adminKey for bootstrap operations
            // Usar adminKey para operaciones de arranque
        }
    }
}
```

## Annotations / Anotaciones

- `@Component`: Registers the class as a Spring bean / Registra la clase como un bean de Spring
- `@ConfigurationProperties(prefix = "keygo.bootstrap")`: Binds properties with prefix / Vincula propiedades con prefijo
- `@Validated`: Enables validation on the properties / Habilita validación en las propiedades
- `@Getter` / `@Setter`: Lombok annotations for getters/setters / Anotaciones de Lombok para getters/setters
- `@AssertTrue`: Custom validation method / Método de validación personalizado

## Testing / Pruebas

The class includes comprehensive unit tests in `KeyGoBootstrapPropertiesTest`:
La clase incluye pruebas unitarias completas en `KeyGoBootstrapPropertiesTest`:

- Default values testing / Pruebas de valores por defecto
- Getters and setters testing / Pruebas de getters y setters
- Validation scenarios:
  - Valid when enabled=false with null/blank adminKey
  - Valid when enabled=true with valid adminKey
  - Invalid when enabled=true with null/empty/blank adminKey

## Dependencies / Dependencias

### Maven Dependency / Dependencia Maven

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

This dependency provides:
Esta dependencia proporciona:

- `jakarta.validation-api`: Jakarta Bean Validation API
- `hibernate-validator`: Reference implementation of Bean Validation

## Best Practices / Mejores Prácticas

1. **Environment Variables**: Use environment variables for sensitive data like `adminKey`
   **Variables de Entorno**: Usar variables de entorno para datos sensibles como `adminKey`

2. **Production Configuration**: Always set a strong `adminKey` in production
   **Configuración de Producción**: Siempre establecer un `adminKey` fuerte en producción

3. **Disable in Production**: Consider setting `enabled=false` after initial bootstrap
   **Deshabilitar en Producción**: Considerar establecer `enabled=false` después del arranque inicial

4. **Validation**: Let Spring Boot validate properties at startup to catch configuration errors early
   **Validación**: Dejar que Spring Boot valide propiedades al inicio para detectar errores de configuración temprano

## Version / Versión

- **Author / Autor**: cmartinezs
- **Version**: 1.0
- **Created / Creado**: 2026-02-11

