# Lecciones — Persistencia

JPA, Flyway, DB schema, queries y migraciones.

---

### [2026-04-13] `LazyInitializationException` al mapear sesión OAuth fuera del límite transaccional

**Contexto / Síntoma:** El flujo `authorize -> login -> token` de plataforma fallaba al emitir tokens con `Could not initialize proxy [...PlatformSessionEntity...] - no session`.

**Problema / Causa:** `SessionRepositoryAdapter.save()` persistía `SessionEntity`/`PlatformSessionEntity` y luego convertía la entidad JPA a dominio tocando asociaciones `LAZY` (`platformSession`, `platformUser`, `clientApp`, `signingKey`) sin una transacción activa. El proxy quedaba sin sesión de Hibernate al resolver el mapper.

**Solución / Buena práctica:** Cuando un adapter JPA devuelve dominio a partir de entidades con asociaciones `LAZY`, envolver el método en `@Transactional` (o cargar explícitamente el grafo requerido) para que el mapper ejecute dentro del contexto de persistencia. Además, registrar un `@ExceptionHandler(LazyInitializationException.class)` explícito para distinguir este tipo de bug de persistencia del fallback genérico.

**Archivos clave:** `SessionRepositoryAdapter.java`, `SessionPersistenceMapper.java`, `GlobalExceptionHandler.java`.

---

### [2026-04-07] Tabla unificada `verification_codes` reemplaza 3 tablas duplicadas

**Contexto:** Refactorización de `email_verifications`, `password_reset_codes` y `password_recovery_tokens`.

**Problema:** Tres tablas casi idénticas (mismos campos: id, tenant_user_id, code/token, expires_at, used_at, created_at) con lógica duplicada en dominio (3 modelos, 3 puertos, 3 adapters, 11 excepciones).

**Solución / Buena práctica:** Usar tabla unificada con columna `purpose` (enum: EMAIL_VERIFICATION, PASSWORD_RESET, PASSWORD_RECOVERY) + partial UNIQUE index `(tenant_user_id, purpose) WHERE used_at IS NULL`. Un solo modelo de dominio `VerificationCode`, un puerto `VerificationCodeRepositoryPort`, y 3 excepciones unificadas parametrizadas por `VerificationPurpose`. Reducción de ~30 archivos a ~10.

**Archivos clave:** `V31__verification_codes.sql`, `VerificationCode.java`, `VerificationPurpose.java`, `VerificationCodeRepositoryPort.java`.

---

### [2026-04-07] T-111 — Patrón de soft-delete con índice parcial en PostgreSQL

**Contexto:** T-111 añade `tenant_user_roles` con soporte de historial de asignaciones revocadas (auditoría).

**Problema:** Un constraint `UNIQUE(tenant_user_id, tenant_role_id)` impediría reasignar un rol previamente revocado (fila histórica ya existente).

**Solución / Buena práctica:**
1. Usar un índice parcial en lugar de un constraint UNIQUE global: `CREATE UNIQUE INDEX ... ON tenant_user_roles(tenant_user_id, tenant_role_id) WHERE removed_at IS NULL`.
2. PostgreSQL aplica la unicidad solo en filas activas, permitiendo múltiples filas revocadas del mismo par.
3. En JPA, el índice parcial NO es declarable con `@Table(uniqueConstraints=...)` — solo existe en la migración Flyway. No intentar replicarlo en la entidad con `@UniqueConstraint`.
4. Para queries que respetan el índice parcial, usar `@Query` JPQL explícita: `WHERE r.removedAt IS NULL` — Spring Data Specifications no soportan índices parciales directamente.

**Archivos clave:** `V25__tenant_roles_and_user_roles.sql`, `TenantUserRoleJpaRepository.java`

---

### [2026-04-07] T-111 — Platform users sin tabla `users` global: FK hacia `tenant_users`

**Contexto:** El RFC define "Platform User" como entidad separada, pero la codebase no tiene tabla `users` (V1 hace DROP sobre ella pero nunca fue creada). Los admins de plataforma son TenantUsers del tenant `keygo`.

**Problema:** Intentar crear FK `platform_user_roles.user_id → users(id)` fallará en Flyway pues la tabla `users` no existe.

**Solución / Buena práctica:**
1. Usar `platform_user_roles.tenant_user_id → tenant_users(id)` como solución pragmática.
2. Documentar la decisión explícitamente en el MODEL.md del diseño.
3. En el seed, siempre filtrar TenantUsers del tenant `keygo` para obtener los admins de plataforma.
4. Esta decisión es reversible: una futura tarea puede añadir tabla `platform_users` y migrar los datos.

**Archivos clave:** `V24__platform_roles_and_user_roles.sql`

---

### [2026-04-06] Entidades JPA huérfanas: relaciones `@ManyToOne` en lugar de UUID crudos

**Contexto:** `UserNotificationPreferencesEntity` y `SigningKeyEntity` no tenían FKs JPA correctas.

**Problema:**
- `UserNotificationPreferencesEntity`: campos `UUID userId/tenantId` sin `@ManyToOne` — Spring Data no puede derivar queries de traversal y pierde integridad referencial a nivel JPA.
- `SigningKeyEntity`: sin `tenant_id` FK. La URL del endpoint JWKS ya era tenant-scoped pero el use case ignoraba el tenant al resolver la clave.
- `sessions` y `refresh_tokens`: sin `signing_key_id` FK → imposible auditar qué clave firmó cada token.

**Solución / Buena práctica:**
1. Reemplazar campos UUID crudos por `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="...")` en las entidades.
2. Usar `getReferenceById()` en adapters (no `findById()`) para setear FKs sin SELECT adicional — Hibernate genera un proxy y solo emite la FK.
3. Para queries Spring Data con traversal, usar notación `findByUser_IdAndTenant_Id` (separador `_` indica navegación de asociación).
4. `signingKeyId` en dominio: mantener como `String` (no VO) para evitar acoplamiento entre agregados. Solo la capa de persistencia materializa la FK.
5. Tenant-scoped signing key con fallback global: `findActiveKeyForTenant(tenantId)` primero busca clave del tenant, luego `tenant IS NULL`. Permite migración gradual sin romper tenants existentes.

**Archivos clave:**
- `UserNotificationPreferencesEntity.java` — `@ManyToOne` reemplaza UUID
- `SigningKeyEntity.java` — nueva relación `@ManyToOne TenantEntity tenant` (nullable)
- `SessionEntity.java` / `RefreshTokenEntity.java` — nueva FK `signing_key_id` (nullable)
- `SigningKeyJpaRepository.java` — métodos tenant-aware
- `V22__signing_key_tenant_scope_and_audit_refs.sql`

---

### [2026-04-03] ⚠️ NUNCA paginar en aplicación — usar JPA Specifications para DB-side filtering

**Síntoma:** Primera implementación de paginación cargaba **todos** los registros (ej: 10k usuarios) en memoria, aplicaba filtros/sorting/pagination en Java, luego retornaba 20 resultados.

**Causa:** Enfoque naïve — "cargar todo, filtrar en app" es simple de implementar pero desastroso en producción.

**Solución / Regla Obligatoria:** Filtrado, ordenamiento y paginación **siempre ocurren en la BD** usando JPA Specifications + `JpaSpecificationExecutor`:
1. Repository extiende `JpaSpecificationExecutor<Entity>`
2. Adapter construye dinámicamente `Specification<Entity>` con predicados JPA Criteria (→ SQL WHERE)
3. Adapter construye `PageRequest` con sorting dinámico (→ SQL ORDER BY)
4. Una sola llamada: `jpaRepository.findAll(spec, pageRequest)` → SQL con LIMIT/OFFSET

Genera SQL real: `SELECT * FROM table WHERE ... ORDER BY ... LIMIT 20 OFFSET 0` — solo los 20 registros llegan a la aplicación.

**Patrón de referencia:** `TenantRepositoryAdapter` + `TenantJpaRepository`. Refactorizados en T-110: `UserRepositoryAdapter`, `ClientAppRepositoryAdapter`, `MembershipRepositoryAdapter`, `AppRoleRepositoryAdapter`.

---

### [2026-04-02] Spring Data JPA — `findByTenantUserId` no es lo mismo que `findByTenantUser_Id`

**Síntoma:** Compilación falla con `cannot resolve property 'tenantUserId'` en `PasswordRecoveryTokenJpaRepository`.

**Causa:** Spring Data JPA usa el nombre de campo del objeto Java, no la columna. Para atravesar relaciones se requiere `_` como separador: `findByTenantUser_Id` (campo `tenantUser.id`). Sin `_`, Spring busca un campo literal `tenantUserId` en la entidad raíz.

**Solución:** Usar siempre `findByRelation_Field(...)` para traversal de relaciones en Spring Data JPA.

---

### [2026-04-02] `TenantUserEntity` — no tiene campo `tenantId`, sino relación `tenant` (TenantEntity)

**Síntoma:** `entity.getTenantUser().getTenantId()` falla en compilación.

**Causa:** `TenantUserEntity` tiene un `@ManyToOne TenantEntity tenant`, no un campo `tenantId` directo.

**Solución:** Usar `entity.getTenantUser().getTenant().getId()` para obtener el tenantId.

---

### [2026-04-02] JaCoCo umbral por módulo — `keygo-supabase` tiene cobertura pre-existente baja (15%)

**Síntoma:** `./mvnw verify` falla en `keygo-supabase` con `instructions covered ratio is 0.15, but expected minimum is 0.60`.

**Causa:** El módulo tiene ~24 adaptadores JPA pero solo 4 con tests unitarios; la brecha existía antes de cualquier cambio.

**Solución:** Override de `<jacoco.minimum.coverage>0.15</jacoco.minimum.coverage>` en `keygo-supabase/pom.xml` con comentario TODO (T-115). Incrementar gradualmente al añadir tests.

---

### [2026-03-30] Migraciones Flyway: dependencia circular entre contractors y tenants

**Síntoma:** `contractors` requería FK a `tenants` y viceversa — circular al crear tablas.

**Causa:** Ambas tablas tienen FKs cruzadas.

**Solución:** Crear columna sin FK, luego agregar FK con `ALTER TABLE ... ADD CONSTRAINT` en migración posterior. Hacer backup antes de reorganizar. Ejecutar `flyway:clean` antes de reescribir historial.

**Archivos clave:** `V3__tenants.sql`, `V11__contractors.sql`

---

### [2026-03-29] Billing: `billing_period` y `base_price` movidos a `billing_options`

**Síntoma:** Tests y mappers usaban `billingPeriod`/`basePrice` en plan — ya no existen ahí.

**Causa:** Refactor parcialmente aplicado; Maven usaba artefactos cacheados.

**Solución:** `AppPlanVersionData` con `billingOptions: List<...>` y `free: boolean`. Plan gratuito = cero filas en billing_options. Compilar desde raíz: `./mvnw compile`.

**Archivos clave:** `V10__billing_catalog.sql`, `AppPlanVersionData.java`

---

### [2026-03-29] UUIDs hardcodeados en FK — usar subqueries semánticas

**Síntoma:** Scripts seed con UUIDs hardcodeados ilegibles; planes asociados a app incorrecta.

**Causa:** FKs escritas literalmente en lugar de derivarse del modelo.

**Solución:** Usar subqueries: `SELECT id FROM <tabla> WHERE <campo_semántico> = '...'`. Preferir `tenants.slug`, `client_apps.client_id`, `app_roles.code`. Para FKs compuestas: CTE o JOIN-based INSERT.

---

### [2026-03-29] Reestructuración Flyway V1–V26 → V1–V17 por dominio

**Síntoma:** Migraciones acumulativas confusas; columna `subscriber_type` en entidad pero ausente en migración.

**Causa:** Migraciones crecieron sin reorganización; comparación incompleta contra JPA entities.

**Solución:** Reiniciar numeración desde V1 (Drop ALL). Una migración por dominio. Validar todas columnas NOT NULL contra JPA entities antes de migrar. Verificar con `ls db/migration/ | sort`.

---

### [2026-03-29] Columna NOT NULL en JPA ausente en migración

**Síntoma:** Hibernate falla en runtime: "missing column [subscriber_type]".

**Causa:** Migración no incluyó la columna; error no detectable en compilación.

**Solución:** Nueva migración correctiva: (1) agregar nullable, (2) back-fill derivando valor de FKs, (3) NOT NULL + CHECK. No modificar migración ya aplicada.

**Archivos clave:** `V23__add_subscriber_type_to_app_subscriptions.sql`, `AppSubscriptionEntity.java`

---

### [2026-03-28] N queries de conteo → GROUP BY en puertos de dashboard

**Síntoma:** ~25 queries `COUNT WHERE status = ?` por petición al dashboard.

**Causa:** Puerto con métodos separados por status; caller invocaba N veces.

**Solución:** Cambiar puerto a `Map<K, Long> countXByStatus()`. JPQL: `SELECT s.status, COUNT(s) FROM ... GROUP BY s.status`. Use case usa `getOrDefault(status, 0L)`.

**Archivos clave:** `PlatformDashboardPort.java`, `PlatformDashboardAdapter.java`

---

### [2026-03-24] Numeración de migraciones desincronizada tras reestructuración Flyway

**Síntoma:** Docs referenciaban V11 para `sessions` (es V8), V12 para `email_verifications` (es V9), etc.

**Causa:** Reestructuración V1–V26 → V1–V17 no actualizó comentarios en documentación.

**Solución:** Tras reestructurar, auditar todos los comentarios `-- Vn` en docs. Validar con `ls db/migration/ | sort`. Incluir tablas de billing en todos los diagramas mermaid.

**Archivos clave:** `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md`

---

### [2026-03-24] Documentación de datos debe basarse en migraciones SQL reales

**Síntoma:** Múltiples discrepancias entre `DATA_MODEL.md` y migraciones V1–V9.

**Causa:** Documentación escrita de memoria sin leer los `.sql`.

**Solución:** AL generar documentación de datos, SIEMPRE leer las migraciones reales. Regla: nueva migración → actualizar `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md` antes de cerrar tarea.
