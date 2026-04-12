# Contexto y Gap

## Contexto

El sistema ya avanzó hacia un modelo de datos donde:

- `platform_users` es la identidad global;
- `tenant_users` es pertenencia al tenant, no identidad primaria;
- `platform_sessions` y `oauth_sessions` separan sesión global y sesión contextual OAuth;
- `authorization_codes` y `refresh_tokens` heredan contexto exacto desde sesiones;
- el aislamiento multi-tenant se apoya en FKs compuestas y constraints a nivel DB.

Sin embargo, el código todavía está en estado híbrido: parte del backend ya migró al nuevo modelo y
otra parte conserva nombres, invariantes y acoplamientos del esquema anterior.

## Problema real

La refactorización ya no puede tratarse como una suma de fixes aislados.

Tenemos drift simultáneo en varias capas:

- dominio que todavía expresa conceptos heredados o ambiguos;
- casos de uso que siguen resolviendo identidad, sujeto o contexto OAuth con reglas viejas;
- entidades JPA y repositories con mapeos parcialmente corregidos;
- adapters que aún traducen entre modelos incompatibles;
- controllers y contratos API que exponen supuestos que ya no representan la base real;
- tests que validan el comportamiento histórico, no el modelo canónico vigente;
- documentación repartida entre RFCs ya absorbidos, docs canónicas y material legacy.

## Síntomas observables

- Campos nullable en dominio que ya no deberían ser opcionales según el flujo actual.
- Relaciones ORM que no reflejan completamente las FKs compuestas del DDL.
- Use cases que mezclan `platform user`, `tenant user`, `subject JWT` y `membership` sin una regla única.
- Reglas de sesiones y tokens repartidas entre controller, app y persistence layer.
- Tests que compilan pero afirman contratos del modelo antiguo.
- Docs que describen el objetivo final, pero el código todavía implementa una mezcla.

## Conclusión

El trabajo pendiente es una realineación end-to-end del backend al DDL actual.

La base de datos ya marcó la dirección. Ahora el resto del sistema debe ponerse al día.
