# T-134 — Separar `ApplicationConfig` por categorías de dominio y estereotipo

**Estado:** ⬜ Registrada
**Horizonte:** corto plazo
**Módulos afectados:** `keygo-run`

## Requisito

Refactorizar `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/ApplicationConfig.java`
para dejar de concentrar en una sola clase la definición de beans de múltiples áreas.

La separación debe evaluar al menos dos ejes de organización:

1. **Dominios/capacidades:** por ejemplo `auth`, `billing`, `tenant`, `user`,
   `platform`, `membership`, `clientapp`.
2. **Estereotipos técnicos:** por ejemplo configuración de casos de uso, adapters,
   serializers/Jackson, filtros, i18n o wiring transversal.

El objetivo es mejorar mantenibilidad, legibilidad y trazabilidad del wiring Spring,
sin alterar el contrato funcional actual ni introducir dependencias inversas entre módulos.

## Decisiones a tomar antes de activar

Antes de planificar la implementación, resolver:

1. Si la partición principal será **por dominio**, **por estereotipo** o una combinación
   jerárquica de ambas.
2. Qué beans deben permanecer en una configuración transversal común y cuáles deben migrar
   a clases específicas.
3. Cómo reorganizar los tests de configuración (`ApplicationConfigTest` y potenciales tests
   nuevos) para mantener cobertura útil después del split.

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
