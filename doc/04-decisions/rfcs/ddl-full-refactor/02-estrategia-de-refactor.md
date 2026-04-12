# Estrategia de Refactor

## Principio rector

El DDL vigente manda.

No se adapta la base al código viejo. Se adapta el código al modelo relacional ya aceptado.

## Fuente de verdad por capa

- Datos: migraciones Flyway reales
- Arquitectura: `doc/03-architecture/`
- Decisiones de transición: RFCs `restructure-multitenant/` y `restructure-implementation/`
- Entidades de persistencia: `keygo-supabase`
- Modelo de dominio y orquestación: `keygo-domain` y `keygo-app`

## Reglas del refactor

1. No hacer parches locales sin cerrar el contrato de extremo a extremo.
2. No introducir nuevos nombres heredados del modelo viejo.
3. Toda regla de identidad debe distinguir explícitamente:
   - `platform user`
   - `tenant user`
   - `membership`
   - `platform session`
   - `oauth session`
4. Toda relación multi-tenant debe respetar las constraints del DDL, especialmente las compuestas.
5. Las invariantes expresadas en DB también son contrato del sistema:
   - `CHECK` constraints;
   - `UNIQUE` parciales;
   - FKs compuestas;
   - triggers y funciones de validación (`oauth_sessions`, `authorization_codes`, `refresh_tokens`,
     jerarquías de roles, etc.).
6. Todo identificador debe declarar si es:
   - PK técnica interna (`id`);
   - identificador funcional o protocolario (`client_id`, `kid`, `email`, `slug`).
7. Cuando documentación y migraciones entren en conflicto, manda Flyway hasta que la documentación
   canónica quede actualizada.
8. El refactor incluye superficies no Java que consumen el modelo:
   - seeds y `data-local.sql`;
   - queries analíticas en `sql/`;
   - OpenAPI, Postman y documentación funcional/producto.
9. Ningún documento canónico debe describir una semántica que el código ya no soporte.
10. No se debe sostener estado funcional crítico en campos transitorios o aliases de compatibilidad
    cuando el flujo necesita sobrevivir round-trips de persistencia.
11. Las verificaciones deben modelarse según el sujeto real del proceso:
    - `PlatformUser` / `TenantUser` para flujos de cuenta ya provisionada;
    - `AppContract` u otro sujeto verificable para onboarding previo a creación de usuario.
12. `password_reset_tokens` sigue representando recuperación self-service con token hashado; no debe
    mezclarse mecánicamente con códigos cortos de onboarding o `RESET_PASSWORD`.
13. `Tenant` no debe seguir cargando semántica persistida de `ownerEmail`; si una API necesita exponer
    “owner” deberá derivarlo del modelo real (`contractor`, memberships, roles o reglas de negocio
    explícitas), no de una columna inexistente.

## Secuencia recomendada

1. Cerrar el modelo de persistencia y sus invariantes.
2. Alinear dominio y puertos al lenguaje correcto.
3. Reescribir use cases sobre esos contratos.
4. Reajustar adapters API y seguridad.
5. Rehacer tests sobre el comportamiento nuevo.
6. Consolidar documentación y roadmap.

## Estrategia de entrega

El refactor debe ejecutarse por workstreams verticales, pero con orden de dependencia claro:

- primero persistencia y contratos internos;
- después orquestación;
- después superficies HTTP y seguridad;
- finalmente hardening, tests y documentación.

Para las brechas ya confirmadas, el orden recomendado es:

1. cerrar el modelo de verificación por sujeto para onboarding de contratos;
2. refactorizar `CreateAppContract` / `ResendContractVerification` / `VerifyContractEmail` /
   `ResumeContractOnboarding` / `ActivateAppContract`;
3. mover el envío de password temporal al punto posterior a pago confirmado y provisionamiento final;
4. limpiar campos legacy transitorios de `AppContract`;
5. refactorizar `Tenant` y su API para eliminar `ownerEmail` como concepto persistido.

Estado actual del orden recomendado:

- los pasos 1-5 ya quedaron absorbidos en código y documentación canónica:
  - `contract_email_verifications`;
  - `contractor_id` nullable hasta pago;
  - provisionamiento en `MockApprovePaymentUseCase`;
  - limpieza legacy de `AppContract`;
  - eliminación de `Tenant.ownerEmail` como semántica persistida.
- el seguimiento restante ya no bloquea este RFC y debe tratarse fuera de este documento
  (modelo transversal de verificación y billing extendido).

## Criterios de aceptación global

- `./mvnw -pl keygo-supabase -am compile` en verde
- `Flyway migrate` y `Flyway validate` en verde
- `ddl-auto=validate` coherente con el esquema
- entidades y queries respetan triggers, FKs compuestas, uniques parciales e identificadores técnicos
  vs funcionales
- tests de dominio, app y persistence ajustados al nuevo modelo
- seeds, `data-local.sql` y dashboards SQL coherentes con el naming y las relaciones nuevas
- docs canónicas actualizadas sin contradicciones abiertas
