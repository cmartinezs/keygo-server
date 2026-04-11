# ADR-004: Bearer JWT For Admin And Protected Routes

- Fecha: 2026-04-11
- Estado: accepted

## Contexto

El repositorio ya no usa `X-KEYGO-ADMIN` como mecanismo vigente para administración. La seguridad actual se apoya en JWT Bearer y authorities derivadas del token.

## Decisión

Las rutas protegidas se documentan y operan con `Authorization: Bearer <jwt>`, con evaluacion por authorities, alcance y aislamiento de tenant cuando corresponde.

## Consecuencias

- La documentación de seguridad debe tratar `X-KEYGO-ADMIN` como historial o material archivado.
- Los tests de seguridad y guías de integración deben usar Bearer JWT como flujo vigente.
- Las rutas públicas y excepciones quedan documentadas de forma explícita.

## Alternativas consideradas

- Mantener header propietario para administración.
- Mezclar header propietario y Bearer como estrategias equivalentes.

