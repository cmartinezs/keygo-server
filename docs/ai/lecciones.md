# AI Context — Lecciones Aprendidas

> Sub-documento de [`AI_CONTEXT.md`](../../AI_CONTEXT.md).
>
> Registra **errores encontrados, buenas prácticas descubiertas y convenciones adoptadas** durante
> el trabajo del agente en el repositorio. Consultar antes de implementar para no repetir errores pasados.
>
> **⚠️ Regla de actualización:** Al concluir cualquier tarea donde ocurra un error, bug, comportamiento
> inesperado o mejor patrón, agregar una entrada aquí **antes de cerrar la tarea**.

---

### [2026-03-29] Scripts de DB usaban `mvn` en lugar de `./mvnw` y faltaba `-pl keygo-supabase`

**Contexto:** Los scripts `migrate.sh`, `info.sh`, `validate.sh`, `repair.sh` y `clean.sh` en `docs/scripts/db/` usaban `mvn` (Maven sistema) y corrían desde `$PROJECT_ROOT` sin `-pl keygo-supabase`.

**Problema:** Tres errores combinados:
1. `mvn: command not found` — el proyecto solo garantiza el Maven wrapper (`./mvnw`), no Maven instalado en el sistema.
2. Sin `-pl keygo-supabase` — Flyway se intentaba ejecutar en todos los módulos; solo `keygo-supabase` tiene el plugin configurado.
3. Flyway 11+ tiene `cleanDisabled=true` por defecto — `flyway:clean` fallaba con "clean is not allowed as it has been disabled" sin `<cleanDisabled>false</cleanDisabled>` en el pom.

**Solución / Buena práctica:**
- Usar siempre `"$PROJECT_ROOT/mvnw"` en scripts de DB (garantiza el wrapper del repo).
- Agregar `-pl keygo-supabase` para apuntar directamente al módulo con Flyway.
- Agregar `--no-transfer-progress` para output más limpio.
- Para Flyway 10+: agregar `<cleanDisabled>false</cleanDisabled>` en el bloque `<configuration>` del `flyway-maven-plugin` si se necesita `flyway:clean` en scripts de desarrollo.

**Archivos clave:**
- `docs/scripts/db/migrate.sh`, `info.sh`, `validate.sh`, `repair.sh`, `clean.sh`
- `keygo-supabase/pom.xml` — sección `flyway-maven-plugin`

---

### [2026-03-29] Reestructuración de migraciones Flyway: consolidar en archivos por dominio

**Contexto:** El esquema original creció de forma acumulativa: V1–V26 donde V1-V9 eran el core, V10-V22 eran extensiones y parches, y V23-V26 eran correcciones de inconsistencias (columnas faltantes, renombrados). La historia era difícil de seguir y había redundancias (ej. V11 volvía a crear tablas que V8 ya creaba, V13/V22/V23/V24 aplicaban parches a tablas creadas en migraciones anteriores).

**Problema:**
1. Las migraciones acumulativas mezclaban el estado inicial con parches, haciendo imposible entender el estado final de una tabla con solo leer una migración.
2. Los archivos con nombres legados (ej. `V10__rename_membership_tables_to_plural.sql`) describían una operación que ya no era relevante en el contexto de una BD limpia.
3. Las entidades JPA debían compararse con múltiples archivos para validar la coherencia del schema.
4. La columna `subscriber_type` fue añadida en la entidad JPA (`AppPlanEntity`) pero el nuevo V10 no la incluía, causando inconsistencia latente que rompería `ddl-auto: validate`.

**Solución / Buena práctica:**
1. **Reiniciar la numeración desde V1** siempre que se haga una reestructuración total — usar V1=Drop ALL como pizarrón limpio.
2. **Una migración por dominio** (no por operación de parche): V3=Tenants, V4=ClientApps, V5=TenantUsers, V6=Memberships, V7=Auth, V8=Sessions, V9=EmailVerifications, V10=BillingCatalog, etc.
3. **Cada migración = estado final del modelo** de ese dominio. Los parches van directamente en el CREATE TABLE, nunca como ALTER TABLE separado (que solo tendría sentido si hubiera datos en producción que proteger).
4. **Validar coherencia contra las entidades JPA antes de hacer el DROP** en V1: las entidades son la fuente de verdad para el schema al usar `ddl-auto: validate`. Si una entidad tiene `@Column(name = "subscriber_type")`, la tabla debe tenerlo.
5. **No incluir en V1 (Drop ALL) el Drop de `flyway_schema_history`** — eso corresponde al script `./docs/scripts/db/clean.sh` que el usuario ejecuta manualmente.

**Archivos clave:**
- `keygo-supabase/src/main/resources/db/migration/V1__initial_schema.sql` — Drop ALL
- `keygo-supabase/src/main/resources/db/migration/V10__rename_membership_tables_to_plural.sql` — Billing Catalog (con `subscriber_type` en `app_plans`)
- `keygo-supabase/src/main/java/io/cmartinezs/keygo/supabase/billing/entity/AppPlanEntity.java` — entidad JPA con `subscriberType`

---

### [2026-03-29] Modelo de billing unificado: eliminar polimorfismo TENANT/TENANT_USER como tipo de suscriptor

**Contexto:** El modelo original de billing implementó dos tipos de suscriptor (`TENANT` = B2B, `TENANT_USER` = B2C) con columnas polimórficas (`subscriber_tenant_id`, `subscriber_tenant_user_id`) en `app_contracts`, `app_subscriptions` y `usage_counters`, y un campo `subscriber_type` en `app_plans`. Esto duplicó la lógica de activación (`ActivateAppContractUseCase` tenía dos ramas), complicó los constraints y generó migraciones correctivas (V23, V24) solo para añadir el discriminador faltante.
**Problema:** El modelo era innecesariamente complejo — en la práctica siempre hay una persona real que administra el servicio, y la base de toda entidad del sistema es el Tenant. La diferencia entre persona física y empresa es solo un detalle del perfil de facturación, no de la estructura de suscripción. Además faltaba por completo la representación de datos de facturación (RFC, domicilio fiscal) y métodos de pago.
**Solución / Buena práctica:**
1. **Un único tipo de suscriptor**: toda suscripción apunta a `subscriber_tenant_id` (NOT NULL). Eliminar `subscriber_tenant_user_id` y `subscriber_type` de `app_subscriptions`, `usage_counters` y `app_contracts`. Eliminar `subscriber_type` de `app_plans`.
2. **`billing_type` (PERSONAL/COMPANY)** reemplaza a `subscriber_type` en `app_contracts` — solo afecta qué datos de facturación se recolectan, no la estructura de la suscripción.
3. **Nueva tabla `tenant_billing_profiles`**: datos fiscales por Tenant (RFC, domicilio fiscal, tipo persona). Soporta múltiples perfiles con uno predeterminado.
4. **Nueva tabla `payment_methods`**: tokens PSP por Tenant (tarjeta, PayPal). Nunca datos crudos. Múltiples métodos con uno predeterminado.
5. Al diseñar entidades polimórficas con FKs opcionales, evaluar primero si el polimorfismo es necesario o si una sola FK con datos de presentación separados es suficiente.
**Archivos clave:** `V25__add_billing_support_tables.sql`, `V26__unify_billing_subscriber_model.sql`

---

### [2026-03-29] Tablas JPA de billing con columnas faltantes (invoices, usage_counters)

**Contexto:** V19 creó las tablas `invoices` y `usage_counters`. Al comparar contra `InvoiceEntity` y `UsageCounterEntity`, `invoices` tenía todas sus columnas pero `usage_counters` omitió `subscriber_type`, definida como `NOT NULL` en la entidad.
**Problema:** Hibernate falla al arrancar con `SchemaManagementException: Schema validation: missing column [subscriber_type] in table [usage_counters]`. La primera corrección propuesta añadía con `ALTER TABLE` TODAS las columnas de ambas tablas sin revisar primero cuáles ya existían — incorrecto e innecesario.
**Solución / Buena práctica:** Antes de escribir la migración correctiva, leer el `CREATE TABLE` original (V19 en este caso) y compararlo columna por columna contra el JPA entity. Solo agregar con `ALTER TABLE ADD COLUMN IF NOT EXISTS` las columnas que realmente faltan. En este caso: únicamente `subscriber_type` en `usage_counters`, con el mismo patrón de V23 (nullable → back-fill → NOT NULL + CHECK).
**Archivos clave:** `V19__add_billing_invoices_and_usage.sql`, `V24__add_billing_invoices_and_usage_counters.sql`, `UsageCounterEntity.java`

---

### [2026-03-29] Columna NOT NULL definida en entidad JPA pero ausente en migración Flyway (subscriber_type)

**Contexto:** `AppSubscriptionEntity` define el campo `subscriberType` con `@Column(name = "subscriber_type", nullable = false, length = 20)`, pero la migración V18 que creó la tabla `app_subscriptions` no incluyó esa columna.
**Problema:** Hibernate falla al arrancar con `SchemaManagementException: Schema validation: missing column [subscriber_type] in table [app_subscriptions]`. El error no se detectó en compilación porque JPA no valida el schema hasta conectarse a la DB en runtime.
**Solución / Buena práctica:** Nunca modificar la migración original ya aplicada. Crear una nueva migración (`V23__add_subscriber_type_to_app_subscriptions.sql`) que: (1) añade la columna como nullable, (2) back-fill las filas existentes derivando el valor de las FKs polimórficas ya presentes, (3) agrega `NOT NULL` y `CHECK` constraint. Al implementar una entidad con columnas `NOT NULL`, verificar que la migración correspondiente incluya **todas** las columnas antes de aplicarla.
**Archivos clave:** `keygo-supabase/src/main/resources/db/migration/V23__add_subscriber_type_to_app_subscriptions.sql`, `keygo-supabase/src/main/java/.../billing/entity/AppSubscriptionEntity.java`

---

### [2026-03-29] Imports duplicados al añadir anotaciones OpenAPI a controllers existentes

**Contexto:** Al agregar anotaciones OpenAPI/Swagger (`@Tag`, `@Operation`, `@ApiResponse`, `@SecurityRequirement`) a `AppBillingSubscriptionController`, se insertaron nuevos imports (`AppSubscription`, `SubscriberType`) antes de los ya existentes, resultando en dos declaraciones idénticas.
**Problema:** `javac` puede reportar error de compilación por imports duplicados; incluso si tolera la duplicidad, el código queda inconsistente y genera warnings. El build en ocasiones usa artefactos en caché local y no detecta el problema de inmediato.
**Solución / Buena práctica:** Al agregar imports a un archivo existente, revisar primero el bloque completo de imports y verificar que el nuevo import no esté ya presente. Ordenar el bloque: `api.*` → `app.*` → `domain.*` → librerías externas → `java.*`. Ejecutar `./mvnw -pl keygo-api compile` justo después para detectar el error sin esperar al full build.
**Archivos clave:** `keygo-api/src/.../api/billing/controller/AppBillingSubscriptionController.java`

---

### [2026-03-29] Ports inyectados pero nunca usados en CreateAppContractUseCase

**Contexto:** Al implementar la fase de billing (B-1→B-8), el `CreateAppContractUseCase` recibía en su constructor `UserRepositoryPort`, `PasswordHasherPort` y `EmailVerificationRepositoryPort`, pero ninguno era invocado en `execute()`. El email de verificación tampoco se enviaba.
**Problema:** El código compilaba y los tests pasaban (el use case estaba mockeado), enmascarando que el flujo real nunca enviaría el email de verificación al contratante. Además, el port de `EmailVerificationRepositoryPort` no aplica para contratos (requiere `tenant_user_id` FK), por lo que usar ese flujo hubiera fallado en runtime.
**Solución / Buena práctica:** Para el flujo de contratos, el código de verificación se almacena directamente en la tabla `app_contracts` (V22 migration), evitando la dependencia de una entidad que aún no existe (el TenantUser se crea en activación). El `CreateAppContractUseCase` ahora solo necesita `contractRepo`, `versionRepo`, `emailNotification`, `contractExpiryHours` y `verificationCodeExpiryMinutes`. Usar `SecureRandom` para generar el código numérico de 6 dígitos.
**Archivos clave:** `keygo-app/src/.../billing/contracting/usecase/CreateAppContractUseCase.java`, `V22__add_contract_verification_code.sql`

---

### [2026-03-29] Stub sin implementar en ActivateAppContractUseCase rama B2C

**Contexto:** `activateTenantUserBranch()` era un stub que retornaba `createSubscription(..., null, null, now)` — suscripción creada sin `subscriberTenantId` ni `subscriberTenantUserId`, lo que rompe la integridad del modelo.
**Problema:** El test del use case estaba mockeado a nivel de `AppContractRepositoryPort`, por lo que los tests pasaban sin detectar que la rama B2C generaba datos inválidos.
**Solución / Buena práctica:** Para B2C (TENANT_USER), obtener el `tenantId` del PROVEEDOR desde `clientAppRepo.findById(ClientAppId.of(contract.getClientAppId()))`, luego buscar o crear el `TenantUser` bajo ese tenant. Agregar `ClientAppRepositoryPort` al constructor del use case. El método `findById(ClientAppId)` fue añadido al port y su implementación en el adapter.
**Archivos clave:** `keygo-app/src/.../billing/contracting/usecase/ActivateAppContractUseCase.java`, `keygo-app/src/.../clientapp/port/ClientAppRepositoryPort.java`

---

### [2026-03-29] AppBillingSubscriptionController resolvía appId bajo el tenant del suscriptor

**Contexto:** `AppBillingSubscriptionController.getSubscription()` usaba `resolveClientAppId(tenantSlug, clientId)` que busca el `clientApp` BAJO el tenant del path. Para la gestión post-activación, `{tenantSlug}` es el tenant SUSCRIPTOR (p.ej. "acme-corp") pero la app (`keygo-platform`) pertenece al tenant PROVEEDOR ("keygo"). La búsqueda fallaba siempre.
**Problema:** Error de diseño: el `{clientId}` en los endpoints de suscripción/facturas es el `client_id` globalmente único del PROVEEDOR. No pertenece al tenant suscriptor. `resolveClientAppId` (que filtra por tenantId) era el método incorrecto para este contexto.
**Solución / Buena práctica:** Agregar `findByClientId(ClientId)` al `ClientAppRepositoryPort` (búsqueda global por client_id, sin filtro de tenant). Usar `resolveAppIdGlobally(clientId)` en el subscription controller. El `{tenantSlug}` en esos endpoints identifica al SUSCRIPTOR; el `{clientId}` identifica la app del PROVEEDOR (globalmente único por diseño OAuth2).
**Archivos clave:** `keygo-api/src/.../billing/controller/AppBillingSubscriptionController.java`, `keygo-app/src/.../clientapp/port/ClientAppRepositoryPort.java`

---

### [2026-03-29] replace_string_in_file reemplaza en lugar de agregar cuando el oldString es el import a mantener

**Contexto:** Al intentar agregar imports a un archivo de test, se usó el import existente como `oldString` y los nuevos imports como `newString`, lo que reemplazó (eliminó) el import original.
**Problema:** El test dejó de compilar porque `CreateAppContractRequest` ya no estaba importado.
**Solución / Buena práctica:** Al agregar imports, incluir TODOS los imports relevantes (tanto el existente como los nuevos) en el `newString`. Verificar siempre con `./mvnw -pl <módulo> compile` antes de ejecutar el full test suite.

---

### [2026-03-28] `BaseResponse` está en sub-paquete `response`, no directamente en `shared`

**Contexto:** Implementación del módulo de billing — controllers en `keygo-api`.
**Problema:** Los imports usaban `io.cmartinezs.keygo.api.shared.BaseResponse` pero la clase real está en `io.cmartinezs.keygo.api.shared.response.BaseResponse`. Generó errores de compilación en los tres controllers de billing.
**Solución / Buena práctica:** Siempre verificar la ubicación real de clases del paquete `shared` antes de escribir imports. Ejecutar `find ... -name "BaseResponse.java"` o buscar un controller existente de referencia.
**Archivos clave:** `keygo-api/src/.../api/shared/response/BaseResponse.java`

---

### [2026-03-28] `List.of()` sin tipo genérico explícito falla con tipos incompatibles en Java 21

**Contexto:** Implementación de use cases de catálogo de billing.
**Problema:** Código como `var entitlements = versions.isEmpty() ? List.of() : entitlementRepo.findByAppPlanVersionId(...)` produce error de compilación porque Java infiere `List<Object>` para `List.of()` cuando hay una rama alternativa de tipo diferente. Aplica cuando el tipo esperado no puede inferirse del contexto.
**Solución / Buena práctica:** Usar siempre el tipo explícito: `List.<AppPlanEntitlement>of()` o declarar la variable con el tipo completo `List<AppPlanEntitlement> entitlements = ...`.
**Archivos clave:** `keygo-app/src/.../billing/catalog/usecase/GetAppPlanCatalogUseCase.java`

---

### [2026-03-28] Patrón inner class `Response` en DTOs para visibilidad real del schema en Swagger

**Contexto:** Los controllers devuelven `ResponseEntity<BaseResponse<T>>` y todos usaban `@Schema(implementation = BaseResponse.class)` en `@ApiResponse`, lo que hacía que Swagger UI solo mostrara la estructura de `BaseResponse` con `data: {}` — el frontend debía inferir la estructura del campo `data` sin ayuda de la documentación.

**Problema:** springdoc-openapi no puede inferir el tipo genérico `T` desde `BaseResponse.class` en tiempo de documentación. Cuando se sobreescribe el schema con `@Schema(implementation = BaseResponse.class)`, la información de tipo se pierde completamente y `data` aparece como un objeto vacío.

**Solución / Buena práctica:** Agregar una inner class estática `Response` (y `ListResponse`, `PagedResponse` según corresponda) en cada DTO de respuesta que extienda `BaseResponse<PropioDTOType>`:

```java
// En cada DTO (p. ej. TenantData.java):
public static final class Response extends BaseResponse<TenantData> {
    public Response() { super(LocalDateTime.now()); }
}
public static final class PagedResponse extends BaseResponse<PagedData<TenantData>> {
    public PagedResponse() { super(LocalDateTime.now()); }
}
// En cada DTO para listas (p. ej. UserData.java):
public static final class ListResponse extends BaseResponse<List<UserData>> {
    public ListResponse() { super(LocalDateTime.now()); }
}
```

Luego en el controller usar la inner class en el `@ApiResponse` del código 2xx:
```java
@ApiResponse(responseCode = "200",
    content = @Content(schema = @Schema(implementation = TenantData.Response.class)))
```

springdoc lee la parametrización de la superclase (`BaseResponse<TenantData>`) vía reflexión y genera el campo `data` con el tipo correcto. Las clases 4xx/5xx se mantienen con `BaseResponse.class` ya que son `BaseResponse<Void>`.

**Nota técnica:** El constructor de `BaseResponse` es `public BaseResponse(LocalDateTime date)` (generado por `@RequiredArgsConstructor` sobre el campo `final date`). Las inner classes deben llamar `super(LocalDateTime.now())`. No se instancian en runtime — solo se usan como referencia de schema.

**Archivos clave:**
- Todos los DTOs en `keygo-api/*/response/*Data.java` — agregar inner class `Response`
- Todos los controllers en `keygo-api/*/controller/*Controller.java` — actualizar `@ApiResponse` 2xx

---

### [2026-03-28] Lombok debe declararse explícitamente en cada módulo que lo use

**Contexto:** Al agregar `PlatformDashboardResult` y `PlatformStatsResult` al módulo `keygo-app` con anotaciones `@Getter` y `@Builder`, IntelliJ reportaba "symbol not found" y no podía arrancar la app desde el IDE, aunque `./mvnw compile` era exitoso.

**Problema:** `keygo-app/pom.xml` no declaraba Lombok como dependencia. Maven compilaba de todas formas porque Lombok transitaba desde `keygo-domain` (scope `provided`) al classpath del reactor multi-módulo. Sin embargo, IntelliJ gestiona el procesamiento de anotaciones por módulo y creaba para `keygo-app` el perfil "Maven default annotation processors profile" (sin `processorPath` de Lombok), lo que impedía generar los métodos `get*()` y el builder, provocando el error de símbolo.

**Solución / Buena práctica:**
- Declarar `org.projectlombok:lombok` con `scope=provided` en **cada módulo** que use anotaciones Lombok, sin importar si ya está en un módulo del que se depende.
- Configurar `maven-compiler-plugin` con `annotationProcessorPaths` en ese mismo módulo.
- Mover el módulo al perfil de IntelliJ que incluye el `processorPath` de Lombok (`.idea/compiler.xml`).
- Regla mnemotécnica: **"Si usas `@Getter`/`@Builder`/`@Setter` en un archivo, Lombok va en el `pom.xml` de ese módulo"**.

**Archivos clave:**
- `keygo-app/pom.xml` — agregar `lombok` + `maven-compiler-plugin` con APT
- `.idea/compiler.xml` — mover `keygo-app` del perfil sin processorPath al perfil con Lombok

---

### [2026-03-28] Usar GROUP BY en lugar de N queries por status en puertos de dashboard

**Contexto:** Implementación del endpoint `GET /api/v1/admin/platform/dashboard` que consolida ~25 métricas de la plataforma en una sola respuesta.

**Problema:** El diseño inicial del puerto tenía métodos como `long countTenantsByStatus(TenantStatus status)`, `long countSessionsByStatus(String status)`, etc. El use case llamaba a cada método 2–4 veces pasando distintos status. Ejemplo para sessions:
```java
// ❌ Antes: 3 queries separadas
long activeSessions     = dashboardPort.countSessionsByStatus("ACTIVE");
long expiredSessions    = dashboardPort.countSessionsByStatus("EXPIRED");
long terminatedSessions = dashboardPort.countSessionsByStatus("TERMINATED");
```
En total, el dashboard ejecutaba ~25 queries individuales de `COUNT WHERE status = ?` por cada petición.

**Solución / Buena práctica:**
1. Cambiar la firma del puerto para devolver un `Map<K, Long>` (sin parámetro):
   ```java
   // ✅ Después: 1 query GROUP BY
   Map<String, Long> countSessionsByStatus();
   ```
2. En el repositorio JPA, agregar un método con `@Query` JPQL de GROUP BY:
   ```java
   @Query("SELECT s.status, COUNT(s) FROM SessionEntity s GROUP BY s.status")
   List<Object[]> countGroupByStatus();
   ```
3. En el adaptador, usar un helper genérico para convertir el `List<Object[]>` a `Map`:
   ```java
   @SuppressWarnings("unchecked")
   private <K> Map<K, Long> toCountMap(List<Object[]> rows) {
     return rows.stream().collect(Collectors.toMap(
         row -> (K) row[0],
         row -> ((Number) row[1]).longValue()));
   }
   // Para status tipo String (sessions, tokens, auth codes, signing keys):
   private Map<String, Long> toStringCountMap(List<Object[]> rows) {
     return rows.stream().collect(Collectors.toMap(
         row -> row[0].toString(),
         row -> ((Number) row[1]).longValue()));
   }
   ```
4. En el use case, consumir el mapa con `getOrDefault(status, 0L)` para evitar NPE cuando algún status no tiene filas:
   ```java
   var sessionCounts      = dashboardPort.countSessionsByStatus();
   long activeSessions    = sessionCounts.getOrDefault("ACTIVE", 0L);
   long expiredSessions   = sessionCounts.getOrDefault("EXPIRED", 0L);
   long terminatedSessions = sessionCounts.getOrDefault("TERMINATED", 0L);
   ```
5. Para status de tipo enum (JPA devuelve el enum directamente desde JPQL), el cast genérico `(K) row[0]` funciona y el compilador solo genera un `unchecked` warning — suprimir con `@SuppressWarnings("unchecked")` en el helper.
6. Definir los literales de status como constantes privadas en el use case para evitar el warning de SonarQube de literales duplicados:
   ```java
   private static final String STATUS_ACTIVE = "ACTIVE";
   ```
7. En tests, reemplazar stubs individuales `when(port.countX(Status.Y)).thenReturn(n)` por stubs de mapa `when(port.countX()).thenReturn(Map.of(Status.Y, n, ...))`. Para estatus ausentes (count = 0), simplemente omitir la entrada del mapa — `getOrDefault` la maneja.

**Resultado:** ~25 queries individuales → ~9 queries GROUP BY por petición al dashboard. El patrón aplica a cualquier caso donde el mismo método de conteo se llame N veces con distintos valores de un enum finito o conjunto cerrado de strings.

**Archivos clave:**
- `keygo-app/.../platform/port/PlatformDashboardPort.java` — firmas `Map<K,Long> countX()`
- `keygo-supabase/.../platform/adapter/PlatformDashboardAdapter.java` — helpers + implementaciones
- `keygo-supabase/.../auth/repository/SessionJpaRepository.java` — patrón JPQL GROUP BY
- `keygo-app/.../platform/usecase/GetPlatformDashboardUseCase.java` — constantes + `getOrDefault`
- `keygo-app/.../platform/usecase/GetPlatformDashboardUseCaseTest.java` — stubs con `Map.of`

---


**Contexto:** Swagger UI mostraba campos en camelCase (`clientId`, `redirectUris`, `createdAt`, `firstName`, etc.) cuando la API real los serializa en snake_case (`client_id`, `redirect_uris`, `created_at`, `first_name`).

**Problema:** El bean `JsonMapperBuilderCustomizer` configura `PropertyNamingStrategies.SNAKE_CASE` en el runtime de Jackson 3, pero SpringDoc 3.0.1 genera los schemas OpenAPI por reflexión sobre los campos Java **independientemente** de ese customizer. Al no existir `spring.jackson.property-naming-strategy` en `application.yml`, `JacksonProperties` no tenía snake_case configurado, y SpringDoc mostraba los nombres Java originales (camelCase). Los campos con `@JsonProperty` explícito (como `TokenData`) no estaban afectados.

**Solución / Buena práctica:**
1. Agregar `spring.jackson.property-naming-strategy: SNAKE_CASE` en `application.yml` para que `JacksonProperties` lo reciba y SpringDoc lo pueda leer.
2. Como refuerzo definitivo ante incompatibilidades entre Spring Boot 4 / Jackson 3 / SpringDoc 3.0.1, crear un `SnakeCaseModelConverter` que implemente `io.swagger.v3.core.converter.ModelConverter`, renombre los keys del schema de camelCase a snake_case post-resolución y se registre como `@Bean` en `OpenApiConfig` (SpringDoc lo autodescubre).
3. La conversión es idempotente: un campo ya en snake_case (p.ej. `access_token` de `@JsonProperty`) no cambia.
4. Al usar `-pl keygo-run test` sin `-am`, el build falla porque los módulos dependientes no están compilados. Usar siempre `./mvnw -pl keygo-run -am test` o `./mvnw test` completo.

**Archivos clave:**
- `keygo-run/src/main/resources/application.yml` — `spring.jackson.property-naming-strategy: SNAKE_CASE`
- `keygo-run/src/main/java/.../config/SnakeCaseModelConverter.java` — converter personalizado
- `keygo-run/src/main/java/.../config/OpenApiConfig.java` — bean `snakeCaseModelConverter()`
- `keygo-run/src/test/java/.../config/SnakeCaseModelConverterTest.java` — 31 tests unitarios

---

### [2026-03-28] Extender una interfaz de puerto rompe todas las anonymous classes en tests

**Contexto:** Al agregar `getEnvironment()` y `getStatus()` a `ServiceInfoProvider` (puerto existente), el build falló en 3 archivos de test con "is not abstract and does not override abstract method".

**Problema:** Los tests de `ServiceInfoControllerTest`, `GetServiceInfoUseCaseTest` y `ApplicationConfigTest` usaban anonymous classes para implementar `ServiceInfoProvider`. Al agregar métodos abstractos a la interfaz, todas esas classes dejan de compilar.

**Solución / Buena práctica:**
1. Después de extender cualquier interfaz de puerto, buscar con `grep -rn "new ServiceInfoProvider"` (o el nombre de la interfaz) todos los lugares donde se implementa inline.
2. Actualizar todas las anonymous classes encontradas agregando los nuevos métodos antes de intentar compilar.
3. Alternativa más robusta: usar Mockito (`@Mock`) en los tests en lugar de anonymous classes — los mocks no requieren implementar todos los métodos.
4. Verificar con `./mvnw clean package -DskipTests` primero y luego `./mvnw test` para separar errores de compilación de fallos de lógica.

**Archivos clave:**
- `keygo-api/src/test/.../platform/controller/ServiceInfoControllerTest.java`
- `keygo-app/src/test/.../platform/usecase/GetServiceInfoUseCaseTest.java`
- `keygo-run/src/test/.../config/ApplicationConfigTest.java`

---

### [2026-03-27] Métodos de use case sin scope de tenant permiten acceso cross-tenant

**Contexto:** Revisión de `TenantMembershipController` y sus use cases `ListMembershipsUseCase` y `RevokeMembershipUseCase`.

**Problema:** El controller recibía el `{tenantSlug}` del path pero no lo pasaba a los use cases:
- `listMembershipsUseCase.listByUserId(userId)` y `listByClientAppId(clientAppId)` filtraban solo por ID, devolviendo membresías de cualquier tenant que posea ese usuario/app.
- `revokeMembershipUseCase.execute(membershipId)` eliminaba la membresía sin verificar si pertenece al tenant del URL, permitiendo que un ADMIN_TENANT borre membresías de otro tenant si conoce el UUID.
El `@PreAuthorize` con `hasTenantAccess()` solo protege la autenticación/autorización del JWT pero NO garantiza que los datos retornados/eliminados pertenezcan al tenant del path.

**Solución / Buena práctica:**
1. Agregar métodos tenant-scoped al `MembershipRepositoryPort`: `findByUserIdAndTenantSlug`, `findByClientAppIdAndTenantSlug`, `findByIdAndTenantSlug`.
2. Implementarlos con JPQL usando navegación JPA: `m.user.tenant.slug` y `m.clientApp.tenant.slug`.
3. Actualizar los use cases para recibir `tenantSlug` y usar siempre los métodos scoped.
4. Regla general: cuando un controller tiene `{tenantSlug}` en el path, **todos** los use cases invocados desde ese controller deben recibir y usar el slug para acotar sus queries.
5. El `@PreAuthorize` controla QUIÉN puede acceder; la validación de scope de datos (QUÉ tenant) es responsabilidad del use case/repositorio.

**Archivos clave:**
- `keygo-app/.../membership/port/MembershipRepositoryPort.java`
- `keygo-app/.../membership/usecase/ListMembershipsUseCase.java`
- `keygo-app/.../membership/usecase/RevokeMembershipUseCase.java`
- `keygo-supabase/.../membership/repository/MembershipJpaRepository.java`
- `keygo-supabase/.../membership/adapter/MembershipRepositoryAdapter.java`
- `keygo-api/.../membership/controller/TenantMembershipController.java`

### [2026-03-27] Colección Postman corrompida por edición parcial del agente

**Contexto:** Verificación de integridad de los archivos Postman (`KeyGo-Server.postman_collection.json` y `KeyGo-Server-Local.postman_environment.json`).

**Problema:** Se encontraron 4 errores que impedían importar la colección en Postman:
1. **Línea 1 suelta**: un fragmento `"description": "..."` antes del `{` de apertura del objeto JSON raíz (resultado de una edición anterior mal aplicada).
2. **Objeto `nameLike` malformado**: dentro del array `query` del request `GET List Tenants`, faltaba el `{` de apertura y había dos propiedades `"key"` duplicadas (`name_like` y `nameLike`).
3. **Cierre de objeto `url` faltante**: el objeto `url` no tenía `}` después del array `query`.
4. **`"description"` duplicado**: dos descripciones en el mismo objeto `request` (versión antigua con `name_like` y versión nueva con `nameLike`).

Adicionalmente, el entorno carecía de 6 variables usadas por la colección (`accessToken`, `adminToken`, `codeChallenge`, `codeVerifier`, `state`, `roleCode`) y la colección tenía 2 bugs funcionales:
- El script del paso "3. POST Token Exchange" no guardaba `accessToken` en el entorno.
- El request "1. GET Authorize" no generaba automáticamente los parámetros PKCE.

**Solución / Buena práctica:**
- Siempre validar con `python3 -m json.tool <archivo>.json` después de editar colecciones Postman.
- Al actualizar un request que ya existe, usar `replace_string_in_file` con suficiente contexto para evitar inserciones duplicadas o líneas sueltas.
- Las variables `{{...}}` usadas en la colección deben estar declaradas como variable de entorno, variable de colección (`"variable"` en el raíz), o seteadas por scripts `pm.environment.set`/`pm.collectionVariables.set`.
- Los scripts de token exchange deben siempre hacer `pm.environment.set('accessToken', ...)` al recibir un `access_token` exitoso.
- Los pre-request scripts de endpoints PKCE deben generar `codeVerifier`, `codeChallenge` y `state` automáticamente.

**Archivos clave:**
- `docs/postman/KeyGo-Server.postman_collection.json`
- `docs/postman/KeyGo-Server-Local.postman_environment.json`



**Contexto:** Tarea de normalización de `@RequestParam` a snake_case. Se intentó actualizar dos secciones (§9.4 y §14.2.7) de `FRONTEND_DEVELOPER_GUIDE.md` en una sola llamada a `insert_edit_into_file` usando comentarios `...existing code...` como marcadores.

**Problema:** El tool interpretó `...existing code...` entre §9.4 y §14.2.7 como "todo el contenido intermedio". Al aplicar el diff, eliminó el bloque §14.2.x completo (684 líneas) que existía entre §14.1.2 y §15. El archivo pasó de ~2572 a ~1888 líneas.

**Solución / Buena práctica:**
- Para archivos Markdown grandes con secciones similares/duplicadas (como §9 y §14), **NO** usar una sola llamada `insert_edit_into_file` para múltiples secciones distantes.
- Usar `replace_string_in_file` con contexto muy específico (3-5 líneas únicas antes y después del cambio) para cada ocurrencia por separado.
- Si hay múltiples ocurrencias del mismo texto, incluir contexto adicional (línea del heading de la sección) en el `oldString` para que sea único.
- Verificar con `wc -l` antes y después de editar archivos Markdown grandes.

**Archivos clave:** `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`

---


**Contexto:** Al ejecutar con perfil `local` (`application-local.yml` tiene `keygo.bootstrap.enabled: false`), el endpoint `GET /api/v1/tenants` devolvía 403 Access Denied incluso con un JWT Bearer válido.

**Problema:** Cuando `bootstrapProperties.isEnabled()` es `false`, el filtro hacía `filterChain.doFilter()` directamente sin establecer ningún `SecurityContext`. La anotación `@PreAuthorize("hasRole('ADMIN')")` evaluaba contra un contexto vacío (sin `Authentication`) → lanzaba `AuthorizationDeniedException` → 403. El JWT del usuario nunca era leído en este escenario.

**Solución / Buena práctica:**
- Cuando bootstrap está desactivado (modo dev/test), el filtro debe establecer un **authentication de bypass** en el `SecurityContext` con `ROLE_ADMIN`, `ROLE_ADMIN_TENANT` y `ROLE_USER`.
- El `TenantAuthorizationEvaluator` hace short-circuit al detectar `ROLE_ADMIN` (retorna `true` directamente), por lo que el principal del bypass (un `Map` compatible) es suficiente.
- El `SecurityContextHolder.clearContext()` sigue invocándose en el bloque `finally` para no contaminar solicitudes posteriores.
- Agregar test que valide con `doAnswer` que el contexto tiene la autenticación de bypass durante la ejecución de la cadena de filtros.

**Archivos clave:**
- `keygo-run/src/main/java/…/filter/BootstrapAdminKeyFilter.java` (método `setBypassAuthentication()`)
- `keygo-run/src/main/resources/application-local.yml` (tiene `keygo.bootstrap.enabled: false`)

---

### [2026-03-27] JWT roles en minúsculas vs. `@PreAuthorize("hasRole('ADMIN')")` en mayúsculas — case mismatch

**Contexto:** El endpoint `GET /api/v1/tenants` devolvía `Access Denied` para `keygo_admin` con un JWT válido que incluía `"roles": ["admin"]`.

**Problema:** El `BootstrapAdminKeyFilter` construía las `GrantedAuthority` como `ROLE_admin` (preservando las minúsculas del claim JWT). La anotación `@PreAuthorize("hasRole('ADMIN')")` busca `ROLE_ADMIN` (mayúsculas). El mismatch causaba que la autorización fallara siempre para tokens generados por el sistema (que emite roles en minúsculas).

**Solución / Buena práctica:**
- Normalizar los roles a mayúsculas en el filtro al construir las authorities: `.map(role -> "ROLE_" + role.toUpperCase())`.
- Esto asegura que el filtro siempre produce `ROLE_ADMIN`, `ROLE_ADMIN_TENANT`, etc., independientemente del case que tenga el JWT.
- Los tests de `BootstrapAdminKeyFilterTest` ya mockeaban con `"ADMIN"` (uppercase), lo que los hacía pasar pero enmascaraba el bug con tokens reales.

**Archivos clave:**
- `keygo-run/src/main/java/…/filter/BootstrapAdminKeyFilter.java` (método `authenticateBearer`)

---

### [2026-03-27] `AuthorizationDeniedException` interceptada por `ExceptionTranslationFilter` antes del `@RestControllerAdvice`

**Contexto:** Al fallar `@PreAuthorize`, la excepción `AuthorizationDeniedException` (que extiende `AccessDeniedException`) no era capturada por el handler específico en `GlobalExceptionHandler` sino que llegaba al handler genérico de `Exception`. El comportamiento real es que `ExceptionTranslationFilter` de Spring Security intercepta `AccessDeniedException` antes de que los resolvers de Spring MVC puedan actuar, llamando al `AccessDeniedHandler` por defecto que devuelve una respuesta 403 sin formato JSON.

**Problema:** Sin un `AccessDeniedHandler` personalizado en `SecurityConfig`, las respuestas de autorización denegada eran respuestas HTTP 403 sin cuerpo JSON (o con el cuerpo por defecto de Spring Security), rompiendo el contrato de `BaseResponse<T>` del API.

**Solución / Buena práctica:**
- Configurar un `AccessDeniedHandler` custom en `SecurityConfig` que escriba un `BaseResponse<ErrorData>` con `ResponseCode.INSUFFICIENT_PERMISSIONS` y status 403.
- El `@ExceptionHandler(AccessDeniedException.class)` en `GlobalExceptionHandler` es necesario como segunda línea de defensa para casos donde la excepción sí llega al DispatcherServlet (p.ej. cuando Spring MVC intercepta antes que `ExceptionTranslationFilter`).
- Ambos mecanismos deben coexistir: `AccessDeniedHandler` en `SecurityConfig` + `@ExceptionHandler` en `GlobalExceptionHandler`.

**Archivos clave:**
- `keygo-run/src/main/java/…/config/security/SecurityConfig.java` (bean `keyGoAccessDeniedHandler`)
- `keygo-api/src/main/java/…/error/GlobalExceptionHandler.java` (handler `handleAccessDeniedException`)

---

### [2026-03-27] `replace_string_in_file` en archivos de test puede dejar contenido duplicado fuera de la clase

**Contexto:** Al ampliar `PlatformTenantControllerTest` reemplazando el bloque de `@Mock` + `@InjectMocks`, la tool reemplazó únicamente la cabecera de la clase pero dejó el cuerpo original del archivo intacto a continuación, resultando en métodos y código fuera de los corchetes de cierre de la clase.

**Problema:** El compilador reportó `unnamed classes are a preview feature` y `class, interface, enum, or record expected` porque el contenido extra quedó como código suelto fuera de la clase. El `replace_string_in_file` emparejó correctamente el `oldString` pero no eliminó el bloque posterior al match.

**Solución / Buena práctica:**
- Al reemplazar la cabecera de una clase de test que ya tenía contenido, incluir en el `oldString` **todo el cuerpo** hasta la llave de cierre, o usar un `oldString` que abarque hasta el final del contenido que debe ser reemplazado.
- Alternativamente, reescribir el archivo completo (cuando la tool lo permita) en lugar de hacer un reemplazo parcial de la cabecera.
- Tras cualquier operación de edición de test, verificar con `./mvnw -pl <módulo> test-compile` antes de ejecutar tests completos.

---

### [2026-03-27] `SigningKeyInitializer` debe incluir el perfil `local` — sin él no hay clave de firma en H2


**Problema:** `SigningKeyBootstrapService` tenía `@Profile("supabase")` → no corre en `local`. `data-local.sql` no incluye seed de `signing_keys`. Con perfil `local` el banco H2 arrancaba vacío de claves, y la primera llamada a `/oauth2/token` o similar explotaba con `IllegalStateException: No active signing key found`.

**Solución / Buena práctica:**
- `SigningKeyInitializer` debe declarar `@Profile({"supabase", "local"})` para cubrir ambos perfiles.
- Además, `SigningKeyBootstrapService` (en `keygo-run/config/auth/`) es redundante: hace exactamente lo mismo que `SigningKeyInitializer` pero solo para `supabase`. Con ambos activos en el mismo perfil, el segundo encuentra la clave del primero y hace no-op. Propuesta T-067: eliminarlo.
- `data-local.sql` **no debe** incluir claves de firma hard-codeadas: el inicializador las genera al vuelo y las persiste en el banco H2 file-based. Esto es correcto.
- Para producción, **nunca auto-generar** la clave privada RSA: ver T-028 (KMS externo).

**Archivos clave:**
- `keygo-run/src/main/java/io/cmartinezs/keygo/run/startup/SigningKeyInitializer.java` — `@Profile({"supabase", "local"})`
- `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/auth/SigningKeyBootstrapService.java` — candidato a eliminar (T-067)
- `keygo-run/src/main/resources/application-local.yml` — perfil `local` con H2 file-based
- `keygo-run/src/main/resources/data-local.sql` — sin seed de signing_keys (correcto)

---

## Formato de entrada

```markdown
### [YYYY-MM-DD] Título descriptivo de la lección
**Contexto:** Breve descripción de la tarea o escenario que generó el aprendizaje.
**Problema:** Qué falló, qué comportamiento inesperado se detectó o qué patrón mejoró.
**Solución / Buena práctica:** Cómo se resolvió o qué debe hacerse en el futuro.
**Archivos clave:** (opcional) Rutas relevantes para contextualizar la solución.
```

---

## Índice de lecciones

| Fecha | Tema | Categoría |
|---|---|---|
| 2026-03-27 | [`data-local.sql` debe ser copia fiel de V14+V15 — mismos UUIDs, 2 tenants, 5 usuarios, hashes V15](#2026-03-27-data-localsql-debe-ser-copia-fiel-de-v14v15--mismos-uuids-2-tenants-5-usuarios-hashes-v15) | DB / Seed / H2 / Compatibilidad |
| 2026-03-27 | [BD H2 file-based persiste entre reinicios: borrar `db/*.mv.db` al cambiar seed o esquema](#2026-03-27-bd-h2-file-based-persiste-entre-reinicios-borrar-dbmvdb-al-cambiar-seed-o-esquema) | H2 / Local / Ops |
| 2026-03-26 | [`ON CONFLICT DO NOTHING` es sintaxis PostgreSQL — usar `INSERT ... SELECT ... WHERE NOT EXISTS` en H2](#2026-03-26-on-conflict-do-nothing-es-sintaxis-postgresql--usar-insert-select-where-not-exists-en-h2) | SQL / H2 / Compatibilidad |
| 2026-03-26 | [El agente nunca debe ejecutar comandos `git` directamente — usar listas de comandos sugeridos](#2026-03-26-el-agente-nunca-debe-ejecutar-comandos-git-directamente) | Agente / Reglas de trabajo |
| 2026-03-26 | [Verificar estado de compilación pre-existente con `./mvnw clean` antes de atribuir errores a cambios propios](#2026-03-26-verificar-estado-de-compilación-pre-existente-con-mvnw-clean) | Build / Testing |
| 2026-03-26 | [Documentar Swagger: 5 controllers de auth/OIDC sin anotaciones `@Tag`/`@Operation` y grupos desactualizados](#2026-03-26-documentar-swagger-controllers-sin-anotaciones-y-grupos-desactualizados) | API / Swagger / Docs |
| 2026-03-26 | [Contraseñas de seed SQL deben documentarse siempre junto al hash BCrypt](#2026-03-26-contraseñas-de-seed-sql-deben-documentarse-junto-al-hash-bcrypt) | DB / Seed / Convenciones |
| 2026-03-26 | [ADR-001: documentar decisiones de error handling como ADR (`docs/keygo-ui/ADR-001-error-handling-oauth2.md`)](#2026-03-26-adr-001-documentar-decisiones-de-error-handling-como-adr) | API / Error Handling / Docs |
| 2026-03-26 | [Subclasificar `CLIENT_REQUEST` en `CLIENT_TECHNICAL` vs `USER_INPUT` mejora triage de UI y soporte](#2026-03-26-subclasificar-client_request-en-client_technical-vs-user_input-mejora-triage-de-ui-y-soporte) | API / Error Handling |
| 2026-03-26 | [En Spring Framework 7, `HttpMessageNotReadableException` en tests requiere `HttpInputMessage`](#2026-03-26-en-spring-framework-7-httpmessagenotreadableexception-en-tests-requiere-httpinputmessage) | Testing / Spring |
| 2026-03-26 | [Clasificar errores por origen (`CLIENT_REQUEST`, `BUSINESS_RULE`, `SERVER_PROCESSING`) mejora diagnóstico frontend y soporte](#2026-03-26-clasificar-errores-por-origen-client_request-business_rule-server_processing-mejora-diagnóstico-frontend-y-soporte) | API / Error Handling |
| 2026-03-26 | [Errores API con `BaseResponse.data`: detalle tecnico en `local/dev`, mensaje amigable en otros perfiles](#2026-03-26-errores-api-con-baseresponsedata-detalle-tecnico-en-localdev-mensaje-amigable-en-otros-perfiles) | API / Error Handling / Testing |
| 2026-03-26 | [CORS en Spring Boot 4: `CorsConfigurationSource` + `http.cors(...)` en `SecurityFilterChain`](#2026-03-26-cors-en-spring-boot-4-configurar-corsconfigurationsource--httpcors-en-securityfilterchain) | Security / CORS |
| 2026-03-26 | [ClientApp CONFIDENTIAL en tests de adapter/mapper debe incluir `hashedSecret`](#2026-03-26-clientapp-confidential-en-tests-de-adaptermapper-debe-incluir-hashedsecret) | Testing / Dominio |
| 2026-03-26 | [Missing query param en MockMvc standalone: sin handler específico cae en 500 (`OPERATION_FAILED`)](#2026-03-26-missing-query-param-en-mockmvc-standalone-sin-handler-específico-cae-en-500-operation_failed) | Testing / Error Handling |
| 2026-03-26 | [`ACCEPT_CASE_INSENSITIVE_PROPERTIES` no convierte snake_case a camelCase — se requiere `@JsonProperty` en DTOs OAuth2](#2026-03-26-accept_case_insensitive_properties-no-convierte-snake_case-a-camelcase--se-requiere-jsonproperty-en-dtos-oauth2) | Jackson / Deserialización / OAuth2 |
| 2026-03-26 | [OAuth2 authorize: para query params en snake_case usar `@RequestParam(name = ...)` en lugar de depender de `@ModelAttribute`](#2026-03-26-oauth2-authorize-para-query-params-en-snake_case-usar-requestparamname---en-lugar-de-depender-de-modelattribute) | API / Spring MVC / OAuth2 |
| 2026-03-26 | [Shebang faltante en shell script causa ejecución con `sh` en lugar de `bash` → `Bad substitution` y rutas incorrectas](#2026-03-26-shebang-faltante-en-shell-script-causa-ejecución-con-sh-en-lugar-de-bash) | Scripts / Shell |
| 2026-03-26 | [Vitest en ejemplos aislados: importar `describe/it/expect` explícitamente evita depender de globals](#2026-03-26-vitest-en-ejemplos-aislados-importar-describeitexpect-explícitamente-evita-depender-de-globals) | Frontend / Testing |
| 2026-03-26 | [Hosted login compartido: la UI central no debe apropiarse del contexto OAuth2 del tenant origen](#2026-03-26-hosted-login-compartido-la-ui-central-no-debe-apropiarse-del-contexto-oauth2-del-tenant-origen) | OAuth2 / Frontend / Arquitectura |
| 2026-03-26 | [Scripts dispersos en módulos: centralizar en `scripts/` + menú principal evita rutas rotas y mejora DX](#2026-03-26-scripts-dispersos-en-módulos-centralizar-en-scripts--menú-principal-evita-rutas-rotas-y-mejora-dx) | Scripts / Shell / DX |
| 2026-03-25 | [Mermaid en Markdown: evitar signos de interrogación invertidos en nodos validados por parser](#2026-03-25-mermaid-en-markdown-evitar-signos-de-interrogación-invertidos-en-nodos-validados-por-parser) | Documentación / Tooling |
| 2026-03-25 | [Tests Maven por módulo en monorepo: usar `-am` para resolver dependencias de clases](#2026-03-25-tests-maven-por-módulo-en-monorepo-usar--am-para-resolver-dependencias-de-clases) | Build / Testing |
| 2026-03-25 | [Bearer-only admin auth: `@PreAuthorize` + tenant match token/path](#2026-03-25-bearer-only-admin-auth-preauthorize--tenant-match-tokenpath) | Security / Authorization |
| 2026-03-25 | [Claims map puede ser inmutable en tests: copiar antes de agregar `tenant_slug`](#2026-03-25-claims-map-puede-ser-inmutable-en-tests-copiar-antes-de-agregar-tenant_slug) | OAuth2 / Testing |
| 2026-03-24 | [Endpoint `POST /roles`: evitar respuestas "exitosas" sin persistencia real](#2026-03-24-endpoint-post-roles-evitar-respuestas-exitosas-sin-persistencia-real) | API / Hexagonal / Persistencia |
| | 2026-03-24 | [Claim `roles` en JWT para que el frontend lea roles directamente desde el JWT](#2026-03-24-claim-roles-en-jwt-para-que-el-frontend-keygo-ui-lea-los-roles-directamente-desde-el-jwt-sin-llamadas-adicionales-a-la-api) | OAuth2 / JWT / Arquitectura |
| | 2026-03-24 | [JWT admin en filtro: rutas OAuth2 públicas + Bearer con rol admin](#2026-03-24-jwt-admin-en-filtro-rutas-oauth2-públicas--bearer-con-rol-admin) | Security / Filter |
| 2026-03-24 | [SigningKeyInitializer: auto-generar clave RSA en startup con @Profile](#2026-03-24-signingkeyinitializer-auto-generar-clave-rsa-en-startup) | Spring / Startup |
| 2026-03-24 | [replace_string_in_file duplica clase si el string a reemplazar es solo la cabecera](#2026-03-24-replace_string_in_file-puede-duplicar-clase-si-el-texto-a-reemplazar-es-solo-el-importpaquete) | Tooling |
| 2026-03-23 | [keygo-ui — app unificada con roles en JWT (no tres portales separados)](#2026-03-23-keygo-ui--arquitectura-de-app-unificada-con-roles-en-jwt) | Arquitectura / Frontend |
| 2026-03-23 | [Manual frontend: flujo OAuth2 retorna code en JSON, no HTTP 302](#2026-03-23-manual-frontend-flujo-oauth2-retorna-code-en-json-no-http-302) | OAuth2 / Frontend |
| 2026-03-23 | [Nuevas variables de entorno deben documentarse en .env y ENVIRONMENT_SETUP.md](#2026-03-23-nuevas-variables-de-entorno-deben-documentarse-en-env-y-environment_setupmd) | Convenciones / Entorno |
| 2026-03-23 | [Registro con verificación email — ClientApp requiere campos obligatorios en tests](#2026-03-23-registro-con-verificación-email--clientapp-requiere-campos-obligatorios-en-tests) | Tests / Dominio |
| | 2026-03-23 | [Fase 8: client_credentials — sub=clientId, sin refresh_token ni id_token](#2026-03-23-fase-8-client_credentials--sub-clientid-sin-refresh_token-ni-id_token) | OAuth2 / M2M |
| 2026-03-22 | [Flyway: CREATE TABLE IF NOT EXISTS oculta errores de esquema incompleto](#2026-03-22-flyway-create-table-if-not-exists-oculta-errores-de-esquema-incompleto-de-ejecuciones-parciales) | Flyway / DB |
| 2026-03-22 | [Fase 7: SHA-256 como hash determinista para refresh tokens](#2026-03-22-fase-7-sha-256-como-hash-determinista-para-refresh-tokens) | Security / OAuth2 |
| 2026-03-22 | [Fase 7: Mockito UnnecessaryStubbing en tests de use cases complejos](#2026-03-22-fase-7-mockito-unnecessarystubbing-en-tests-de-use-cases-complejos) | Testing |
| 2026-03-22 | [Fase 7: BootstrapAdminKeyFilter — rutas userinfo y revoke como públicas](#2026-03-22-fase-7-bootstrapadminkeyfilter--rutas-userinfo-y-revoke-como-públicas) | Security / Filter |
| 2026-03-22 | [Fase 6: jacoco.skip en módulos que maduran de stub a activo](#2026-03-22-fase-6-eliminar-jacocoskip-cuando-un-módulo-stub-se-activa) | Maven / CI |
| 2026-03-22 | [Fase 6: tests de controllers con MockMvc standalone sin Spring context](#2026-03-22-fase-6-tests-de-controllers-oidcjwks-con-mockmvc-standalone) | Testing |
| 2026-03-22 | [Reorganización de docs AI a docs/ai/](#2026-03-22-reorganización-de-documentos-ai-a-docsai) | Proceso / Documentación |
| 2026-03-22 | [Conversión de diagramas ASCII a Mermaid — criterio de selección](#2026-03-22-conversión-de-diagramas-ascii-a-mermaid--criterio-de-selección) | Documentación / Diagramas |
| 2026-03-22 | [Corrección de inconsistencias: docs vs DB — criterio de decisión](#2026-03-22-corrección-de-inconsistencias-docs-vs-db--criterio-de-decisión) | Convenciones / DB |
| 2026-03-22 | [Docs de datos desincronizados con migraciones Flyway](#2026-03-22-documentación-de-datos-desincronizada-con-migraciones-flyway-reales) | Documentación |
| 2026-03-22 | [Value objects: acceso con records](#2026-03-22-value-objects-acceso-a-value-diferente-según-record-vs-clase-regular) | Java / Domain |
| 2026-03-22 | [Nimbus JOSE+JWT vía dependencia transitiva](#2026-03-22-nimbus-josejwt-en-spring-boot-4--dependencia-transitiva-vía-spring-security-oauth2-jose) | Maven / Deps |
| 2026-03-22 | [OIDC/JWKS retornar JSON nativo](#2026-03-22-endpoints-oidcjwks-deben-retornar-json-nativo-no-baseresponse) | API / OIDC |
| 2026-03-22 | [JwksBuilderPort fuera de keygo-api](#2026-03-22-jwksbuilderport--arquitectura-hexagonal-para-nimbus-fuera-de-keygo-api) | Arquitectura |
| 2026-03-22 | [OAuth2: estado entre /authorize y /login](#2026-03-22-oauth2-authorization-code-pasar-estado-entre-get-authorize-y-post-login-vía-http-session) | OAuth2 |
| 2026-03-21 | [JaCoCo en monorepo multi-módulo](#2026-03-21-configuración-de-jacoco-en-monorepo-maven-multi-módulo-con-spring-boot-4) | Maven / CI |
| 2026-03-21 | [Convenciones de coding Java](#2026-03-21-convenciones-de-coding-java-adoptadas-para-el-codebase) | Java / Style |
| 2026-03-21 | [jakarta.validation-api no transitivo](#2026-03-21-jakartavalidation-api-no-es-transitivo-en-keygo-api) | Maven / Deps |
| 2026-03-21 | [SpringDoc 3.0.1 con Spring Boot 4.x](#2026-03-21-springdoc-301-con-spring-boot-4x--integración-y-anotaciones-de-seguridad) | Spring / OpenAPI |
| 2026-03-21 | [Retroalimentación obligatoria de docs AI](#2026-03-17-retroalimentación-obligatoria-de-documentos-ai-tras-cada-tarea) | Proceso |
| 2026-03-21 | [Fase 0 cerrada: verificar sub-puntos](#2026-03-21-fase-0-cerrada-qué-faltaba-vs-qué-se-asumía-como-completo) | Proceso |
| 2026-03-21 | [Generación de colecciones Postman](#2026-03-21-generación-de-colecciones-postman-para-pruebas-funcionales-manuales) | Testing |
| 2026-03-21 | [Slug generado automáticamente](#2026-03-21-generación-automática-de-slug-a-partir-del-nombre-del-tenant) | Domain / API |
| 2026-03-21 | [Bug T-001 BootstrapAdminKeyFilter](#2026-03-21-bug-t-001--bootstrapadminkeyfilter-getrequesturi-vs-getservletpath-con-context-path) | Bug / Security |
| 2026-03-21 | [Reorganización de paquetes por feature](#2026-03-17-reorganización-de-paquetes-internos-por-feature-en-monorepo-multi-módulo) | Maven / Build |
| 2026-03-21 | [SupabaseJpaConfig basePackages](#2026-03-17-supabasejpaconfig-requiere-basepackages-ampliado-al-reorganizar-entidades-por-feature) | Spring / JPA |
| 2026-03-21 | [Script check-ai-docs.sh](#2026-03-17-script-de-verificación-de-actividad-del-agente-ai-extendido-a-agentsmd) | Tooling |
| 2026-03-21 | [Fase 2: import enum en use case](#2026-03-21-fase-2--clientapp-import-faltante-de-enum-en-use-case) | Java / Compiler |
| 2026-03-21 | [orphanRemoval en colecciones JPA](#2026-03-21-fase-2--patrón-de-orphanremoval-en-colecciones-jpa-de-entidades-hijo) | JPA / Hibernate |
| 2026-03-21 | [TenantEntity referencia no-managed](#2026-03-21-fase-2--tenantentity-como-referencia-no-managed-en-clientapprepositoryAdapter) | JPA |
| 2026-03-21 | [Mockito UnnecessaryStubbing con tryFindByEmail](#2026-03-21-fase-3--mockito-unnecessarystubbing-con-tryfindbyemail-que-captura-iae) | Testing |
| 2026-03-21 | [PasswordHash.toString() redactado](#2026-03-21-fase-3--passwordhashtostring-nunca-expone-el-hash-seguridad) | Security |
| 2026-03-21 | [Import correcto de BaseResponse](#2026-03-21-fase-4--import-correcto-de-baseresponse-en-controllers-rest) | Java / API |

---

## Lecciones

### [2026-03-26] Contraseñas de seed SQL deben documentarse junto al hash BCrypt
**Contexto:** Al revisar los usuarios de seed de V2 y V14 para pruebas del flujo OAuth2, se detectó que el hash BCrypt `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy` no coincide con ninguna contraseña común y no estaba documentado en ningún lugar del repositorio.
**Problema:** Un agente AI generó ese hash sin registrar el texto plano correspondiente, dejando los usuarios de seed inutilizables para pruebas hasta que se ejecute una migración correctiva.
**Solución / Buena práctica:** Siempre que se inserte un `password_hash` en una migración Flyway de seed, documentar la contraseña en texto plano de forma explícita:
1. Como comentario en el propio archivo SQL.
2. En la tabla de credenciales de `AGENTS.md` (sección "Seed credentials").
3. En `docs/data/MIGRATIONS.md` en la sección de esa migración.
Se creó `V15__reset_seed_user_passwords.sql` para corregir el hash con contraseñas conocidas y verificadas mediante `BCryptPasswordEncoder`.
**Archivos clave:** `keygo-supabase/src/main/resources/db/migration/V15__reset_seed_user_passwords.sql`, `docs/data/MIGRATIONS.md`, `AGENTS.md`

### [2026-03-26] ADR-001: documentar decisiones de error handling como ADR
**Contexto:** Se completó la clasificación de errores con `ErrorData` (`origin`, `clientRequestCause`, `clientMessage`) y se actualizaron los docs de flujo OAuth2 y la guía de frontend.
**Problema:** El estándar de manejo de errores quedaba documentado de forma dispersa entre `AUTH_FLOW.md`, `FRONTEND_DEVELOPER_GUIDE.md` y código Java, sin un documento canónico de decisión arquitectónica para onboarding de nuevos integradores o cambio de decisión futura.
**Solución / Buena práctica:** Crear `docs/keygo-ui/ADR-001-error-handling-oauth2.md` como ADR de una página que consolide: contexto, decisión, alternativas descartadas, consecuencias, mapa de errores por etapa OAuth2, árbol de decisión (Mermaid), contrato JSON + tipos TypeScript, helper de referencia y propuestas de evolución (T-064/T-065/T-066).
**Archivos clave:** `docs/keygo-ui/ADR-001-error-handling-oauth2.md`, `docs/api/AUTH_FLOW.md`, `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`

### [2026-03-26] Subclasificar `CLIENT_REQUEST` en `CLIENT_TECHNICAL` vs `USER_INPUT` mejora triage de UI y soporte
**Contexto:** Se necesitaba distinguir, dentro de errores de cliente, si la falla proviene de implementación UI/protocolo (cookies, payload, headers, sesión) o de datos capturados por el usuario.
**Problema:** Con solo `origin=CLIENT_REQUEST`, frontend y soporte seguían sin poder decidir rápidamente quién debía corregir: equipo UI o experiencia/formulario del usuario.
**Solución / Buena práctica:** Agregar `clientRequestCause` opcional en `ErrorData` con valores `CLIENT_TECHNICAL` y `USER_INPUT`, calculado en `ApiErrorDataFactory` usando `ResponseCode` y tipo de excepción (`InvalidCredentialsException`, `MethodArgumentNotValidException`, `MissingServletRequestParameterException`, `HttpMessageNotReadableException`).
**Archivos clave:** `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/ErrorData.java`, `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/ApiErrorDataFactory.java`, `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/GlobalExceptionHandler.java`

### [2026-03-26] En Spring Framework 7, `HttpMessageNotReadableException` en tests requiere `HttpInputMessage`
**Contexto:** Se agregó cobertura para payload JSON malformado en `GlobalExceptionHandlerTest`.
**Problema:** El test falló a compilación al usar `new HttpMessageNotReadableException("...")` porque en Spring Framework 7 no existe constructor de un solo parámetro.
**Solución / Buena práctica:** Construir la excepción con `MockHttpInputMessage`, por ejemplo `new HttpMessageNotReadableException("Malformed JSON", new MockHttpInputMessage(new byte[0]))`.
**Archivos clave:** `keygo-api/src/test/java/io/cmartinezs/keygo/api/error/GlobalExceptionHandlerTest.java`

### [2026-03-26] Clasificar errores por origen (`CLIENT_REQUEST`, `BUSINESS_RULE`, `SERVER_PROCESSING`) mejora diagnóstico frontend y soporte
**Contexto:** Se mejoró el contrato de errores para distinguir más claramente si la falla viene del request del cliente, de una regla de negocio o del procesamiento del servidor.
**Problema:** Con solo `code` y `clientMessage` era difícil para frontend y soporte decidir rápidamente si debían corregir payload, ajustar flujo funcional o escalar una incidencia backend.
**Solución / Buena práctica:** Agregar `origin` en `ErrorData` y calcularlo de forma centralizada en `ApiErrorDataFactory` a partir de `ResponseCode` para mantener consistencia entre `GlobalExceptionHandler` y `BootstrapAdminKeyFilter`.
**Archivos clave:** `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/ErrorData.java`, `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/ApiErrorDataFactory.java`, `keygo-run/src/main/java/io/cmartinezs/keygo/run/filter/BootstrapAdminKeyFilter.java`

### [2026-03-26] Errores API con `BaseResponse.data`: detalle tecnico en `local/dev`, mensaje amigable en otros perfiles
**Contexto:** Se ajusto el manejo global de errores y el `BootstrapAdminKeyFilter` para que siempre respondan `BaseResponse` con `failure` + `data`.
**Problema:** Los errores devolvian solo `failure` (sin `data`), lo cual complicaba diagnostico en desarrollo y dejaba respuestas poco orientadas al cliente en produccion.
**Solucion / Buena practica:** Centralizar la construccion de `data` de error con una factory (`ApiErrorDataFactory`) y conmutar por perfil (`local/dev`): en dev incluir `detail` y `exception`; en otros perfiles exponer solo `code` + `clientMessage` amigable. En tests con Mockito strict, usar `lenient()` para el stubbing por defecto que se sobreescribe en casos puntuales.
**Archivos clave:** `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/ApiErrorDataFactory.java`, `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/GlobalExceptionHandler.java`, `keygo-run/src/main/java/io/cmartinezs/keygo/run/filter/BootstrapAdminKeyFilter.java`, `keygo-api/src/test/java/io/cmartinezs/keygo/api/error/GlobalExceptionHandlerTest.java`

### [2026-03-26] ClientApp CONFIDENTIAL en tests de adapter/mapper debe incluir `hashedSecret`
**Contexto:** Se agregó `ClientAppRepositoryAdapterTest` para validar el fix de lazy loading en búsqueda de client apps usadas por `/oauth2/authorize`.
**Problema:** El test falló al mapear `ClientAppEntity` a dominio con `IllegalArgumentException: CONFIDENTIAL client apps must have a hashed secret` porque las entidades de prueba `CONFIDENTIAL` no incluían `hashedSecret`.
**Solución / Buena práctica:** En tests de adapters/mappers que construyen `ClientAppEntity` de tipo `CONFIDENTIAL`, setear siempre `hashedSecret` para respetar invariantes del dominio al ejecutar `ClientApp.builder().build()`.
**Archivos clave:** `keygo-supabase/src/test/java/io/cmartinezs/keygo/supabase/clientapp/adapter/ClientAppRepositoryAdapterTest.java`, `keygo-domain/src/main/java/io/cmartinezs/keygo/domain/clientapp/model/ClientApp.java`

### [2026-03-26] Missing query param en MockMvc standalone: sin handler específico cae en 500 (`OPERATION_FAILED`)
**Contexto:** Tests de `AuthorizationController` al migrar `/oauth2/authorize` a `@RequestParam` explícitos.
**Problema:** El caso de `response_type` faltante lanzó `MissingServletRequestParameterException`; en el `GlobalExceptionHandler` actual no hay handler dedicado para esa excepción, por lo que entra al catch-all y responde `500 OPERATION_FAILED`.
**Solución / Buena práctica:** En tests de controller standalone, al validar parámetros faltantes, alinear expectativas con el manejo real de excepciones configurado. Si se desea `400 INVALID_INPUT`, agregar un handler explícito para `MissingServletRequestParameterException`.
**Archivos clave:** `keygo-api/src/test/java/io/cmartinezs/keygo/api/auth/controller/AuthorizationControllerTest.java`, `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/GlobalExceptionHandler.java`

### [2026-03-26] OAuth2 authorize: para query params en snake_case usar `@RequestParam(name = ...)` en lugar de depender de `@ModelAttribute`
**Contexto:** Corrección del endpoint `GET /api/v1/tenants/{tenantSlug}/oauth2/authorize` tras recibir error de validación con `response_type=code`.
**Problema:** Con `@Valid @ModelAttribute AuthorizationRequest`, el parámetro RFC `response_type` no se enlazaba automáticamente al campo Java `responseType`; el backend terminaba evaluando `null` y lanzaba `response_type must be 'code' for this implementation`.
**Solución / Buena práctica:** Para query params OAuth2/OIDC con naming estándar en snake_case, declarar `@RequestParam(name = "...")` explícitos en el controller y mapearlos al request interno. Configuración de Jackson no aplica en este binding porque no interviene en `@ModelAttribute` de query string.
**Archivos clave:** `keygo-api/src/main/java/io/cmartinezs/keygo/api/auth/controller/AuthorizationController.java`, `keygo-api/src/test/java/io/cmartinezs/keygo/api/auth/controller/AuthorizationControllerTest.java`

### [2026-03-26] Vitest en ejemplos aislados: importar `describe/it/expect` explícitamente evita depender de globals
**Contexto:** Validación del ejemplo `examples/hosted-login-handoff/` agregado para implementar `T-056`.
**Problema:** Al ejecutar `vitest run`, ambos archivos de test fallaron con `ReferenceError: describe is not defined` porque el paquete de ejemplo no tenía habilitado `globals: true` y los tests asumían esa configuración implícita.
**Solución / Buena práctica:** En ejemplos aislados o paquetes pequeños, importar `describe`, `it` y `expect` directamente desde `vitest` hace los tests más portables y evita depender de configuración global adicional.
**Archivos clave:** `examples/hosted-login-handoff/tests/hostedLoginParams.test.ts`, `examples/hosted-login-handoff/tests/HostedLoginBoundary.test.tsx`

### [2026-03-26] Hosted login compartido: la UI central no debe apropiarse del contexto OAuth2 del tenant origen
**Contexto:** Actualización del flujo de autenticación y de la guía frontend para documentar cómo una app de otro tenant puede reutilizar el login visual de `keygo-ui`.
**Problema:** Es fácil confundir "usar la misma pantalla de login" con "autenticar siempre contra el tenant `keygo`" o con hacer que la UI central canjee y almacene tokens de una app ajena.
**Solución / Buena práctica:** En el patrón recomendado de hosted login, la UI central solo presta la experiencia visual y ejecuta `/oauth2/authorize` + `/account/login` con el contexto recibido. El `tenantSlug`, `client_id`, `redirect_uri`, `state`, `code_verifier` y el canje final en `/oauth2/token` deben seguir perteneciendo a la app origen.
**Archivos clave:** `docs/api/AUTH_FLOW.md`, `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`

### [2026-03-25] Mermaid en Markdown: evitar signos de interrogación invertidos en nodos validados por parser
**Contexto:** Validación de documentos AI tras registrar una inconsistencia documental de seguridad.
**Problema:** El validador reportó error en un bloque Mermaid de `docs/ai/inconsistencias.md` cuando un nodo usaba texto con signo de interrogación invertido (`¿Corregida?`).
**Solución / Buena práctica:** Cuando un Markdown sea validado por parser estricto, preferir labels Mermaid simples y ASCII-safe en los nodos (por ejemplo `Corregida?`) para evitar falsos errores de sintaxis.
**Archivos clave:** `docs/ai/inconsistencias.md`, `docs/ai/lecciones.md`

### [2026-03-25] Tests Maven por módulo en monorepo: usar `-am` para resolver dependencias de clases
**Contexto:** Validación de cambios de migración en `keygo-supabase` ejecutando tests del módulo de forma aislada.
**Problema:** `./mvnw -pl keygo-supabase test` falló con `NoClassDefFoundError` de clases en `keygo-app` y `keygo-domain` durante tests del adapter, porque no se habían construido dependencias reactor necesarias en esa ejecución.
**Solución / Buena práctica:** En monorepo Maven multi-módulo, para correr tests de un módulo que depende de artefactos locales, usar `-am` (`also-make`): `./mvnw -pl keygo-supabase -am test`. Esto compila módulos requeridos y evita errores de classpath.
**Archivos clave:** `keygo-supabase/pom.xml`, `docs/ai/lecciones.md`

### [2026-03-25] Bearer-only admin auth: `@PreAuthorize` + tenant match token/path
**Contexto:** Migración de seguridad para endpoints admin: remover `X-KEYGO-ADMIN` y usar solo `Authorization: Bearer`.
**Problema:** Validar JWT en el filtro no es suficiente para autorización fina; sin control por endpoint ni tenant se puede habilitar acceso cruzado entre tenants para usuarios con rol administrativo.
**Solución / Buena práctica:**
1. Mantener autenticación central en filtro (`BootstrapAdminKeyFilter`) para validar firma/expiración y publicar authorities desde claim `roles`.
2. Aplicar `@PreAuthorize` en cada controller admin para declarar explícitamente el rol requerido.
3. Usar evaluador SpEL (`tenantAuthorizationEvaluator`) para comparar `tenantSlug` del path con claim `tenant_slug` (fallback a `iss`) en tokens de `ADMIN_TENANT`.
4. Reservar bypass global por rol solo para `ADMIN` plataforma.
**Archivos clave:** `keygo-run/src/main/java/io/cmartinezs/keygo/run/filter/BootstrapAdminKeyFilter.java`, `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/security/SecurityConfig.java`, `keygo-api/src/main/java/io/cmartinezs/keygo/api/security/TenantAuthorizationEvaluator.java`

### [2026-03-25] Claims map puede ser inmutable en tests: copiar antes de agregar `tenant_slug`
**Contexto:** Al agregar claim `tenant_slug` en emisión de access tokens (`IssueTokensUseCase` y `RotateRefreshTokenUseCase`), algunos tests empezaron a fallar con `UnsupportedOperationException`.
**Problema:** En tests se mockeaba `TokenClaimsFactoryPort` con `Map.of(...)` (mapas inmutables). Al hacer `claims.put("tenant_slug", ...)` sobre ese mapa, la ejecución falla.
**Solución / Buena práctica:** Siempre copiar el resultado del factory a un mapa mutable (`new LinkedHashMap<>(...)`) antes de enriquecer claims adicionales en el caso de uso.
**Archivos clave:** `keygo-app/src/main/java/io/cmartinezs/keygo/app/auth/usecase/IssueTokensUseCase.java`, `keygo-app/src/main/java/io/cmartinezs/keygo/app/auth/usecase/RotateRefreshTokenUseCase.java`, `keygo-app/src/main/java/io/cmartinezs/keygo/app/auth/usecase/IssueClientCredentialsTokenUseCase.java`

### [2026-03-24] Endpoint `POST /roles`: evitar respuestas "exitosas" sin persistencia real
**Contexto:** Al preparar bootstrap de tenants/apps/usuarios para `keygo-ui`, se detectó que `POST /api/v1/tenants/{tenantSlug}/apps/{clientAppId}/roles` devolvía `201 ROLE_CREATED`, pero el rol no se guardaba en DB.
**Problema:** El controller construía el objeto de dominio en memoria y respondía éxito sin pasar por un use case ni por `AppRoleRepositoryPort.save()`. Esto rompe el contrato funcional y provoca fallos encadenados en memberships/scripts.
**Solución / Buena práctica:** Mover la creación a `CreateAppRoleUseCase` en `keygo-app`, usando command + validaciones (tenant activo, app perteneciente al tenant, código no duplicado) y persistencia vía puerto de salida. El controller debe orquestar request/response y nunca simular persistencia.
**Archivos clave:** `keygo-app/src/main/java/io/cmartinezs/keygo/app/membership/usecase/CreateAppRoleUseCase.java`, `keygo-api/src/main/java/io/cmartinezs/keygo/api/membership/controller/TenantAppRoleController.java`, `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/ApplicationConfig.java`

### [2026-03-24] Claim `roles` en JWT para que el frontend `keygo-ui` lea los roles directamente desde el JWT sin llamadas adicionales a la API.
**Problema:** El claim `roles` debe propagarse por toda la cadena de emisión de tokens: `MembershipRepositoryPort` → use case → `TokenClaimsFactoryPort` → `StandardTokenClaimsFactory`. Al agregar un parámetro a la interfaz, **todos** los callers deben actualizarse: `IssueTokensUseCase`, `RotateRefreshTokenUseCase`, `AuthorizationController` y también `IssueClientCredentialsTokenUseCase` (que pasa `null` porque M2M no tiene usuario ni membresía).
**Solución / Buena práctica:**
1. Agregar `findRoleCodesByUserAndClientApp(UUID userId, UUID clientAppId): List<String>` al port `MembershipRepositoryPort`.
2. Usar `@Query(nativeQuery = true)` en `MembershipJpaRepository` para el JOIN de 3 tablas (`app_roles` ← `membership_roles` ← `memberships`) filtrando por `status = 'ACTIVE'` — evita múltiples roundtrips.
3. Propagar `List<String> roles` como parámetro explícito en **ambas** firmas de `TokenClaimsFactoryPort`.
4. Para M2M (`client_credentials`), pasar `null` — la factory omite el claim si la lista es null o vacía.
5. Al agregar `MembershipRepositoryPort` como dependencia de `RotateRefreshTokenUseCase`, actualizar el bean en `ApplicationConfig` con el nuevo parámetro en el orden correcto del constructor.
6. Los tests de `IssueTokensUseCaseTest`, `RotateRefreshTokenUseCaseTest` y `StandardTokenClaimsFactoryTest` deben actualizarse para reflejar las nuevas firmas.
**Archivos clave:** `keygo-app/src/main/java/.../app/auth/port/TokenClaimsFactoryPort.java`, `keygo-app/src/main/java/.../app/membership/port/MembershipRepositoryPort.java`, `keygo-infra/src/main/java/.../infra/auth/jwt/StandardTokenClaimsFactory.java`, `keygo-supabase/src/main/java/.../supabase/membership/repository/MembershipJpaRepository.java`, `keygo-run/src/main/java/.../run/config/ApplicationConfig.java`

### [2026-03-24] JWT admin en filtro: rutas OAuth2 públicas + Bearer con rol admin
**Contexto:** Implementar el ítem "Los endpoints admin aún no validan JWT" para habilitar el frontend `keygo-ui` a llamar endpoints protegidos con su Bearer token (obtenido tras login OAuth2).
**Problema:** Dos issues combinados: (1) las rutas OAuth2 del flujo de autorización (`/oauth2/authorize`, `/account/login`, `/oauth2/token`) estaban protegidas por `X-KEYGO-ADMIN`, creando un círculo vicioso — el frontend no puede obtener un JWT sin autenticarse primero. (2) el filtro solo aceptaba `X-KEYGO-ADMIN` pero no Bearer JWT.
**Solución / Buena práctica:**
1. Agregar sufijos públicos para las rutas OAuth2: `authorizePathSuffix`, `loginPathSuffix`, `tokenPathSuffix` en `KeyGoBootstrapProperties` + `application.yml`.
2. Agregar dos campos opcionales al filtro con `@Autowired(required = false)` (package-private para permitir inyección en tests del mismo paquete): `AccessTokenVerifierPort` y `SigningKeyRepositoryPort`.
3. El nuevo método `validateBearerAdminToken()` usa `signingKeyRepository.findPublishableKeys()` + `accessTokenVerifier.verify()` y luego comprueba el claim `roles` contra `bootstrapProperties.getAdminRoles()`.
4. El método `validateAuthentication()` prueba primero `X-KEYGO-ADMIN` (si está presente), luego Bearer JWT. Si ninguno está disponible, retorna false.
5. Cuando se usa `replace_string_in_file` sobre archivos largos con coincidencia de texto corta, el tool puede DUPLICAR el archivo. La solución segura es usar `create_file` para archivos nuevos o `head -N > tmp && mv tmp original` para truncar duplicaciones.
**Archivos clave:** `keygo-run/.../filter/BootstrapAdminKeyFilter.java`, `keygo-run/.../properties/KeyGoBootstrapProperties.java`, `keygo-run/src/main/resources/application.yml`

### [2026-03-24] SigningKeyInitializer: auto-generar clave RSA en startup
**Contexto:** El frontend necesita un signing key RSA activo para que el flujo OAuth2 emita JWTs. Sin una clave activa, el endpoint de token falla con `NoActiveSigningKeyException`.
**Problema:** No había mecanismo automático para generar la primera clave RSA al iniciar el servidor en un entorno nuevo (DB vacía). Los desarrolladores debían insertar la clave manualmente o via script SQL.
**Solución / Buena práctica:** Crear `SigningKeyInitializer implements ApplicationRunner` en `keygo-run`, anotado con `@Profile("supabase")` para que solo se active cuando hay DB. En `run()`: si no hay clave ACTIVE, genera un par RSA-2048 usando `KeyPairGenerator`, codifica a PEM con `Base64.getMimeEncoder(64, ...)` y persiste via `SigningKeyRepositoryPort.save()`. Esto es idempotente: en re-inicios con clave existente, el método es un no-op.
**Archivos clave:** `keygo-run/src/main/java/.../run/startup/SigningKeyInitializer.java`

### [2026-03-24] replace_string_in_file puede duplicar clase si el texto a reemplazar es solo el import/paquete
**Contexto:** Al actualizar `UserPersistenceMapper.java` para agregar campos OIDC extendidos, se usó `replace_string_in_file` con solo la sección de imports como `oldString`, reemplazándola por el archivo completo nuevo.
**Problema:** El tool concatenó el nuevo contenido al principio del string coincidente pero dejó el resto del contenido original intacto. El resultado fue que el archivo tenía dos declaraciones `public class UserPersistenceMapper` — error `duplicate class` en compilación.
**Solución / Buena práctica:** Cuando se quiere reescribir un archivo Java completamente, usar el comando shell `cat > file << 'EOF' ... EOF` en lugar de `replace_string_in_file`. Alternativamente, si se usa `replace_string_in_file` para reemplazar por contenido completo, el `oldString` debe incluir la clase completa (package + imports + class body) para que el reemplazo sea total.
**Archivos clave:** `keygo-supabase/src/main/java/io/cmartinezs/keygo/supabase/user/mapper/UserPersistenceMapper.java`

### [2026-03-24] Al extender un record Java (command/request/result), actualizar todos los sitios de construcción del record
**Contexto:** Se extendió `UpdateUserCommand` de 4 a 10 parámetros para soportar campos OIDC extendidos del perfil de usuario.
**Problema:** Los tests existentes que usaban `new UpdateUserCommand(slug, id, "Jane", "Smith")` fallaron al compilar porque el constructor del record cambió.
**Solución / Buena práctica:** Tras cambiar la firma de un record, buscar con grep todos los `new UpdateUserCommand(` / `new UpdateUserRequest(` en el código de producción y tests, y actualizar los constructores con los nuevos parámetros (pasar `null` para opcionales). Hacer esto antes de compilar evita el ciclo error-corrección.
**Archivos clave:** `keygo-app/src/test/java/.../UpdateResetValidateUseCaseTest.java`, `keygo-api/src/test/java/.../TenantUserControllerTest.java`

### [2026-03-24] Diseño de perfil de usuario en IAM: perfil canónico en tenant_users, metadata app en membership_attributes
**Contexto:** Decisión de diseño sobre si el perfil de usuario debía vivir en `tenant_users` (nivel tenant) o en `memberships` (nivel app).
**Problema:** La pregunta era válida ya que cada app podría querer campos diferentes del usuario.
**Solución / Buena práctica:** Adoptar el modelo en dos capas que siguen Auth0, Keycloak y OIDC §5.3:
  - **Capa 1 (V13):** perfil canónico OIDC en `tenant_users` — los 6 claims estándar (`phone_number`, `locale`, `zoneinfo`, `profile_picture_url`, `birthdate`, `website`) viven aquí porque son del usuario, no de la app.
  - **Capa 2 (pendiente V14):** metadata app-específica en `membership_attributes` — pares clave-valor para datos que varían por app.
  La regla de oro: si el dato respondería a "¿quién eres?" → perfil canónico. Si responde a "¿qué eres en esta app?" → membership_attributes.
**Archivos clave:** `docs/data/MIGRATIONS.md`, `V13__extend_tenant_user_profile.sql`, `keygo-domain/src/main/java/.../user/model/User.java`



### [2026-03-23] Manual frontend: flujo OAuth2 retorna code en JSON, no HTTP 302

**Contexto:** Al crear el manual de desarrollador frontend (`docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`), se revisó el comportamiento actual del endpoint `POST /account/login`.

**Problema:** El estándar OAuth2 (RFC 6749) define que el authorization code se entrega al cliente mediante un redirect HTTP 302 hacia `redirect_uri?code=...&state=...`. El backend de KeyGo actualmente devuelve el código directamente en el JSON de la respuesta (`data.code`) sin hacer el redirect. Esto es una desviación del estándar que debe ser transparente para el desarrollador frontend.

**Solución / Buena práctica:** El frontend debe:
1. Leer el `code` del JSON de la respuesta del `POST /account/login`.
2. Construir manualmente la URL del callback y navegar a ella (`window.location.href`).
3. Diseñar el `CallbackPage` para que pueda procesar el código tanto desde query params (cuando el backend implemente el redirect 302 real) como desde el estado de navegación (para compatibilidad futura).

El manual documenta ambos comportamientos con notas explícitas. Ver `AGENTS.md` → tabla de fases (`POST /account/login` sección "Fase 7 planificada").

**Archivos clave:** `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`, `docs/api/AUTH_FLOW.md` sección "Paso 2".

---

### [2026-03-23] keygo-ui — arquitectura de app unificada con roles en JWT

**Contexto:** Se revisó y corrigió el diseño del manual de frontend. La primera versión planteaba tres portales separados (tres apps React), lo cual es un antipatrón para este tipo de sistema IAM.

**Problema:** Diseñar portales separados implica múltiples apps registradas en el sistema, múltiples flujos de autenticación diferenciados y duplicación de código. Además, los admins siguen siendo usuarios del sistema y deben autenticarse igual que cualquier otro usuario.

**Solución / Buena práctica:**
- **Una sola app React** (`keygo-ui`) registrada como `ClientApp` en el tenant `keygo` (tenant raíz).
- **Un solo flujo OAuth2/PKCE** para todos los usuarios, sin importar su rol.
- Los roles (`ADMIN`, `ADMIN_TENANT`, `USER_TENANT`) se determinan por los claims del JWT.
- El routing y las vistas se adaptan al rol con `<RoleGuard>` y `useHasRole()`.
- El `ADMIN_TENANT` necesita el claim `managed_tenant` en el JWT para saber qué tenant gestiona — este claim es **pendiente de implementación en backend**.
- Mientras el backend no emita roles en el JWT, simularlos con `VITE_MOCK_ROLE` y MSW en desarrollo.

**Archivos clave:** `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`, `src/auth/roleGuard.tsx`, `src/auth/jwksVerify.ts`.

---

### [2026-03-23] Nuevas variables de entorno deben documentarse en .env y ENVIRONMENT_SETUP.md

**Contexto:** Al agregar `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `KEYGO_MAIL_FROM` y `KEYGO_MAIL_APP_NAME` para el flujo de registro con verificación de email, se detectó que solo se habían declarado en `application.yml` pero no en los archivos `.env` ni en la documentación de entornos.

**Problema:** Las variables de entorno nuevas declaradas en `application.yml` con sintaxis `${VAR:default}` no se propagan automáticamente a los archivos `.env*` ni a la documentación. El desarrollador que active un nuevo ambiente no sabrá qué variables configurar.

**Solución / Buena práctica:** Ante **cualquier** variable de entorno nueva en `application.yml`, actualizar obligatoriamente:
1. `keygo-supabase/.env.example` — agregar la variable con valor de ejemplo y comentario explicativo
2. `keygo-supabase/.env-local` — valor apropiado para desarrollo local (ej: MailHog sin autenticación)
3. `keygo-supabase/.env-desa` — valor para sandbox/staging (ej: Mailtrap)
4. `keygo-supabase/.env-prod` — valor placeholder con comentario de dónde obtenerlo
5. `keygo-supabase/.env` — mismo valor que `.env-desa` (es el ambiente activo)
6. `docs/development/ENVIRONMENT_SETUP.md` — agregar a la tabla completa y, si aplica, al bloque `application.yml`
7. `scripts/quick-start.sh` — si la variable es usada por el flujo de desarrollo

**Regla de oro:** Si aparece en `application.yml` como `${VARIABLE:default}`, debe aparecer en todos los `.env*`.

**Archivos clave:** `keygo-supabase/.env.example`, `docs/development/ENVIRONMENT_SETUP.md`, `scripts/quick-start.sh`

---

### [2026-03-23] Registro con verificación email — ClientApp requiere campos obligatorios en tests

**Contexto:** Implementación del flujo de registro de usuarios con verificación por email (Fase 9). Tests unitarios para `RegisterTenantUserUseCase`, `VerifyEmailUseCase` y `ResendVerificationEmailUseCase`.

**Problema:** Al construir un objeto `ClientApp` en los tests usando el builder sin todos los campos requeridos (`id`, `type`, `status`, `accessPolicy`), se obtenía `IllegalArgumentException: ClientApp id cannot be null` antes de que los tests pudieran ejecutarse. El dominio es estricto en validaciones en el constructor.

**Solución / Buena práctica:** Siempre construir objetos de dominio con todos sus campos requeridos en los tests, aunque el objeto solo se use como valor de retorno de un mock. Para `ClientApp`: se requieren `id (ClientAppId.generate())`, `type (ClientType.PUBLIC)`, `status (ClientAppStatus.ACTIVE)`, y `accessPolicy (new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))`.

**Archivos clave:** `keygo-domain/src/main/java/.../domain/clientapp/model/ClientApp.java` (líneas 44-52 — validaciones del constructor)

### [2026-03-23] Fase 8: client_credentials — sub=clientId, sin refresh_token ni id_token
**Contexto:** Implementación del grant `client_credentials` (Fase 8 — M2M) en `IssueClientCredentialsTokenUseCase` y rama correspondiente en `AuthorizationController`.
**Problema:** El grant `client_credentials` no representa a un usuario final, lo que implica diferencias importantes respecto a `authorization_code`:
- El `sub` del access token debe ser el `client_id` (string), **no** un UUID de usuario.
- **No se emite** `id_token` (OIDC es para usuarios; M2M no tiene identidad de usuario).
- **No se emite** `refresh_token` (las apps M2M pueden solicitar un token nuevo directamente).
- Solo apps de tipo `CONFIDENTIAL` pueden usar este grant (las PUBLIC no tienen secret).
- El campo `client_secret` es obligatorio en el request.
**Solución / Buena práctica:**
- En `TokenData`, los campos `idToken` y `refreshToken` se excluyen automáticamente por la config `NON_NULL` de Jackson — no hace falta serialización especial.
- La resolución de scopes M2M es diferente: si no se solicitan scopes específicos, se retornan **todos los permitidos**; si se solicitan, se filtra la intersección con los permitidos.
- El `aud` del token M2M se establece igual al `sub` (`clientId`), diferente al flujo user donde `aud = clientId` pero `sub = userId`.
**Archivos clave:** `IssueClientCredentialsTokenUseCase.java`, `AuthorizationController.java` (método `handleClientCredentialsGrant`), `IssueClientCredentialsTokenCommand.java`, `IssueClientCredentialsTokenResult.java`

---

### [2026-03-22] Fase 6: eliminar jacoco.skip cuando un módulo stub se activa
**Contexto:** `keygo-infra` tenía `<jacoco.skip>true</jacoco.skip>` porque era un módulo "stub vacío". Al implementar la Fase 6 se llenó con código de producción (`RsaJwtTokenSigner`, `JwkSetBuilder`, `StandardTokenClaimsFactory`) y sus tests unitarios.
**Problema:** Si `jacoco.skip=true` queda activo tras activar un módulo, la cobertura de ese código nunca se mide ni reporta, generando un punto ciego en el quality gate.
**Solución / Buena práctica:** Al escribir el primer código productivo en un módulo anteriormente stub, remover `<jacoco.skip>true</jacoco.skip>` en el mismo commit. Un módulo con tests reales no debería tener skip de cobertura.
**Archivos clave:** `keygo-infra/pom.xml`

---

### [2026-03-22] Fase 6: tests de controllers OIDC/JWKS con MockMvc standalone
**Contexto:** `JwksController` y `OidcMetadataController` retornan `Map<String, Object>` (JSON nativo RFC 7517 / OIDC Discovery 1.0) en lugar del envelope `BaseResponse<T>`.
**Problema:** Los controllers no podían testearse con el patrón habitual de `BaseResponse`. Además, `@WebMvcTest` cargaría el contexto Spring completo con beans que no están en `keygo-api`.
**Solución / Buena práctica:** Usar `MockMvcBuilders.standaloneSetup(controller).build()` con `@ExtendWith(MockitoExtension.class)` (sin Spring). Verificar JSON nativo directamente con `jsonPath("$.keys")`, `jsonPath("$.issuer")`, etc. — no buscar envelope. El `setUp()` se llama al inicio de cada test en lugar de `@BeforeEach` para evitar posibles interferencias entre tests.
**Archivos clave:** `keygo-api/src/test/java/.../auth/controller/JwksControllerTest.java`, `OidcMetadataControllerTest.java`

---

### [2026-03-22] Reorganización de documentos AI a docs/ai/
**Contexto:** Limpieza de la raíz del repositorio — los sub-documentos AI estaban directamente en la raíz, mezclados con documentos de producto.
**Problema:** Los archivos `AI_CONTEXT.lecciones.md`, `AI_CONTEXT.propuestas.md`, `AGENTS.registro.md`, `INCONSISTENCIAS.md` e `INCONSISTENCIAS.datos.md` en la raíz dificultaban la navegación. La raíz debería tener solo la info general y los enlaces al detalle.
**Solución / Buena práctica:** Crear `docs/ai/` como categoría específica para documentación de agentes. Los documentos raíz AI (`AI_CONTEXT.md`, `AGENTS.md`) son resúmenes con info general y enlaces a `docs/ai/`. Los sub-documentos detallados viven en `docs/ai/`. El script `check-ai-docs.sh` actualizado apunta a los nuevos paths. Las tablas de referencia en `CLAUDE.md` y `.github/copilot-instructions.md` también actualizadas.
**Archivos clave:** `docs/ai/`, `AI_CONTEXT.md`, `AGENTS.md`, `CLAUDE.md`, `.github/copilot-instructions.md`, `scripts/check-ai-docs.sh`

---

### [2026-03-22] Corrección de inconsistencias docs vs DB — criterio de decisión
**Contexto:** Re-auditoría de las 12 inconsistencias detectadas en `inconsistencias-datos.md` y marcadas como "resueltas". El usuario solicitó revisar si las correcciones fueron suficientes o si la DB debía ajustarse también.
**Problema:** La corrección anterior actualizó los documentos para reflejar lo que había en la DB (tablas en singular: `app_role`, `membership`, `membership_role`). Pero la convención estándar PostgreSQL exige nombres en plural, y la documentación original sí los tenía en plural. Al corregir solo los docs, se perpetuó una inconsistencia real en el schema.
**Solución / Buena práctica:**
Al revisar una inconsistencia entre docs y código/DB, aplicar este criterio:
1. **La documentación manda en convenciones de nomenclatura** (singular/plural, casing, patrones de nombres). Si la doc dice plural, la DB debe ser plural → crear migración.
2. **La implementación manda cuando hay razón técnica clara** (normalización, columnas redundantes, estándares RFC, seguridad). Si la implementación omitió `tenant_id` en `membership` porque sería redundante, es la implementación la correcta.
3. **Ambos pueden tener razón parcial** → aplicar el criterio de menor impacto y mayor consistencia con el sistema.
4. **Corregir la documentación** para reflejar el criterio aplicado (no simplemente para aceptar la implementación si esta está mal).
5. **Nunca marcar como "corregido" una inconsistencia donde solo se ajustó la documentación para aceptar algo que viola convenciones**. Agregar una nota de "pendiente de migración" en ese caso.

**Archivos clave:** `docs/ai/inconsistencias-datos.md`, `V10__rename_membership_tables_to_plural.sql`, `AppRoleEntity.java`, `MembershipEntity.java`

---

### [2026-03-22] Documentación de datos desincronizada con migraciones Flyway reales
**Contexto:** Actualización explícita de `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md`, `DATA_DICTIONARY.md` y `AUTH_FLOW.md`. Se leyeron las migraciones SQL reales (V1–V9) y se compararon con los documentos existentes.
**Problema:** Múltiples discrepancias críticas. Ver detalle completo en [`inconsistencias-datos.md`](inconsistencias-datos.md).
**Solución / Buena práctica:** Al generar documentación de datos, **siempre leer las migraciones SQL reales** antes de escribir el diccionario. No asumir columnas ni tipos — verificar cada campo en `V{n}__*.sql`. **Regla obligatoria:** al crear cualquier migración Flyway nueva, actualizar `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md` y `DATA_DICTIONARY.md` antes de cerrar la tarea.
**Archivos clave:** `docs/data/DATA_MODEL.md`, `docs/ai/inconsistencias-datos.md`, `keygo-supabase/src/main/resources/db/migration/V1–V9`

---

### [2026-03-22] Value objects: acceso a `.value()` diferente según record vs clase regular
**Contexto:** Fase 5 — modelos de dominio para OAuth2 (`AuthorizationCode`, `CodeChallenge`, `ScopeSet`).
**Problema:** Records Java exponen cada parámetro de constructor como método público automáticamente:
- `record AuthorizationCodeId(UUID id)` → acceso `.id()` (no `.getValue()`)
- `record ClientId(String value)` → acceso `.value()`
- `record UserId(UUID value)` → acceso `.value()`
**Solución / Buena práctica:** Todos los value objects del proyecto son records. Nombrar el parámetro de forma descriptiva (`id`, `value`, `email`…). No usar `.getValue()` — usar el nombre exacto del parámetro.
**Archivos clave:** `keygo-domain/.../model/*Id.java`, `AuthorizationCodeId.java`

---

### [2026-03-22] Nimbus JOSE+JWT en Spring Boot 4 — dependencia transitiva vía spring-security-oauth2-jose
**Contexto:** Fase 6 — implementación de firma JWT con RSA en `keygo-infra`.
**Problema:** `com.nimbusds:nimbus-jose-jwt` directo en `keygo-infra/pom.xml` sin versión → Maven falla: "version is missing". Spring Boot 4.x no expone Nimbus en su BOM directamente.
**Solución / Buena práctica:** Usar `org.springframework.security:spring-security-oauth2-jose` (sin versión) como dependencia. Nimbus llega transitivamente con versión compatible garantizada.
**Archivos clave:** `keygo-infra/pom.xml`

---

### [2026-03-22] Endpoints OIDC/JWKS deben retornar JSON nativo, no BaseResponse
**Contexto:** Fase 6 — `JwksController` y `OidcMetadataController`.
**Problema:** Las librerías OAuth2 (Spring Security Resource Server, Auth0 SDK, etc.) consumen `/.well-known/jwks.json` y esperan JSON nativo RFC 7517 — sin envelope `BaseResponse`.
**Solución / Buena práctica:** Controllers OIDC/JWKS retornan `ResponseEntity<Map<String, Object>>`. El resto sigue con `BaseResponse<T>`. Paths `/.well-known` configurados como públicos en `BootstrapAdminKeyFilter`.
**Archivos clave:** `keygo-api/.../JwksController.java`, `keygo-api/.../OidcMetadataController.java`

---

### [2026-03-22] JwksBuilderPort — arquitectura hexagonal para Nimbus fuera de keygo-api
**Contexto:** Fase 6 — `JwkSetBuilder` (Nimbus) no puede ir en `keygo-api` porque `keygo-api` no depende de `keygo-infra`.
**Problema:** Importar implementación Nimbus desde `keygo-api` viola hexagonal — el controller no debe conocer implementaciones concretas.
**Solución / Buena práctica:** Definir `JwksBuilderPort` en `keygo-app` (interface). `JwkSetBuilder` en `keygo-infra` implementa el puerto. El wiring en `ApplicationConfig` (`keygo-run`).
**Archivos clave:** `keygo-app/.../JwksBuilderPort.java`, `keygo-infra/.../JwkSetBuilder.java`

---

### [2026-03-22] OAuth2 Authorization Code: pasar estado entre GET /authorize y POST /login vía HTTP Session
**Contexto:** Fase 5 — flujo OAuth2, corrección de `/account/login` para emitir el authorization code.
**Problema:** El estado del `GET /authorize` (clientId, redirectUri, scope, codeChallenge) debe estar disponible cuando llega el `POST /login`. Sin HTTP Session, cada request llega sin contexto.
**Solución / Buena práctica:**
1. `GET /authorize` guarda `AuthorizationSessionState` en `HttpSession` (`JSESSIONID` cookie).
2. `POST /login` (misma sesión) recupera el estado, autentica usuario y emite authorization code.
3. El cliente **debe** enviar cookies entre los dos pasos (`credentials: 'include'` en fetch / `CookieJar` en OkHttp).
**Archivos clave:** `AuthorizationController.java`, `AuthorizationSessionState.java`, `LoginData.java`

---

### [2026-03-21] SpringDoc 3.0.1 con Spring Boot 4.x — integración y anotaciones de seguridad
**Contexto:** Integración de Swagger/OpenAPI.
**Problema:** (1) `@SecurityRequirementsOptional` no existe en `swagger-annotations-jakarta`. (2) SpringDoc 2.x es para Spring Boot 3.x; se necesita 3.x para Spring Boot 4.x.
**Solución / Buena práctica:** Usar `springdoc-openapi-starter-webmvc-ui:3.0.1`. Para endpoints públicos, no usar anotación de seguridad (no SecurityRequirement global). Para protegidos: `@SecurityRequirement(name = "AdminKeyAuth")` a nivel de clase/método.
**Archivos clave:** `keygo-run/config/OpenApiConfig.java`, `keygo-api/pom.xml`, `application.yml`

---

### [2026-03-21] Configuración de JaCoCo en monorepo Maven multi-módulo con Spring Boot 4
**Contexto:** Propuesta T-016 — JaCoCo con umbral mínimo y reporte consolidado.
**Problema:** (1) Módulos sin código fallan el check si no se excluyen. (2) `keygo-bom` (packaging `pom`) ejecuta JaCoCo sin código. (3) `report-aggregate` requiere que el módulo ejecutor tenga todos los módulos como dependencias.
**Solución / Buena práctica:**
- `prepare-agent` + `report` + `check` en `pluginManagement` del POM raíz.
- Módulos stub vacíos: `<jacoco.skip>true</jacoco.skip>`.
- `report-aggregate` solo en `keygo-run`.
- `<jacoco.minimum.coverage>` como propiedad parametrizable.
- En CI: `./mvnw verify` (no `test`) para ejecutar fases de JaCoCo.
- Versión recomendada: `0.8.12`.
**Archivos clave:** `pom.xml` (raíz), `keygo-run/pom.xml`, `.github/workflows/ci.yml`

---

### [2026-03-21] Convenciones de coding Java adoptadas para el codebase
**Contexto:** Revisión masiva de estilo en todos los módulos activos.
**Problema:** (1) Líneas en blanco en JavaDoc. (2) `/** */` en campos en vez de `/* */`. (3) `@Data` en entidades JPA. (4) Literales string duplicados en tests. (5) `@Override toString()` sin `@NotNull`.
**Solución / Buena práctica:**
- JavaDoc: usar `<p>` para separar párrafos; no líneas en blanco antes de `@param`/`@return`.
- Campos: `/* */`, nunca `/** */`.
- Entidades JPA: `@Getter @Setter` (no `@Data`). Evitar `equals`/`hashCode` sobre colecciones lazy.
- Tests: constantes `private static final` para literales repetidos.
- `toString()` override: `@SuppressWarnings("NullableProblems")`.
- Domain: Lombok `provided`.
**Archivos clave:** `keygo-domain/pom.xml`, `Tenant.java`, `TenantEntity.java`, `UserEntity.java`

---

### [2026-03-21] jakarta.validation-api no es transitivo en keygo-api
**Contexto:** Fase 1 — DTOs con `@NotBlank`, `@Size`, `@Valid` en `keygo-api`.
**Problema:** Build falla con "package jakarta.validation.constraints does not exist". `spring-boot-starter-web` no expone `jakarta.validation-api` transitivamente en Spring Boot 4.
**Solución / Buena práctica:** Agregar `jakarta.validation-api` explícitamente en `keygo-api/pom.xml`. Aplica a cualquier módulo que declare DTOs con anotaciones de validación.
**Archivos clave:** `keygo-api/pom.xml`, `CreateTenantRequest.java`

---

### [2026-03-21] Fase 0 cerrada: qué faltaba vs. qué se asumía como completo
**Contexto:** Verificación del estado real de Fase 0 del plan de implementación.
**Problema:** La reorganización de paquetes (0.2) se marcó como Fase 0 completa, pero faltaban: (a) pipeline CI; (b) enforcement lint; (c) convenciones de código documentadas.
**Solución / Buena práctica:** Al marcar una fase como completa, verificar **cada sub-punto** de la lista. No marcar ✅ solo por el trabajo más visible.
**Archivos clave:** `.github/workflows/ci.yml`, `pom.xml` (Maven Enforcer), `docs/development/CODE_STYLE.md`

---

### [2026-03-21] Generación de colecciones Postman para pruebas funcionales manuales
**Contexto:** El proyecto carecía de colecciones Postman importables.
**Problema:** Sin colecciones estándar, cada prueba requería configuración manual.
**Solución / Buena práctica:** Crear archivos bajo `postman/` (schema v2.1.0). Puntos clave: (1) auth `apikey` a nivel de colección heredada; (2) `{{fullBaseUrl}}` compuesto por pre-request script global; (3) slug único con timestamp en pre-request de Create Tenant; (4) guardar `tenantSlug` en entorno post-request; (5) endpoints públicos overridean auth a `noauth`.
**Archivos clave:** `docs/postman/KeyGo-Server.postman_collection.json`, `docs/postman/KeyGo-Server-Local.postman_environment.json`

---

### [2026-03-21] Generación automática de slug a partir del nombre del tenant
**Contexto:** `slug` era requerido en request body — se migró a generación automática.
**Problema:** Slug editable en API → validaciones redundantes + riesgo de slugs inconsistentes.
**Solución / Buena práctica:** `SlugUtils.toSlug(String)` en `keygo-domain/shared/util/` (Java puro). `TenantSlug.fromName(String)` genera y valida. Postman: timestamp en `name`, no en `slug`.
**Archivos clave:** `SlugUtils.java`, `TenantSlug.java`, `CreateTenantCommand.java`, `CreateTenantUseCase.java`

---

### [2026-03-21] Bug T-001 — BootstrapAdminKeyFilter: getRequestURI() vs. getServletPath() con context-path
**Contexto:** Corrección del bug donde el filtro de seguridad nunca bloqueaba rutas de API.
**Problema:** `getRequestURI()` incluye el context-path (`/keygo-server/api/...`) → nunca coincide con prefijos configurados (`/api/`).
**Solución / Buena práctica:** Usar `request.getServletPath()` — retorna ruta relativa al context-path (`/api/v1/...`). En tests: usar `setServletPath()` (no `setRequestURI()`). Agregar 2 tests de regresión con `setContextPath` + `setServletPath`.
**Archivos clave:** `BootstrapAdminKeyFilter.java`, `BootstrapAdminKeyFilterTest.java`

---

### [2026-03-17] Retroalimentación obligatoria de documentos AI tras cada tarea
**Contexto:** Revisión de documentos de guía para agentes.
**Problema:** `AGENTS.md` no estaba en la lista de lectura obligatoria. No había instrucciones explícitas sobre cuándo actualizar estos docs.
**Solución / Buena práctica:** Los docs AI (`AI_CONTEXT.md`, `AGENTS.md`) son base de conocimiento — actualizar siempre al concluir tarea donde haya error resuelto, mejor patrón, cambio tecnológico o nueva convención.
**Archivos clave:** `AI_CONTEXT.md`, `AGENTS.md`, `CLAUDE.md`, `.github/copilot-instructions.md`

---

### [2026-03-17] Reorganización de paquetes internos por feature en monorepo multi-módulo
**Contexto:** Reorganización de paquetes de organización técnica a organización por feature.
**Problema:** `./mvnw -pl keygo-run test` usaba JARs viejos → errores "cannot find symbol".
**Solución / Buena práctica:** Al reorganizar paquetes en módulos dependidos, ejecutar primero `./mvnw -pl <modulos> install -DskipTests`. Solo el build completo (`./mvnw clean package`) garantiza orden correcto.
**Archivos clave:** `keygo-app/platform/port`, `keygo-api/shared`, `keygo-supabase/config/SupabaseJpaConfig.java`

---

### [2026-03-17] SupabaseJpaConfig requiere basePackages ampliado al reorganizar entidades por feature
**Contexto:** Reorganización de entidades JPA a sub-paquetes por feature.
**Problema:** `@EntityScan` y `@EnableJpaRepositories` con rutas exactas fallan al reorganizar.
**Solución / Buena práctica:** Usar el paquete raíz: `"io.cmartinezs.keygo.supabase"`. Spring Data escanea recursivamente. Cambiar en el mismo commit que la reorganización.
**Archivos clave:** `SupabaseJpaConfig.java`

---

### [2026-03-17] Script de verificación de actividad del agente AI (extendido a AGENTS.md)
**Contexto:** Extensión de `scripts/check-ai-docs.sh`.
**Problema:** Solo verificaba `AI_CONTEXT.md`. `AGENTS.md` podía quedar desactualizado sin detectarse.
**Solución / Buena práctica:** Función reutilizable `check_section(FILE, SECTION_LABEL)` con arrays globales. Compatibilidad GNU/BSD date. Exit code = peor de los dos docs. Tras reorganización a `docs/ai/`, el script verifica `docs/ai/lecciones.md` y `docs/ai/agents-registro.md`.
**Archivos clave:** `scripts/check-ai-docs.sh`

---

### [2026-03-21] Fase 2 — ClientApp: import faltante de enum en use case
**Contexto:** `CreateClientAppUseCase` referencia `ClientType.CONFIDENTIAL`.
**Problema:** Compilador: "package ClientType does not exist" — enum no importado.
**Solución / Buena práctica:** Al agregar comparaciones de enum en un use case, verificar siempre que el enum esté en imports. `./mvnw test --also-make -pl keygo-app` detecta rápido.
**Archivos clave:** `CreateClientAppUseCase.java`

---

### [2026-03-21] Fase 2 — Patrón de orphanRemoval en colecciones JPA de entidades hijo
**Contexto:** `ClientAppEntity` con `@OneToMany(cascade=ALL, orphanRemoval=true)`.
**Problema:** Reasignar referencia de lista (`entity.setRedirectUris(newList)`) → `orphanRemoval` no elimina hijos — Hibernate pierde tracking de colección original.
**Solución / Buena práctica:** Usar `entity.getRedirectUris().clear()` + `addAll(nuevaLista)`. Nunca reasignar la referencia de la colección.
**Archivos clave:** `ClientAppPersistenceMapper.java`, `ClientAppEntity.java`

---

### [2026-03-21] Fase 2 — TenantEntity como referencia no-managed en ClientAppRepositoryAdapter
**Contexto:** Guardar `ClientApp` con FK `tenant_id` sin cargar el tenant de DB.
**Problema:** JPA requiere referencia de entidad para `@ManyToOne`; cargar el tenant completo sería ineficiente.
**Solución / Buena práctica:** Crear instancia de `TenantEntity` con solo `id` seteado — Hibernate lo trata como proxy. Alternativa idiomática: `entityManager.getReference(TenantEntity.class, id)`.
**Archivos clave:** `ClientAppRepositoryAdapter.java`

---

### [2026-03-21] Fase 3 — Mockito UnnecessaryStubbing con tryFindByEmail que captura IAE
**Contexto:** Tests de `ValidateUserCredentialsUseCase`.
**Problema:** Stubear `findByTenantIdAndEmail` para credencial tipo username ("johndoe") → `UnnecessaryStubbing` porque `tryFindByEmail()` captura `IllegalArgumentException` antes de llamar al mock.
**Solución / Buena práctica:** Al testear username: no stubear el mock de email. Al testear "user not found": usar email válido (`"nobody@acme.com"`) para que el mock SÍ se invoque.
**Archivos clave:** `ValidateUserCredentialsUseCase.java`

---

### [2026-03-21] Fase 3 — PasswordHash.toString() nunca expone el hash (seguridad)
**Contexto:** Value object `PasswordHash` wrappea hash BCrypt.
**Problema:** `toString()` que retorna el hash expondría credenciales en logs.
**Solución / Buena práctica:** `PasswordHash.toString()` siempre retorna `"PasswordHash[REDACTED]"`. El hash real solo con `.value()` en los lugares necesarios.
**Archivos clave:** `PasswordHash.java`

---

### [2026-03-21] Fase 4 — Import correcto de BaseResponse en controllers REST
**Contexto:** `TenantMembershipController` y `TenantAppRoleController`.
**Problema:** Error "cannot find symbol: class BaseResponse" — se intentó importar de `shared.BaseResponse` (incorrecto).
**Solución / Buena práctica:** Siempre importar de `io.cmartinezs.keygo.api.shared.response.BaseResponse` (subpaquete `.response`). Los IDEs pueden auto-completar incorrectamente.
**Archivos clave:** `keygo-api/shared/response/BaseResponse.java`

---

### [2026-03-22] Documentación de modelo de datos y diccionario — estructura, flujos y relaciones
**Contexto:** Generación de documentación completa del modelo de datos bajo orden explícita.
**Problema:** Sin doc centralizada, los devs debían navegar migraciones Flyway + entidades JPA + archivos de dominio por separado.
**Solución / Buena práctica:** Crear 3 documentos complementarios en `docs/data/`: `DATA_MODEL.md` (diccionario), `ENTITY_RELATIONSHIPS.md` (flujos), `MIGRATIONS.md` (índice). Usar Mermaid para todos los diagramas. Referencias cruzadas entre los 3.
**Archivos clave:** `docs/data/DATA_MODEL.md`, `docs/data/ENTITY_RELATIONSHIPS.md`, `docs/data/MIGRATIONS.md`

---

### [2026-03-22] Conversión de diagramas ASCII a Mermaid — criterio de selección
**Contexto:** Auditoría de todos los archivos `.md` del repositorio para identificar diagramas hechos con caracteres de dibujo de caja (box-drawing Unicode: `┌ ┐ └ ┘ │ ─ ▼ ▲`) y convertirlos a Mermaid.
**Problema:** Tres archivos tenían diagramas en ASCII: arquitectura de módulos, componentes hexagonales y flujo del filtro de seguridad con jerarquía de excepciones. Además, múltiples archivos tenían árboles de directorios con `├──` `└──` que *parecían* diagramas ASCII pero no lo son.
**Solución / Buena práctica:**
- Usar `grep -rP '[\x{2500}-\x{257F}]'` para encontrar archivos con box-drawing Unicode.
- **Convertir** diagramas visuales de cajas/flechas → Mermaid (`flowchart TD`, `classDiagram`, `flowchart LR`).
- **No convertir** árboles de directorios (`├──`, `└──`): Mermaid no tiene tipo nativo de árbol de directorios y la versión `flowchart TD` es más verbosa y menos legible.
- Al convertir, actualizar el estado de los módulos en el diagrama para reflejar el estado actual del proyecto (no copiar datos desactualizados del ASCII).
**Archivos clave:**
- `docs/design/ARCHITECTURE.md` (3 diagramas: módulos, flujo request, regla de dependencia)
- `docs/api/BOOTSTRAP_FILTER.md` (2 diagramas: flowchart de autenticación + classDiagram de excepciones)

---

### [2026-03-22] Fase 7: SHA-256 como hash determinista para refresh tokens
**Contexto:** Implementación de refresh tokens (Fase 7). El refresh token plano se genera con `SecureRandom` y no se persiste; solo se guarda su hash para búsqueda posterior.
**Problema:** BCrypt genera un salt distinto por cada invocación, haciendo imposible la búsqueda directa en DB por hash. Para encontrar un refresh token a partir del valor plano recibido del cliente, se necesita un hash determinista.
**Solución / Buena práctica:**
- Usar **SHA-256 (hex, 64 chars)** como `token_hash` en DB — determinista, permite búsqueda directa con `WHERE token_hash = ?`.
- El campo está indexado (`idx_refresh_tokens_hash`) para búsquedas eficientes.
- El token plano tiene 256 bits de entropía (`SecureRandom.nextBytes(32)` + Base64URL), lo que hace inviable la fuerza bruta sobre el hash SHA-256.
- BCrypt no se usa para refresh tokens (solo para contraseñas de usuario).
- Exponer el método `sha256Hex()` como `static` package-private en `RotateRefreshTokenUseCase` para que los tests puedan calcular el hash esperado sin duplicar la lógica.
**Archivos clave:**
- `keygo-app/src/main/java/io/cmartinezs/keygo/app/auth/usecase/RotateRefreshTokenUseCase.java`
- `keygo-app/src/main/java/io/cmartinezs/keygo/app/auth/usecase/RevokeTokenUseCase.java`
- `keygo-supabase/src/main/resources/db/migration/V11__add_refresh_tokens_and_sessions.sql`

### [2026-03-22] Fase 7: Mockito UnnecessaryStubbing en tests de use cases complejos
**Contexto:** Test `RotateRefreshTokenUseCaseTest` configuraba stub para `clientApp.getClientId()` en el método `setupCommonMocks`.
**Problema:** Mockito strict stubbing lanzó `UnnecessaryStubbingException` porque `RotateRefreshTokenUseCase` compara el `clientAppId` (UUID) pero nunca llama a `getClientId()` — ese método es para el `ClientId` (string), no para el `ClientAppId` (UUID).
**Solución / Buena práctica:**
- Revisar qué métodos del mock usa realmente el use case antes de agregar stubs en helpers de tests.
- Si un mock se usa con muchos métodos en múltiples tests, preferir `@MockitoSettings(strictness = Strictness.LENIENT)` o usar `lenient().when(...)` solo para los stubs opcionales.
- Eliminar `when(clientApp.getClientId())` ya que el use case solo usa `clientApp.getId()`.
**Archivos clave:**
- `keygo-app/src/test/java/io/cmartinezs/keygo/app/auth/usecase/RotateRefreshTokenUseCaseTest.java`

### [2026-03-22] Fase 7: BootstrapAdminKeyFilter — rutas userinfo y revoke como públicas
**Contexto:** El filtro `BootstrapAdminKeyFilter` protege todas las rutas bajo `/api/` con `X-KEYGO-ADMIN`. Los nuevos endpoints `/userinfo` y `/oauth2/revoke` son endpoints de usuario (Bearer token) y deben ser accesibles sin clave admin.
**Problema:** Al agregar `/api/v1/tenants/{slug}/userinfo` y `/api/v1/tenants/{slug}/oauth2/revoke` bajo `/api/`, el filtro los bloqueaba con 401 si no se enviaba `X-KEYGO-ADMIN`.
**Solución / Buena práctica:**
- Agregar propiedades `userinfo-path-suffix: /userinfo` y `revocation-path-suffix: /oauth2/revoke` a `KeyGoBootstrapProperties`.
- En `isPublicPath()` del filtro, usar `path.endsWith()` para estos sufijos.
- Patrón: cada tipo de ruta pública tiene su propia propiedad de configuración, lo que permite ajustes sin recompilar.
**Archivos clave:**
- `keygo-run/src/main/java/io/cmartinezs/keygo/run/filter/BootstrapAdminKeyFilter.java`
- `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/properties/KeyGoBootstrapProperties.java`
- `keygo-run/src/main/resources/application.yml`

---

### [2026-03-22] Flyway: CREATE TABLE IF NOT EXISTS oculta errores de esquema incompleto de ejecuciones parciales

**Contexto:** La migración V11 fallaba con `ERROR: column "tenant_id" does not exist (SQLState 42703)` al intentar aplicarse.

**Problema:** La tabla `sessions` ya existía en la base de datos desde una ejecución anterior de V11 que falló a mitad (Flyway no transaccional o interrupción). La sentencia `CREATE TABLE IF NOT EXISTS sessions (...)` la saltó silenciosamente porque la tabla existía, pero esa tabla estaba incompleta (no tenía la columna `tenant_id`). Cuando Flyway llegó a `CREATE INDEX IF NOT EXISTS idx_sessions_user_tenant ON sessions(user_id, tenant_id)`, PostgreSQL lanzó el error 42703 porque `tenant_id` no existía en la tabla residual.

**Solución / Buena práctica:**
- Para migraciones que crean tablas **nuevas** (no parte de un esquema ya en producción), agregar `DROP TABLE IF EXISTS <tabla> CASCADE;` al inicio del archivo, **antes** de los `CREATE TABLE IF NOT EXISTS`.
- Esto garantiza idempotencia real: si la tabla existe con un esquema incompleto o anterior, se elimina y se recrea correctamente.
- Después de modificar un archivo de migración que Flyway ya registró (aunque sea como fallido), ejecutar `flyway:repair` para recalcular el checksum:
  ```bash
  ./mvnw -pl keygo-supabase flyway:repair \
    -Dflyway.url=$SUPABASE_URL \
    -Dflyway.user=$SUPABASE_USER \
    -Dflyway.password=$SUPABASE_PASSWORD
  ```
- **Regla:** `CREATE TABLE IF NOT EXISTS` solo es seguro cuando el esquema de la tabla no va a cambiar. En migraciones nuevas (desarrollo), preferir `DROP TABLE IF EXISTS ... CASCADE` + `CREATE TABLE`.

**Archivos clave:**
- `keygo-supabase/src/main/resources/db/migration/V11__add_refresh_tokens_and_sessions.sql`

---

### [2026-03-26] CORS en Spring Boot 4: configurar `CorsConfigurationSource` + `http.cors(...)` en `SecurityFilterChain`
**Contexto:** El frontend SPA (`http://localhost:5173`) bloqueaba todas las llamadas a `/api/v1/tenants/{slug}/oauth2/authorize` por error de CORS: `No 'Access-Control-Allow-Origin' header is present on the requested resource`.
**Problema:** `SecurityConfig` no tenía ninguna configuración CORS. Spring Security bloqueaba los preflight `OPTIONS` antes de que llegaran al controller. Sin `http.cors(...)`, Spring Security 6 ignora completamente las políticas CORS del servidor.
**Solución / Buena práctica:**
1. Crear `@ConfigurationProperties("keygo.cors")` (`KeyGoCorsProperties`) con `allowedOrigins`, `allowedMethods`, `allowedHeaders`, `allowCredentials` y `maxAge`.
2. Registrar un `@Bean CorsConfigurationSource` en `SecurityConfig` que aplique la config a `/**`.
3. Habilitar con `http.cors(cors -> cors.configurationSource(corsConfigurationSource))` en el `SecurityFilterChain` — este es el único punto donde Spring Security aplica CORS antes de evaluar el filtro de autenticación.
4. `allowCredentials: true` es indispensable cuando el frontend mantiene `JSESSIONID` entre `GET /oauth2/authorize` y `POST /account/login` (cookie cross-origin).
5. `allowedHeaders: ["*"]` es válido con Spring — refleja los headers del preflight. No aplica la restricción del estándar para `allowCredentials` porque Spring hace el echo correcto.
6. Los tests unitarios de la config pueden hacerse sin Spring context: instanciar `SecurityConfig` directamente + `KeyGoCorsProperties` + `CorsConfigurationSource.getCorsConfiguration(request)`.
**Archivos clave:** `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/security/SecurityConfig.java`, `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/properties/KeyGoCorsProperties.java`, `keygo-run/src/main/resources/application.yml`

---

### [2026-03-26] El agente nunca debe ejecutar comandos `git` directamente

**Contexto:** Al verificar si errores de compilación en `keygo-api` eran pre-existentes o introducidos por cambios propios, se ejecutó `git stash` / `git stash pop` para comparar el estado antes y después.
**Problema:** Las instrucciones del proyecto (`.github/copilot-instructions.md`, `CLAUDE.md`, `AGENTS.md`) prohíben explícitamente que el agente ejecute cualquier comando `git` directamente — no porque cause daño necesariamente, sino porque las operaciones de control de versiones pertenecen al flujo del desarrollador humano, no al agente. Ejecutar `git stash` puede interferir con el estado de trabajo del usuario.
**Solución / Buena práctica:** Para verificar si un error es pre-existente, utilizar únicamente `./mvnw clean test` o revisar el log de compilación anterior. Si se necesita comparar estados del repo, **listar los comandos sugeridos** para que el usuario los ejecute manualmente. Nunca invocar `git stash`, `git commit`, `git push`, `git merge`, `git rebase` ni ningún otro subcomando de git desde el agente.
**Archivos clave:** `.github/copilot-instructions.md` §5, `CLAUDE.md` §Git, `AGENTS.md` §Git

---

### [2026-03-26] Verificar estado de compilación pre-existente con `./mvnw clean` antes de atribuir errores a cambios propios

**Contexto:** Al ejecutar `./mvnw -pl keygo-api test` tras agregar anotaciones Swagger, aparecieron errores de compilación en `GlobalExceptionHandler` (clases no encontradas: `ScopeNotGrantedException`, `NoActiveSigningKeyException`, etc.). Se intentó erróneamente verificar con `git stash`.
**Problema:** Maven puede usar clases compiladas previamente en `target/`. Si el proyecto tiene errores pre-existentes en archivos no tocados por el agente, `mvnw test` (sin `clean`) puede fallar por artefactos desactualizados. Atribuir esos errores a los cambios del agente lleva a buscar la causa en el lugar equivocado.
**Solución / Buena práctica:** Antes de asumir que mis cambios introdujeron un error de compilación, ejecutar `./mvnw clean test -pl <módulo>` para asegurar compilación desde cero. Si el error persiste en `clean`, revisar si el archivo afectado fue modificado en esta sesión. Si no lo fue, el error es pre-existente y no debe bloquearse en él.

---

### [2026-03-26] Documentar Swagger: controllers sin anotaciones `@Tag`/`@Operation` y grupos desactualizados

**Contexto:** Al revisar la coherencia entre `docs/api/OPENAPI.md` y el código de los controllers, se detectó que 5 controllers de auth/OIDC (implementados en fases 5-9) nunca recibieron anotaciones Swagger, y `docs/api/OPENAPI.md` quedó congelado en el estado de la fase 4 (2026-03-22).
**Problema:**
- `AuthorizationController`, `JwksController`, `OidcMetadataController`, `RevocationController`, `UserInfoController` — sin `@Tag`, sin `@Operation`, sin `@ApiResponse`.
- `TenantMembershipController` y `TenantAppRoleController` con tags numéricos (`"5-memberships"`, `"6-roles"`) inconsistentes con el patrón del resto.
- `OpenApiConfig.java` tenía 4 grupos; el grupo `2-tenants` capturaba sin filtrar auth, OIDC, memberships y account/profile.
- `OPENAPI.md` documentaba el esquema de seguridad obsoleto `AdminKeyAuth`/`X-KEYGO-ADMIN` (cambiado a `BearerAuth`/Bearer JWT en fase 5), 21 endpoints en lugar de 33, y swagger-ui.path incorrecto.
**Solución / Buena práctica:**
- Al implementar un controller nuevo, agregar siempre `@Tag` (con nombre descriptivo sin prefijo numérico), `@Operation` y `@ApiResponse` en cada método en la misma sesión. No diferirlo.
- Al agregar grupos en `OpenApiConfig`, usar `pathsToExclude` explícito en el grupo `2-tenants` para que no absorba los nuevos paths por defecto.
- Los controllers de OIDC/JWKS que retornan `Map<String, Object>` (no `BaseResponse`) son correctos por diseño — documentarlo con `@Content(mediaType = "application/json")` sin `@Schema`.
- Los tags de membership/roles se nombraron `"5-memberships"` y `"6-roles"` probablemente para ordenación en Swagger UI; la solución correcta es usar `tags-sorter: alpha` en springdoc y tags descriptivos puros.
**Archivos clave:** `keygo-api/src/main/java/io/cmartinezs/keygo/api/auth/controller/`, `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/OpenApiConfig.java`, `docs/api/OPENAPI.md`
---

### [2026-03-26] `ACCEPT_CASE_INSENSITIVE_PROPERTIES` no convierte snake_case a camelCase — configurar `PropertyNamingStrategies.SNAKE_CASE` globalmente

**Contexto:** El cliente `keygo-ui` enviaba el payload del paso 3 del flujo OAuth2 (canje de código) en formato JSON snake_case estándar (`grant_type`, `client_id`, `code_verifier`, `redirect_uri`), pero recibía HTTP 400 `INVALID_INPUT`.

**Problema:**
- `TokenRequest` (y `RevokeTokenRequest`) usan nombres de campo camelCase en Java (`grantType`, `clientId`, `codeVerifier`, `redirectUri`).
- La configuración global de Jackson tenía solo `MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES = true`, que maneja diferencias de **case** (`ClientID` → `clientId`), pero **no convierte guiones bajos** (`grant_type` → `grantType`).
- Al deserializar, todos los campos snake_case quedaban en `null`. El campo `clientId` con `@NotBlank` disparaba `MethodArgumentNotValidException` → `GlobalExceptionHandler.handleValidationException()` → HTTP 400 `INVALID_INPUT`.

**Solución / Buena práctica:**
- Configurar `PropertyNamingStrategies.SNAKE_CASE` globalmente en el `JsonMapperBuilderCustomizer` de `ApplicationConfig`:
  ```java
  .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
  ```
- Esta única línea resuelve el problema para **todos** los DTOs de request del proyecto sin necesitar `@JsonProperty` en cada campo.
- **Impacto en serialización (responses):** los campos multi-palabra sin `@JsonProperty` explícito se serializan como snake_case. Los DTOs de respuesta con `@JsonProperty` existente (como `TokenData`) no se ven afectados. Para campos que deben mantener un nombre específico, usar `@JsonProperty`.
- **No crear** un `BeanDeserializerModifier` / `ValueDeserializerModifier` complejo — la estrategia global es suficiente.
- **Test de regresión:** `TokenRequestJsonTest` (5 casos). El `setUp()` del test debe replicar la misma configuración del customizer (incluyendo `SNAKE_CASE`) sin importar clases de `keygo-run` (dependencia cruzada de módulos).

**Archivos clave:**
- `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/ApplicationConfig.java` (customizer)
- `keygo-api/src/main/java/io/cmartinezs/keygo/api/auth/request/TokenRequest.java`
- `keygo-api/src/main/java/io/cmartinezs/keygo/api/auth/request/RevokeTokenRequest.java`
- `keygo-api/src/test/java/io/cmartinezs/keygo/api/auth/request/TokenRequestJsonTest.java`

---

### [2026-03-26] Shebang faltante en shell script causa ejecución con `sh` en lugar de `bash`

**Contexto:** El script `keygo-supabase/scripts/switch-env.sh` tenía `!/bin/bash` en la primera línea (faltaba el `#`). Al ejecutarse con `./keygo-supabase/scripts/switch-env.sh local` desde la raíz del proyecto, el sistema lo interpretaba con `sh` (shell POSIX) en lugar de `bash`.

**Problema:** Tres síntomas encadenados:
1. `sh` no reconoce `${BASH_SOURCE[0]}` → error `Bad substitution` en línea 23.
2. La asignación `SCRIPT_DIR` falla silenciosamente, resultando en que `PROJECT_DIR` se calculaba tomando el CWD como referencia, apuntando **dos niveles arriba del repo** (`/home/user/Github/cmartinezs/`) en lugar del módulo correcto.
3. `echo -e` con `sh` no interpreta el flag `-e`, imprimiéndolo literal en la salida.
Los errores de `Bad substitution` no abortan el script en `sh` con `set -e` porque la falla ocurre dentro de una sustitución de comando en una asignación, resultando en comportamiento indefinido en lugar de exit.

**Solución / Buena práctica:**
- El shebang siempre debe ser `#!/bin/bash` (con el `#`). Sin él, el kernel no reconoce el intérprete y la mayoría de sistemas delegan a `sh`.
- Verificar la primera línea de cualquier script antes de ejecutarlo: `head -1 script.sh | cat -A` (debe mostrar `#!/bin/bash$`).
- Al diagnosticar "ruta incorrecta" en un script shell, verificar primero si `BASH_SOURCE` y `${BASH_SOURCE[0]}` son accesibles (solo disponibles en `bash`, no en `sh`).
- Para validar sintaxis bash sin ejecutar: `bash -n script.sh`.
- Reorganización aplicada: `switch-env.sh` movido a `scripts/` (raíz del proyecto, scripts generales); templates `.env-*` movidos a `scripts/envs/` (fuera de `keygo-supabase/`). El `.env` activo sigue en `keygo-supabase/.env` para compatibilidad con los scripts de DB.

**Archivos clave:**
- `scripts/switch-env.sh` (nuevo — shebang correcto, rutas correctas)
- `scripts/envs/.env-local`, `.env-desa`, `.env-prod`, `.env.example` (templates centralizados)
- `keygo-supabase/scripts/load-env.sh`, `migrate.sh`, `dev-start.sh` (hints actualizados)

---

### [2026-03-26] Scripts dispersos en módulos: centralizar en `scripts/` + menú principal evita rutas rotas y mejora DX

**Contexto:** El proyecto tenía scripts de operación divididos en dos lugares: `scripts/` (raíz) y `keygo-supabase/scripts/`. Los de supabase eran específicos de DB pero configuraban también variables de la aplicación completa. No había punto de entrada unificado.

**Problema:**
- Los scripts de DB en `keygo-supabase/scripts/` tenían `PROJECT_DIR` apuntando a `keygo-supabase/`, lo que es correcto para Maven/Flyway pero confuso para el desarrollador que trabaja desde la raíz.
- No había un "entry point" único; el desarrollador tenía que recordar qué script hacer desde qué directorio.
- Los stubs de delegación (`exec "../../scripts/db/..."`) son la mejor estrategia de compatibilidad: los paths viejos siguen funcionando sin duplicar lógica.

**Solución / Buena práctica:**
- Centralizar **todos** los scripts en `scripts/` con subfolders temáticos: `scripts/db/`, `scripts/envs/`.
- Crear un **menú principal** (`scripts/keygo.sh`) con modo interactivo y modo directo (`./scripts/keygo.sh <N>`). Esto es el punto de entrada único para cualquier operación.
- Los scripts del módulo (`keygo-supabase/scripts/`) se convierten en **stubs de 2 líneas** que delegan con `exec` al centralizado: compatibilidad sin duplicación.
- El helper interno se prefija con `_` (ej: `scripts/db/_load-env.sh`) y se usa con `source`, nunca ejecutado directamente.
- En zsh, usar `bash << 'HEREDOC'` para scripts que usan `declare -A` (arrays asociativos), ya que zsh no soporta esa sintaxis de bash.

**Archivos clave:**
- `scripts/keygo.sh` — menú principal (20 opciones, 5 categorías)
- `scripts/db/` — scripts centralizados de base de datos
- `keygo-supabase/scripts/*.sh` — stubs de delegación con `exec`

---

### [2026-03-27] Archivos SQL con ediciones parciales sucesivas generan corrupción difícil de detectar

**Contexto:** `data-local.sql` fue modificado en dos sesiones distintas de agente. La primera sesión modificó parcialmente el archivo; la segunda sesión leyó el contenido y lo reportó como correcto, pero el archivo real en disco tenía contenido mezclado (UUIDs viejos `e0eebc99...` y nuevos `11111111...` en el mismo archivo, con statements SQL incompletos al inicio).

**Problema:** El archivo empezaba en línea 1 con `WHERE membership_id = 'e0eebc99...'` — el cierre de un `WHERE NOT EXISTS` de una versión anterior que quedó huérfano. El parser de H2 lo interpretaba como un statement autónomo que comenzaba con `WHERE`, causando `JdbcSQLSyntaxErrorException [42000-240]`.

**Solución / Buena práctica:**
- Cuando un archivo de seed/datos tiene corrupción visible (UUIDs mezclados, statements incompletos al inicio), **reescribirlo completo** con `cat >` en lugar de intentar ediciones puntuales.
- Siempre verificar con `head -5` que el archivo empieza correctamente y con `grep -c "patron_viejo"` que no quedan restos.
- Para archivos SQL idempotentes, la estrategia `INSERT ... SELECT ... WHERE NOT EXISTS` es la correcta para H2; nunca usar `ON CONFLICT` ni `INSERT ... VALUES ... WHERE`.

**Archivos clave:**
- `keygo-run/src/main/resources/data-local.sql`

---

### [2026-03-27] Scripts y Postman bajo `docs/` — corregir `PROJECT_ROOT` al mover scripts a subdirectorios más profundos

**Contexto:** Los scripts de operación se movieron de `scripts/` (1 nivel desde raíz) a `docs/scripts/` (2 niveles desde raíz). La colección Postman se movió de `postman/` a `docs/postman/`.

**Problema:** Todos los scripts que calculaban `PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"` dejaron de apuntar a la raíz del proyecto — en cambio apuntaban a `docs/`. Esto haría que `./mvnw`, `source .env` y demás operaciones fallaran silenciosamente o con "command not found".

**Solución / Buena práctica:** Al mover scripts a un nivel adicional de profundidad, actualizar el cálculo de `PROJECT_ROOT`:
```bash
# ✅ Correcto para docs/scripts/ (2 niveles desde raíz)
PROJECT_ROOT="$( cd "$SCRIPT_DIR/../.." && pwd )"

# ❌ Incorrecto después del movimiento (apunta a docs/)
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"
```
Notar que `docs/scripts/db/_load-env.sh` ya tenía `../..` por estar un nivel más profundo — ese patrón es correcto.
Adicionalmente, actualizar **todas las referencias** en docs de agentes (`AGENTS.md`, `CLAUDE.md`, `AI_CONTEXT.md`, `copilot-instructions.md`, `docs/ai/README.md`) para que los agentes AI usen las rutas correctas.

**Archivos clave:**
- `docs/scripts/keygo.sh`, `docs/scripts/switch-env.sh`, `docs/scripts/check-ai-docs.sh`, `docs/scripts/quick-start.sh`
- `AGENTS.md`, `CLAUDE.md`, `AI_CONTEXT.md`, `.github/copilot-instructions.md`

---

### [2026-03-26] `ON CONFLICT DO NOTHING` es sintaxis PostgreSQL — usar `INSERT ... SELECT ... WHERE NOT EXISTS` en H2

**Contexto:** El perfil `local` usa H2 file-based con `MODE=PostgreSQL`. El archivo `data-local.sql` se ejecuta al arranque para hacer seed idempotente.

**Problema:** H2 2.x con `MODE=PostgreSQL` no soporta la sintaxis `INSERT INTO ... VALUES (...) ON CONFLICT (col) DO NOTHING`. Lanza `JdbcSQLSyntaxErrorException [42000-240]` en el primer statement del script.

**Solución / Buena práctica:** Reemplazar todas las sentencias `ON CONFLICT ... DO NOTHING` por la forma ANSI SQL:
```sql
INSERT INTO tabla (col1, col2, ...)
SELECT val1, val2, ...
WHERE NOT EXISTS (SELECT 1 FROM tabla WHERE clave_unica = valor);
```
Esta sintaxis es compatible con H2 y con PostgreSQL. Para tablas de unión (PK compuesta), el `WHERE NOT EXISTS` compara ambas columnas de la clave.

**Archivos clave:**
- `keygo-run/src/main/resources/data-local.sql`

---

