# Documentation Reorganization Summary

## Objetivo ejecutado

Se centralizó la documentación viva bajo `doc/`, separando material canónico de históricos y dejando en raíz solo documentos de GitHub y gobernanza pública.

## Duplicados detectados

- `ARCHITECTURE.md` en raíz duplicaba el resumen de arquitectura ya cubierto por el canon técnico.
- `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md` era un redirect redundante frente a la guía frontend real.
- Múltiples índices AI y planes de reorganización competían con el índice principal y fueron archivados.

## Contradicciones detectadas

- Referencias residuales a `X-KEYGO-ADMIN` convivían con la seguridad real Bearer JWT.
- Había mezcla entre `docs/`, raíz, ejemplos y módulos para documentos del mismo tema.
- Históricos de reorganización previa aparecían como si fueran documentación operativa vigente.

## Vacíos cubiertos

- Índice maestro bajo `doc/README.md`.
- Índices `README.md` por cada subcarpeta de `doc/`.
- ADRs iniciales para arquitectura, modelo multi-tenant, seguridad Bearer y arquitectura documental.
- Documentación por etapa de desarrollo y por sprint.
- Guía funcional inicial para consola admin.
- Inventarios antes y después para trazabilidad.

## Criterios aplicados

- `01-product`: problema, lenguaje y dirección funcional.
- `02-functional`: flujos y superficies consumidoras.
- `03-architecture`: arquitectura, seguridad y contratos técnicos.
- `04-decisions`: decisiones formales y RFCs.
- `05-delivery`: roadmap, etapas, sprints y releases.
- `06-quality`: testing, debugging, seguridad y estándares.
- `07-operations`: entornos, despliegue y runbooks.
- `08-reference`: catálogos, modelos, artefactos de apoyo y referencias modulares.
- `09-ai`: memoria y operación para agentes.
- `99-archive`: históricos y materiales reemplazados.
