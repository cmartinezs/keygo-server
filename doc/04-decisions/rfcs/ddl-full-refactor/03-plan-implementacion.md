# Plan de Implementación

## Fase 0 — Congelación semántica

Objetivo: fijar lenguaje y reglas antes de seguir cambiando código.

Entregables:

- matriz de equivalencias viejo → nuevo por concepto;
- lista de invariantes obligatorias del DDL que deben existir también en código;
- matriz de verdad entre Flyway activo, entidades JPA, SQL auxiliar y documentación;
- decisión explícita de reconciliación para divergencias de línea de migración o naming
  (por ejemplo, artefactos que aún hablan de `verification_codes` vs baseline activo con
  `email_verifications` + `password_reset_tokens`);
- lista de clases, endpoints y tests que aún dependen del modelo anterior.

Criterio de cierre:

- ya no hay ambigüedad sobre qué representa cada entidad principal.

## Fase 1 — Persistencia canónica

Objetivo: dejar `keygo-supabase` totalmente alineado con el DDL.

Trabajo:

- revisar todas las entidades JPA afectadas por identidad, sesiones, tokens, signing keys y RBAC;
- incluir `client_apps`, `client_redirect_uris`, `client_allowed_grants`, `client_allowed_scopes`,
  `app_memberships`, `app_membership_roles`, preferencias y actividad global;
- corregir `@JoinColumn` y `@JoinColumns` según FKs reales;
- modelar explícitamente las tablas puente y jerarquías (`*_role_hierarchy`, `*_user_roles`,
  `app_membership_roles`) sin shortcuts conceptuales;
- normalizar repositories y queries al naming actual;
- cerrar mappers persistence ↔ domain para que no dependan de columnas o relaciones obsoletas;
- revisar que entidades y adapters no ignoren invariantes impuestas por triggers/constraints de DB;
- eliminar accessors o aliases que escondan diferencias entre `platform_users` y `tenant_users`.

Entregables:

- entidades JPA consistentes;
- adapters de persistencia consistentes;
- tests de mapper y adapter actualizados;
- compilación de reactor verde.

Criterio de cierre:

- `keygo-supabase` compila y pasa tests focalizados sin workarounds funcionales.

## Fase 2 — Dominio y puertos

Objetivo: alinear el lenguaje del core con el modelo real.

Trabajo:

- revisar aggregates y value objects de auth, user, tenant, membership y RBAC;
- eliminar nullability heredada donde ya no aplica;
- redefinir puertos con responsabilidades explícitas por contexto;
- declarar con precisión cuándo el dominio usa PK técnica versus identificador funcional/protocolario;
- separar mejor identidad global, pertenencia tenant y contexto app;
- reemplazar el modelo de verificación atado solo a `UserId` por un modelo de sujeto verificable que
  también cubra onboarding de contratos;
- mantener eliminado `ownerEmail` como atributo persistido de `Tenant` y, si alguna API necesita
  exponer ownership, derivarlo sin reintroducir semántica vieja.

Entregables:

- contratos de dominio claros;
- puertos sin dependencia conceptual del esquema viejo;
- exceptions y naming coherentes.

Criterio de cierre:

- ningún use case necesita “reinterpretar” el dominio para entender el DDL.

## Fase 3 — Use cases y orquestación

Objetivo: rehacer la lógica de aplicación sobre el modelo nuevo.

Trabajo:

- refactorizar flujos de auth platform y tenant;
- ajustar apertura de sesión, emisión de auth code, rotación de refresh token y revocación;
- incluir verificación de email, reset/recovery de contraseña, preferencias de notificación y
  actividad de cuenta como parte del modelo nuevo de identidad;
- rehacer onboarding de contratación para que la validación de email sobreviva persistencia real,
  no dependa de campos transitorios en `AppContract`, y respete la secuencia:
  validación de email → confirmación de pago → provisionamiento final → envío de password temporal;
- revisar RBAC por scope: platform, tenant y app;
- revisar scopes `GLOBAL`, `CONTRACTOR` y `TENANT` donde el DDL los hace explícitos;
- mover reglas ad hoc desde controllers/adapters a application layer o domain donde corresponda.

Entregables:

- use cases de auth y acceso consistentes;
- comandos/results alineados;
- tests de app reescritos para la semántica nueva.

Estado parcial ya absorbido:

- `CreateAppContract`, `ResendContractVerification`, `VerifyContractEmail` y
  `ResumeContractOnboarding` ya quedaron conectados a persistencia real de verificación de contrato;
- `MockApprovePayment` ya concentra provisionamiento de `PlatformUser` / `Contractor` y envío de
  password temporal;
- `AppContract` ya no arrastra campos/helpers legacy de verificación local y el snapshot de
  onboarding ya quedó persistido en `app_contracts`.

Criterio de cierre:

- los flujos críticos se entienden de punta a punta sin puentes legacy.

## Fase 4 — API, seguridad y contratos externos

Objetivo: adaptar la superficie HTTP al modelo ya corregido internamente.

Trabajo:

- revisar controllers, DTOs y responses afectados;
- ajustar claims, subjects y reglas de autorización;
- verificar que filtros y seguridad no dependan de IDs o scopes obsoletos;
- mantener fuera de contratos HTTP y DTOs de tenant la semántica persistida de `ownerEmail`;
- ajustar DTOs/responses de billing para que no dependan de códigos o nombres que solo existan en
  compatibilidad transitoria;
- actualizar OpenAPI, ejemplos, Postman y contratos manuales si aplica.

Entregables:

- contratos API coherentes;
- seguridad alineada con el nuevo contexto de identidad;
- tests de controller actualizados.

Criterio de cierre:

- la API deja de exponer semántica histórica.

## Fase 5 — Calidad, datos y documentación

Objetivo: cerrar el refactor con validación objetiva.

Trabajo:

- ampliar tests de integración JPA ↔ Flyway;
- validar `Hibernate validate` en base limpia;
- actualizar documentación canónica:
  - `03-architecture/database-schema.md`
  - `01-product/current-state.md`
  - `02-functional/authentication-flow.md`
  - `08-reference/data/migrations.md`
  - `08-reference/data/*`
  - módulos y flujos afectados
- revisar artefactos auxiliares que consultan o seed-ean el schema:
  - `keygo-run/src/main/resources/data-local.sql`
  - `sql/platform_dashboards/*`
- registrar lecciones y riesgos residuales.

Entregables:

- suite mínima de regresión para el nuevo modelo;
- documentación actualizada;
- checklist de cierre del RFC.

Criterio de cierre:

- el código, la base y la documentación cuentan la misma historia.

## Workstreams transversales

### A. Identidad y usuarios

- `platform_users`
- `tenant_users`
- vínculo entre ambos
- naming y subject resolution

### B. Sesiones y tokens

- `platform_sessions`
- `oauth_sessions`
- `authorization_codes`
- `refresh_tokens`
- `signing_keys`

### C. Catálogo OAuth y acceso a apps

- `client_apps`
- `client_redirect_uris`
- `client_allowed_grants`
- `client_allowed_scopes`
- `app_memberships`
- `app_membership_roles`

### D. Autorización y RBAC

- platform roles
- tenant roles
- app roles
- role hierarchies
- claims JWT y lookup de permisos

### E. Flujos de identidad de usuario

- `email_verifications`
- `password_reset_tokens`
- `platform_user_notification_preferences`
- `platform_activity_events`
- modelo de verificación por sujeto (`PlatformUser` vs `AppContract` u otro sujeto de onboarding)

### F. Contratos API y seguridad

- login platform
- login tenant
- userinfo / revoke / token
- admin endpoints
- claims, subject resolution y DTOs expuestos
- tenant API sin `ownerEmail` persistido
- billing onboarding y verificación de contrato

### G. Calidad y trazabilidad

- tests
- documentación
- roadmap
- seeds y SQL analítico
- checklist de absorción a canon

## Orden sugerido de ejecución

1. Fase 0
2. Fase 1
3. Fase 2
4. Fase 3
5. Fase 4
6. Fase 5

## Riesgos principales

- seguir mezclando fixes puntuales con semántica vieja;
- usar docs archivados o snapshots previos del baseline como si fueran fuente de verdad actual;
- corregir entidades sin corregir seeds, dashboards SQL o documentación operativa que dependen del
  mismo naming;
- dejar sin modelar invariantes que hoy ya viven en triggers/constraints de PostgreSQL;
- no resolver explícitamente divergencias entre baseline activo y artefactos heredados/archivados;
- mantener onboarding de contratos sobre estado transitorio que se pierde al leer desde DB;
- seguir tratando `ownerEmail` de tenant como dato persistido aunque el baseline ya no lo soporte;
- dejar nullability temporal convertida en deuda permanente;
- actualizar adapters sin corregir primero dominio y puertos;
- considerar “verde” una compilación sin validar contratos y tests;
- mantener documentación vieja como si fuera canónica.

## Definición de terminado

Este RFC puede cerrarse cuando:

- el modelo legacy ya no condiciona decisiones nuevas;
- los módulos principales compilan y prueban sobre el modelo nuevo;
- la documentación viva refleja el estado final;
- las decisiones absorbidas se consolidan en ADR o en docs canónicas.

Estado de cierre alcanzado:

- el drift principal DDL ↔ código ↔ documentación ya quedó absorbido;
- la compilación final fue confirmada desde IntelliJ/Maven;
- los seguimientos restantes (`align-user-verification-storage`, billing extendido) ya no forman
  parte del alcance de este RFC.
