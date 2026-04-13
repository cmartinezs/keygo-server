# Lecciones — Tests

Patrones y errores recurrentes en pruebas unitarias e integración.

---

### [2026-04-07] Al agregar parámetro a un constructor, actualizar TODOS los tests que lo instancian

**Contexto:** Al agregar `PlatformUserRepositoryPort` como 4to parámetro al constructor de `ValidateUserCredentialsUseCase`.

**Problema:** El test `UpdateResetValidateUseCaseTest` instanciaba el use case manualmente con 3 parámetros (sin @InjectMocks), causando 7 errores de compilación por firma de constructor inconsistente.

**Solución / Buena práctica:** Buscar TODAS las instanciaciones del use case en tests (no solo las que usan @InjectMocks). Usar `grep 'new UseCaseName(' --include='*Test.java'` para encontrar todas antes de modificar la firma del constructor.

---

### [2026-04-06] Tests: actualizar stubs cuando cambian firmas de métodos en repositorios

**Contexto:** Al renombrar métodos en `SigningKeyJpaRepository` (de `findFirstByStatus` a `findFirstByTenantIsNullAndStatus` y `findFirstByTenant_IdAndStatus`), los tests de adapter, mapper y use cases seguían usando los nombres antiguos.

**Problema:** Errores de compilación en cascada en tests de `keygo-supabase`, `keygo-app` y `keygo-api`. También afectó `PlatformDashboardAdapter` (código de producción usando el método renombrado).

**Solución / Buena práctica:**
- Al renombrar un método de repositorio JPA, buscar todas las referencias con grep antes de considerar completo el cambio: `grep -r "findFirstByStatus" --include="*.java"`.
- Los archivos afectados deben actualizarse en la misma sesión: adapters, mappers, tests de adapters, tests de mappers, y cualquier otro adapter que use el mismo repositorio.
- Para nuevos parámetros opcionales en métodos de dominio, agregar `null` como último arg en todos los tests existentes.

**Archivos clave:**
- `SigningKeyRepositoryAdapterTest.java` — constructor + stubs actualizados
- `SigningKeyPersistenceMapperTest.java` — `toEntity(domain, null)` en lugar de `toEntity(domain)`
- `PlatformDashboardAdapter.java` — método correcto
- `JwksControllerTest.java` — stubs con tenant

---

### [2026-04-04] Stubs innecesarios en Mockito: `tryFindByEmail` lanza IAE internamente

**Contexto:** Tests de `SendPasswordResetCodeUseCaseTest` con input de username (no email).

**Problema:** En `tryFindByEmail()`, si el string no es un email válido, `EmailAddress.of()` lanza `IllegalArgumentException` capturada internamente, por lo que el mock de `userRepositoryPort.findByTenantIdAndEmail()` nunca se llama. Mockito Strict detecta el stub como innecesario (`UnnecessaryStubbing`) y falla el test.

**Solución / Buena práctica:** No stub-ear `findByTenantIdAndEmail` cuando el input claramente no es un email válido (ej. "johndoe" sin `@`). El método lanza IAE internamente sin llegar al repositorio.

**Archivos clave:** `SendPasswordResetCodeUseCaseTest.java`

---

### [2026-04-04] Al añadir parámetros a controllers y use cases, actualizar tests en cascada

**Contexto:** Flujo RESET_PASSWORD completo — múltiples constructores extendidos.

**Problema:**
1. `ResetPasswordUseCaseTest` instanciaba el use case con el constructor de 3 args en lugar del nuevo de 4 (faltaba `PasswordResetCodeRepositoryPort`).
2. `AuthorizationControllerTest` no pasaba `SendPasswordResetCodeUseCase` al constructor del controller (nuevo parámetro).
3. `AccountSettingsControllerTest` construía `AccountResetPasswordRequest` con 3 args en lugar de 5 (faltaban `confirmNewPassword` y `verificationCode`).
4. `GlobalExceptionHandlerTest` esperaba HTTP 403 para `UserPasswordResetRequiredException` pero el handler retorna 401.

**Solución / Buena práctica:**
- Al añadir parámetros a un use case constructor, **actualizar siempre** el test `@BeforeEach` con el nuevo `@Mock` y el constructor.
- Al añadir parámetros a un controller constructor, **actualizar siempre** el test del controller.
- Al añadir campos a un `record` de request/command, **buscar y actualizar** todos los tests que lo construyen directamente.
- `RESET_PASSWORD_REQUIRED` → HTTP 401 (no 403) porque el usuario no puede autenticarse, no que esté "prohibido".

---

### [2026-04-03] `ApiErrorDataFactory` — nunca mockear cuando los tests verifican campos de `ErrorData`

**Contexto:** `ApiErrorDataFactory` pasó a ser un `@Component` de instancia que depende de `MessageTranslator`.

**Problema:** Varios tests usaban `@Mock private ApiErrorDataFactory factory`. Al ser mock sin stubbear, `factory.fromException(...)` devolvía `null`, causando NPE o `assertThat(...).isNotNull()` fallido.

**Solución / Buena práctica:** Reemplazar `@Mock` por instancia real construida con `StaticMessageSource`:
```java
@BeforeEach
void setUp() {
    factory = new ApiErrorDataFactory(new MessageTranslator(new StaticMessageSource()));
}
```
Esta instancia real devuelve `ErrorData` correctamente populado sin necesitar un Spring context completo.
Cuando `factory` solo necesita existir (sin verificar campos), puede seguir siendo `@Mock` stubbeable.

**Archivos clave:**
- `GlobalExceptionHandlerTest.java`
- `AuthorizationControllerTest.java`
- `BootstrapAdminKeyFilterTest.java`

---

### [2026-04-03] `LocaleContextHolder.resetLocaleContext()` no retorna null — asume default Locale

**Síntoma:** Tests que verificaban `LocaleContextHolder.getLocale() == null` tras `resetLocaleContext()` fallaban, retornando `Locale.US`.

**Causa:** Spring's `LocaleContextHolder` retorna `Locale.getDefault()` si no hay contexto explícito. `reset()` quita el contexto pero `getLocale()` retorna fallback default.

**Solución:** Tests que verifiquen cleanup deben usar `verify(filterChain).doFilter()` en lugar de asumir que `getLocale()` retorna null. El cleanup sucede correctamente; el return value de `getLocale()` es un detalle de Spring.

---

### [2026-04-03] Test pollution con singleton estático — necesita reset en `@AfterEach`

**Síntoma:** `GlobalExceptionHandlerTest.handleUnauthorizedException_shouldReturnUnauthorized` fallaba intermitentemente con `Expecting not blank but was: null` en `clientMessage`.

**Causa:** `ApiErrorDataFactory` tiene un singleton estático `instance`. `ApiErrorDataFactoryI18nTest` creaba una instancia con `MessageSource` mock, pero nunca reseteaba `instance`. Test posterior heredaba la instancia previa.

**Solución:** Usar reflection en `@AfterEach` para resetear campos estáticos:
```java
Field instanceField = ApiErrorDataFactory.class.getDeclaredField("instance");
instanceField.setAccessible(true);
instanceField.set(null, null);
```
Previene test pollution en singletons mutables.

**Archivos clave:** `ApiErrorDataFactoryI18nTest.java`

---

### [2026-04-02] Jerarquía de roles — stub Mockito obsoleto al cambiar nombre de método de puerto

**Síntoma:** `UnnecessaryStubbingException` en `RotateRefreshTokenUseCaseTest` tras cambiar `findRoleCodesByUserAndClientApp` → `findEffectiveRoleCodesByUserAndClientApp` en `MembershipRepositoryPort`.

**Causa:** Al renombrar un método de un puerto (interface), los stubs de Mockito en los tests de los use cases que lo inyectan quedan obsoletos y Mockito strict-mode los detecta.

**Solución:** Buscar con grep el nombre del método antiguo en los directorios de test y actualizar cada `when(mock.oldMethod(...))` al nuevo nombre.

---

### [2026-03-31] `@InjectMocks` no inyecta nuevos puertos si los `@Mock` no están declarados

**Síntoma:** Test fallaba en construcción del use case al agregar `PasswordHasherPort` y `EmailNotificationPort` al constructor.

**Causa:** Al extender un use case con nuevos puertos, los mocks no estaban declarados como `@Mock`.

**Solución:** Al extender un use case con nuevos puertos, siempre agregar `@Mock` correspondientes. Nuevos valores de enum requieren `ALTER TABLE ... DROP CONSTRAINT ... ADD CONSTRAINT` en PostgreSQL (no permite `ALTER CONSTRAINT`).

---

### [2026-03-31] Al cambiar modelo en cascada, actualizar TODOS los `@Mock` y builders de test

**Síntoma:** Tests usaban `.companySlug()` (eliminado), `ACTIVATED` (renombrado a `ACTIVE`), `executeForTenant()`/`executeForUser()` (reemplazados).

**Causa:** Cambios de modelo propagados a toda la pila; mocks insuficientes; fields obligatorios no inicializados.

**Solución:** Al cambiar constructor de use case, actualizar TODOS los `@Mock`. Buscar constantes renombradas con grep. Inicializar todos los fields requeridos en builders de test.

---

### [2026-03-28] Agregar método abstracto a interfaz rompe anonymous classes en tests

**Síntoma:** Tests de 3 archivos fallan: "not abstract and does not override abstract method".

**Causa:** Nuevos métodos abstractos requieren implementación en todas las anonymous classes existentes.

**Solución:** Buscar `new NombreInterfaz()` con grep. Actualizar o migrar a Mockito `@Mock`.

---

### [2026-03-25] Claims map inmutable en tests causa `UnsupportedOperationException`

**Síntoma:** `claims.put("tenant_slug", ...)` lanza `UnsupportedOperationException`.

**Causa:** `Map.of(...)` retorna mapa inmutable; enriquecimiento de claims modifica en-place.

**Solución:** Copiar a mapa mutable antes de enriquecer: `new LinkedHashMap<>(claims)`.

**Archivos clave:** `IssueTokensUseCase.java`, `RotateRefreshTokenUseCase.java`

---

### [2026-03-25] Tests Maven en monorepo sin `-am` fallan por classpath incompleto

**Síntoma:** `./mvnw -pl keygo-supabase test` falla: `NoClassDefFoundError`.

**Causa:** Sin `-am` no se compilan los módulos de los que depende.

**Solución:** Usar `./mvnw -pl keygo-supabase -am test`.

---

### [2026-03-24] Extender record Java: actualizar todos los sitios de construcción

**Síntoma:** Tests con `new UpdateUserCommand(slug, id, "Jane", "Smith")` fallan compilación tras extender a 10 parámetros.

**Causa:** Cambio de constructor no rastreado en todos los callers.

**Solución:** Buscar con grep todos `new NombreRecord(` antes de extender. Pasar `null` para parámetros opcionales.

---

### [2026-03-23] Builder de `ClientApp` en tests: incluir todos los campos requeridos

**Síntoma:** `IllegalArgumentException: ClientApp id cannot be null` en tests.

**Causa:** Builder incompleto en setup de test.

**Solución:** Para `ClientApp` en tests incluir siempre: `id(ClientAppId.generate())`, `type(ClientType.PUBLIC)`, `status(ClientAppStatus.ACTIVE)`, `accessPolicy(new AccessPolicy(...))`.

**Archivos clave:** `ClientApp.java`
