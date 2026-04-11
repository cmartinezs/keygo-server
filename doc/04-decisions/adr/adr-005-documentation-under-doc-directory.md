# ADR-005: Documentation Under Doc Directory

- Fecha: 2026-04-11
- Estado: accepted

## Contexto

La documentación del repositorio estaba dispersa entre raíz, `docs/`, submódulos, ejemplos y árboles históricos. Eso generaba redundancia, enlaces rotos y conflicto entre fuente de verdad y material histórico.

## Decisión

Toda la documentación viva del proyecto se centraliza bajo `doc/`, manteniendo en raíz solo los documentos que tienen sentido para GitHub y la gobernanza pública del repositorio.

## Consecuencias

- Secciones vivas y archivadas quedan separadas de forma explícita.
- La navegación mejora mediante índices por producto, funcionalidad, arquitectura, decisiones, entrega, calidad, operaciones, referencia y AI.
- Los documentos históricos siguen disponibles en `99-archive` sin competir con el canon.

## Alternativas consideradas

- Mantener `docs/` como contenedor mixto.
- Repartir documentación viva entre raíz, módulos y ejemplos.
