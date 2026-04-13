# Lecciones — Dominio y Casos de Uso

Patrones de dominio, decisiones de diseño, arquitectura hexagonal y casos de uso.

---

### [2026-04-07] T-125 — `Membership.PENDING` como estado inicial requiere `approve()`

**Problema:** `CreateMembershipUseCase` creaba membresías directamente como `ACTIVE`, sin flujo de aprobación. Esto impedía un proceso controlado de otorgamiento de acceso a apps.

**Solución / Buena práctica:** Cambiar el estado inicial a `PENDING` en `CreateMembershipUseCase`; agregar método `approve()` en el dominio `Membership` que valida transición `PENDING→ACTIVE` (lanza `MembershipAlreadyActiveException` si ya activa, `MembershipAlreadySuspendedException` si suspendida); nuevo `ApproveMembershipUseCase` + endpoint `PUT /tenants/{slug}/memberships/{membershipId}/approve`. El método `activate()` existente se mantiene para reactivación de membresías suspendidas.

**Archivos clave:** `Membership.java`, `CreateMembershipUseCase.java`, `ApproveMembershipUseCase.java`, `TenantMembershipController.java`

---

### [2026-04-07] T-111 — Código de `TenantRole` vs `AppRole`: convenciones distintas que se confunden

**Problema:** `AppRole.code` usa lowercase con guiones (validado como `RoleCode`); `TenantRole.code` usa UPPERCASE con guiones bajos (`^[A-Z][A-Z0-9_]*$`). Mezclar la validación genera `IllegalArgumentException` difíciles de detectar.

**Solución / Buena práctica:**
1. `TenantRole.code` valida con `^[A-Z][A-Z0-9_]*$` en el constructor del dominio.
2. `PlatformRole.code` usa lowercase con guiones bajos (ej. `keygo_admin`) — convención diferente a `TenantRole`.
3. Al crear seeds SQL, respetar la convención: `platform_roles` en lowercase, `tenant_roles` en UPPERCASE.
4. Los tests de dominio deben cubrir explícitamente los rechazos de formato incorrecto.

**Archivos clave:** `TenantRole.java`, `PlatformRole.java`, `V26__seed_platform_and_tenant_roles.sql`

---

### [2026-04-02] Excepciones nativas de Java reemplazadas por excepciones propias de KeyGo

**Síntoma:** `IllegalStateException` / `IllegalArgumentException` / `RuntimeException` lanzadas desde modelos de dominio, use cases e infra.

**Solución:** Crear excepciones concretas por capa y contexto: `DomainException` para modelos de dominio, `UseCaseException` para use cases, `PortException` para adaptadores. Los guards de constructores/value-objects (`IllegalArgumentException` en `.of()`) se mantienen — son invariantes de construcción, no reglas de negocio. Al migrar, actualizar los tests que asertan `.isInstanceOf(IllegalArgumentException.class)` a la nueva excepción concreta.

---

### [2026-04-01] Jerarquía de excepciones por capa — patrón de constructores estructurados

**Síntoma:** Los consumers de la API no podían identificar la capa arquitectónica del error ni el tipo específico de excepción. Los use cases lanzaban excepciones genéricas.

**Solución:** `KeyGoException(layer, msg)` → `DomainException` / `UseCaseException` / `PortException`. Constructores con valores tipados, nunca strings construidos por el caller. `ErrorData.layer` siempre visible. Al instalar nuevas clases de `keygo-domain` en módulos dependientes, usar `mvnw install -DskipTests` antes de `test` en el módulo hijo.

---

### [2026-04-01] Falta de membership al crear TenantUser durante onboarding de billing

**Síntoma:** Al completar el onboarding (verificación de email del contrato) el login respondía "usuario no relacionado a la app".

**Causa:** `VerifyContractEmailUseCase.execute()` creaba el `TenantUser` pero **nunca creaba el `Membership`** que vincula a ese usuario con la `clientApp` del proveedor. `IssueAuthorizationCodeUseCase` lanza `MembershipInactiveException` al no encontrar nada.

**Solución:** Inyectar `MembershipRepositoryPort` en `VerifyContractEmailUseCase`. Después de resolver el `TenantUser` (nuevo o existente), verificar con `existsByUserAndClientApp` y si no existe, crear `Membership` con status `ACTIVE`. Stubs `lenient()` para evitar `UnnecessaryStubbingException` en tests que lanzan excepción antes de llegar al membership.

**Archivos clave:** `VerifyContractEmailUseCase.java`, `ApplicationConfig.java`, `VerifyContractEmailUseCaseTest.java`

---

### [2026-04-01] Cobertura completa de excepciones tipadas en todos los use cases

**Síntoma:** 16 lanzamientos de `IllegalArgumentException`/`IllegalStateException` quedaron en `keygo-app` después de la implementación inicial de T-106.

**Solución:** Crear excepción concreta por contexto: `DuplicatePlanCodeException`, `ContractInvalidStateException`, `SubscriptionNotFoundException`, `SubscriptionInvalidStateException`, `UnsupportedPkceMethodException`, `HashingUnavailableException`, `DuplicateAppRoleException`, `DuplicateMembershipException`, `InvalidCommandFieldException`, `ClientAppInactiveException`, `DuplicateTenantException`, `InvalidPaginationParamException`. Para instalar con JaCoCo bloqueando: `mvnw install -Djacoco.skip=true`.

---

### [2026-03-31] Email de contrato billing debe incluir el `contractId` para retomar el onboarding

**Síntoma:** El correo de verificación no incluía el `contractId`, impidiendo que el usuario retomara el proceso con `GET /billing/contracts/{contractId}/resume` si cerraba el navegador.

**Solución:** Agregar método específico `sendContractVerificationEmail(toEmail, recipientName, verificationCode, contractId)` al puerto. `CreateAppContractUseCase` y `ResendContractVerificationUseCase` usan el nuevo método pasando `contract.getId()` (disponible después de `contractRepo.save()`). No modificar `sendVerificationEmail()` ya existente — mantener separación entre flujo de registro de usuarios y flujo de contratos.

**Archivos clave:** `EmailNotificationPort.java`, `SmtpEmailNotificationAdapter.java`, `CreateAppContractUseCase.java`, `ResendContractVerificationUseCase.java`

---

### [2026-03-30] Modelo polimórfico billing — `Contractor` como sujeto central

**Síntoma:** Flujo B2C creaba `TenantUser` sin `tenant_id`, violando constraint NOT NULL. Columnas polimórficas frágiles.

**Causa:** Modelo B2C incompleto; FKs opcionales con CHECK de exclusividad insuficientes.

**Solución:** Introducir `Contractor` como sujeto comercial único. Reemplazar columnas polimórficas por `contractor_id`. Índice único parcial en `app_contracts` por `contractor_id` ACTIVE.

---

### [2026-03-29] Puertos inyectados pero nunca invocados en `CreateAppContractUseCase`

**Síntoma:** Email de verificación nunca se enviaba; código de verificación no se almacenaba en DB.

**Causa:** Puertos inyectados al constructor pero no usados; lógica en repositorio equivocado.

**Solución:** Código de verificación va en `app_contracts` directamente. Use case solo necesita `contractRepo`, `versionRepo`, `emailNotification`. Generar código numérico 6 dígitos con `SecureRandom`.

**Archivos clave:** `CreateAppContractUseCase.java`, `V22__add_contract_verification_code.sql`

---

### [2026-03-29] `ActivateAppContractUseCase`: rama B2C retornaba datos inválidos

**Síntoma:** Suscripción generada sin `subscriberTenantId` ni `subscriberTenantUserId`.

**Causa:** Rama mockeada; tests pasaban sin detectar datos inválidos de negocio.

**Solución:** Para B2C, obtener `tenantId` del PROVEEDOR desde `clientAppRepo`. Agregar `ClientAppRepositoryPort` al constructor.

**Archivos clave:** `ActivateAppContractUseCase.java`

---

### [2026-03-29] `AppBillingSubscriptionController`: resolución de `appId` en tenant incorrecto

**Síntoma:** `getSubscription()` fallaba al buscar app del proveedor bajo el tenant suscriptor.

**Causa:** `resolveClientAppId(tenantSlug, clientId)` filtra por tenant del suscriptor; la app `keygo-platform` pertenece al proveedor.

**Solución:** Agregar `findByClientId(ClientId)` al puerto (búsqueda global). `{tenantSlug}` = suscriptor; `{clientId}` = app del proveedor.

---

### [2026-03-24] Controller respondía 201 sin persistencia real

**Síntoma:** `POST /roles` respondía `201 ROLE_CREATED` pero el rol no existía en DB.

**Causa:** Controller construía objeto en memoria sin invocar use case ni repositorio.

**Solución:** Mover lógica a `CreateAppRoleUseCase` en `keygo-app` con validaciones y puerto de salida.

**Archivos clave:** `CreateAppRoleUseCase.java`, `TenantAppRoleController.java`

---

### [2026-03-24] Claim `roles` en JWT: propagación en cascada al cambiar firma

**Síntoma:** Agregar `roles` requirió cambiar 2 use cases, 1 controller, 1 factory y tests.

**Causa:** Cambio de firma de `TokenClaimsFactoryPort` impacta todos los callers.

**Solución:** Agregar `findRoleCodesByUserAndClientApp(UUID, UUID): List<String>` con `@Query nativeQuery`. Para M2M pasar `null`. Actualizar `@Bean` en `ApplicationConfig`.

**Archivos clave:** `TokenClaimsFactoryPort.java`, `StandardTokenClaimsFactory.java`, `ApplicationConfig.java`

---

### [2026-03-24] Perfil de usuario en IAM: dos capas (canónico + metadata por app)

**Causa:** Indefinición sobre dónde almacenar campos de perfil.

**Solución:**
- Capa 1 (`tenant_users`): perfil canónico OIDC — "¿quién eres?".
- Capa 2 (`membership_attributes`): metadata específica por app — "¿qué eres en esta app?".

---

### [2026-03-24] Inconsistencia docs vs DB: criterio de decisión para correcciones

**Causa:** Corrección anterior actualizó docs para aceptar DB incorrecta (singular), perpetuando inconsistencia.

**Solución / Regla:**
1. Docs mandan en convenciones (plural/singular, casing).
2. Implementación manda si hay razón técnica clara.
3. Nunca marcar "corregido" si solo se ajustaron docs para aceptar un error — agregar "pendiente de migración".

---

### [2026-03-23] keygo-ui: una sola app React con roles en JWT

**Síntoma:** Tres apps React separadas — duplicación de flujos OAuth2 y código.

**Solución:** Una sola app React. Roles en JWT. Routing con `<RoleGuard>` y `useHasRole()`. Simular roles con `VITE_MOCK_ROLE` + MSW en desarrollo.
