# T-135 — Revisar scripts `*.sh` con fallas de ejecución

**Estado:** ⬜ Registrada
**Horizonte:** corto plazo
**Módulos afectados:** `scripts/`, docs

## Requisito

Revisar los scripts shell del repositorio (`scripts/**/*.sh`) porque algunos no funcionan
correctamente en todos los contextos de ejecución.

La revisión debe identificar al menos:

1. Incompatibilidades entre `bash` y `sh`.
2. Problemas de carga de variables desde `.env`.
3. Supuestos no documentados sobre el directorio actual, permisos de ejecución o herramientas
   disponibles.
4. Scripts con prompts interactivos, mensajes o flujos que dificultan su uso operativo.

El objetivo es dejar el set de scripts más consistente, predecible y documentado, sin romper
los flujos actuales de desarrollo, operación local o mantenimiento de base de datos.

## Decisiones a tomar antes de activar

Antes de planificar la implementación, resolver:

1. Si el estándar oficial para los scripts será **Bash exclusivamente** o si algunos deben
   seguir siendo compatibles con `sh`.
2. Qué scripts son prioritarios para la primera pasada (`db/`, bootstrap, docker, utilitarios).
3. Si la salida esperada será solo corrección técnica o también unificación de estilo,
   mensajes y ayudas de uso.

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
