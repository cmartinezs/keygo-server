# Convenciones de Estilo de Código — KeyGo Server

> Documento establecido en la Fase 0 del plan de implementación.
> Aplica a todos los módulos del monorepo.

## Herramienta de validación

El proyecto usa **Maven Enforcer Plugin** (configurado en el `pom.xml` raíz) para validar:
- Java 21 o superior
- Maven 3.9 o superior
- Encoding `UTF-8`
- Sin dependencias duplicadas en los POMs

Ejecución manual:
```bash
./mvnw validate
```

## Estilo de código Java

### Indentación y formato

| Regla | Valor |
|---|---|
| Indentación | **2 espacios** (no tabs) |
| Longitud máxima de línea | **100 caracteres** |
| Encoding del archivo | **UTF-8** |
| Final de línea | **LF (Unix)** |

> Referencia base: [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)

### Nombre de clases por tipo arquitectónico

| Tipo | Sufijo / Convención | Ejemplo | Módulo |
|---|---|---|---|
| Caso de uso | `<Acción><Entidad>UseCase` | `GetServiceInfoUseCase` | `keygo-app` |
| Puerto OUT (interfaz) | `<Entidad>Provider` o `<Entidad>Port` | `ServiceInfoProvider` | `keygo-app` |
| Controlador REST | `<Entidad>Controller` | `ServiceInfoController` | `keygo-api` |
| DTO de respuesta | `<Entidad>Data` o `<Entidad>Response` | `ServiceInfoData` | `keygo-api` |
| DTO de petición | `<Entidad>Request` | `CreateTenantRequest` | `keygo-api` |
| Entidad JPA | `<Entidad>Entity` | `UserEntity` | `keygo-supabase` |
| Repositorio JPA | `<Entidad>Repository` | `UserRepository` | `keygo-supabase` |
| Adaptador de repositorio | `<Entidad>RepositoryAdapter` | `TenantRepositoryAdapter` | `keygo-supabase` |
| Mapper | `<Entidad>Mapper` | `TenantPersistenceMapper` | `keygo-supabase` |
| Propiedades de config | `<Feature>Properties` | `KeyGoBootstrapProperties` | `keygo-run` |
| Filtro Servlet | `<Feature>Filter` | `BootstrapAdminKeyFilter` | `keygo-run` |
| Excepción de dominio | `<Concepto>Exception` | `TenantNotFoundException` | `keygo-domain` |

### Estructura de paquetes

Todos los módulos se organizan **por feature**, no por capa técnica:

```
io.cmartinezs.keygo.<módulo>/
  <feature>/              # grupo funcional: platform, tenant, user, membership, auth...
    controller/           # (keygo-api) controladores REST
    response/             # (keygo-api) DTOs de salida
    request/              # (keygo-api) DTOs de entrada
    port/                 # (keygo-app) interfaces de puertos OUT
    usecase/              # (keygo-app) casos de uso
    entity/               # (keygo-supabase) entidades JPA
    repository/           # (keygo-supabase) repositorios Spring Data
  shared/                 # (keygo-api) utilidades transversales: ResponseCode, ResponseHelper, BaseResponse
  error/                  # (keygo-api) manejadores y excepciones globales
  config/                 # configuración Spring de cada módulo
```

### Javadoc y comentarios

- Toda clase pública debe tener Javadoc de clase.
- Los métodos públicos más complejos deben tener Javadoc con `@param` y `@return`.
- Los comentarios de implementación pueden estar en **español o inglés**; preferible inglés para compatibilidad con herramientas.
- No comentar código obvio; comentar el **por qué**, no el **qué**.

### Anotaciones Lombok

Usar Lombok para reducir boilerplate:
- `@Getter` / `@Setter` solo donde necesario (evitar `@Data` en entidades JPA)
- `@Builder` / `@SuperBuilder` para builders
- `@RequiredArgsConstructor` para inyección de dependencias (mejor que `@Autowired`)
- `@Slf4j` para logging

### Importaciones

- Sin wildcard imports (`import java.util.*` — ❌)
- Orden: Java standard → Jakarta → Spring → Third-party → Proyecto interno
- Separar bloques con una línea en blanco

### Ejemplo de clase bien formada

```java
package io.cmartinezs.keygo.app.platform.usecase;

import io.cmartinezs.keygo.app.platform.port.ServiceInfoProvider;

/**
 * Use case for retrieving public service information.
 *
 * @author cmartinezs
 */
public class GetServiceInfoUseCase {

  private final ServiceInfoProvider serviceInfoProvider;

  public GetServiceInfoUseCase(ServiceInfoProvider serviceInfoProvider) {
    this.serviceInfoProvider = serviceInfoProvider;
  }

  /**
   * Executes the use case.
   *
   * @return the service info provider with current metadata
   */
  public ServiceInfoProvider execute() {
    return serviceInfoProvider;
  }
}
```

## Reglas de calidad de tests

- Usar `@ExtendWith(MockitoExtension.class)` para tests unitarios (sin Spring context).
- Estructura: comentarios `// Given / When / Then` en cada método de test.
- Nombres de test: `<método>_<escenario>_<resultado>` (p. ej. `execute_whenProviderAvailable_returnsInfo`).
- Framework: JUnit 5 + Mockito + AssertJ.
- **Sin** Spring context en tests unitarios; solo para integration tests.

## Roadmap de lint/format

La configuración de un plugin de lint/formato automático (Checkstyle o Spotless) está registrada como propuesta técnica **T-023** en el ROADMAP.md.

Hasta su implementación, el formato se valida mediante revisión de código en Pull Requests.

