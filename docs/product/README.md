# Product Design - KeyGo Server

Índice de documentación funcional y de análisis del producto.

> Esta carpeta no es la fuente de verdad operativa del runtime. Para arquitectura, seguridad, setup o migraciones, usar `docs/design/`, `docs/api/`, `docs/development/` y `docs/data/`.

## Documentos activos

- [`SITUACION_ACTUAL.md`](SITUACION_ACTUAL.md): estado funcional y técnico observado.
- [`ANALISIS_DOLORES.md`](ANALISIS_DOLORES.md): problemas, restricciones y dolor actual.
- [`REQUERIMIENTOS.md`](REQUERIMIENTOS.md): requerimientos funcionales y no funcionales.
- [`PROPUESTA_SOLUCION.md`](PROPUESTA_SOLUCION.md): propuesta consolidada y roadmap funcional.
- [`BOUNDED_CONTEXTS.md`](BOUNDED_CONTEXTS.md): dominios y fronteras.
- [`GLOSARIO.md`](GLOSARIO.md): términos y conceptos unificados.
- [`DEPENDENCIAS.md`](DEPENDENCIAS.md): dependencias entre propuestas.

## Diagramas disponibles

- [`DIAGRAMAS/CASOS_DE_USO.md`](DIAGRAMAS/CASOS_DE_USO.md)
- [`DIAGRAMAS/FLUJO_AUTENTICACION.md`](DIAGRAMAS/FLUJO_AUTENTICACION.md)
- [`DIAGRAMAS/FLUJO_TENANT_MANAGEMENT.md`](DIAGRAMAS/FLUJO_TENANT_MANAGEMENT.md)
- [`DIAGRAMAS/FLUJO_BILLING.md`](DIAGRAMAS/FLUJO_BILLING.md)
- [`DIAGRAMAS/FLUJO_ACCOUNT.md`](DIAGRAMAS/FLUJO_ACCOUNT.md)

## Uso recomendado

- Para entender el negocio y el contexto funcional, empezar aquí.
- Para implementar cambios en backend, complementar con:
  - [`../design/ARCHITECTURE.md`](../design/ARCHITECTURE.md)
  - [`../api/AUTH_FLOW.md`](../api/AUTH_FLOW.md)
  - [`../data/MIGRATIONS.md`](../data/MIGRATIONS.md)
  - [`../development/ENVIRONMENT_SETUP.md`](../development/ENVIRONMENT_SETUP.md)
