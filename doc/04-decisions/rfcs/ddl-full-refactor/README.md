# RFC — Refactorización Integral para Alinear Todo el Código al Nuevo DDL

> **Estado:** ✅ Closed
> **Scope:** refactor transversal
> **Módulos afectados:** `keygo-domain`, `keygo-app`, `keygo-infra`, `keygo-api`, `keygo-supabase`, `keygo-run`
> **Fuente de verdad técnica:** `keygo-supabase/src/main/resources/db/migration/` + `doc/08-reference/data/migrations.md`

## Objetivo

Definir el plan maestro para realinear todo el backend con el nuevo modelo relacional del sistema,
tomando el DDL vigente como fuente de verdad y eliminando los restos del modelo anterior que aún
siguen filtrándose en dominio, puertos, adapters, seguridad, tests y documentación.

## Documentos

1. `01-contexto-y-gap.md` — por qué el cambio ya no es incremental y cuáles son las brechas
2. `02-estrategia-de-refactor.md` — principios, secuencia y reglas de migración
3. `03-plan-implementacion.md` — fases ejecutables, entregables y criterios de cierre

## Resultado esperado

Al cerrar este RFC:

- el código modela `platform_users`, `tenant_users`, `platform_sessions`, `oauth_sessions`,
  `authorization_codes`, `refresh_tokens`, `signing_keys`, `client_apps`, catálogos OAuth,
  jerarquías RBAC, memberships y flujos de verificación/actividad según el DDL vigente;
- no quedan adapters, entidades, use cases ni contratos API construidos sobre supuestos del esquema
  anterior;
- no quedan artefactos auxiliares críticos (`data-local.sql`, dashboards SQL, seeds, docs funcionales
  y de producto) describiendo o consultando el modelo anterior;
- `Flyway`, `Hibernate validate`, compilación y tests relevantes vuelven a ser coherentes;
- la documentación canónica refleja el modelo actual y no el histórico.

## Estado de cierre

Durante la alineación JPA ↔ Flyway y la revisión funcional posterior quedaron confirmadas dos
brechas de modelo que este RFC absorbió explícitamente:

1. **Onboarding de contratación / billing**
   - `app_contracts.contractor_id` puede quedar nulo hasta el pago/provisionamiento final;
   - la verificación de email del contrato vive en `contract_email_verifications`;
   - `VerifyContractEmail` ya no provisiona usuario ni contractor;
   - `MockApprovePayment` concentra el provisionamiento final, el vínculo `contractor_users`
     (`OWNER`) y el envío de password temporal;
   - `AppContract` ya no arrastra `verificationCode*` ni helpers legacy de verificación local;
   - el snapshot de onboarding (`contractorFirstName/LastName`, `companyName`, `companyTaxId`,
     `companyAddress`) ya quedó persistido en `app_contracts`.
   - la infraestructura general de `VerificationCode` sigue atada a `UserId`, por lo que el
     onboarding de contratos continúa resuelto como flujo específico separado; esto queda como
     seguimiento fuera de este RFC;
   - `password_reset_tokens` no debe reutilizarse para onboarding: representa
     `PASSWORD_RECOVERY` con token hashado, no un código corto de validación de contrato.

2. **Modelo de tenant**
   - este gap quedó despejado: `Tenant`, sus mappers y la API dejaron de
     exponer `ownerEmail` como atributo persistido;
   - el ownership/administración debe seguir saliendo del modelo real (`contractor_id`,
     memberships y roles), no de una columna propia en `tenants`.

## Alcance cerrado y seguimiento fuera del RFC

- Este RFC se considera cerrado porque el drift principal entre DDL activo, código y documentación
  canónica ya quedó absorbido.
- Lo que sigue pendiente pertenece a otros frentes:
  - unificación transversal del modelo de verificación por sujeto más allá de `AppContract`;
  - billing extendido (`invoice`, gateway real, modelo ampliado de pagos/facturación).
