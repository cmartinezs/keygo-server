# Documentación - KeyGo Server

Índice documental canónico del proyecto.

## Objetivo

Esta carpeta organiza la documentación activa por categoría y define la fuente de verdad de cada tema.
Si dos documentos parecen contradecirse, este índice y las fuentes de verdad listadas aquí prevalecen.

## Estructura activa

```text
docs/
├── ai/             # Operación y memoria de agentes AI
├── api/            # Flujos y guías humanas de API
├── data/           # Schema, relaciones y migraciones Flyway
├── design/         # Arquitectura y diseño técnico transversal
├── development/    # Setup local, IDE, testing y estilo
├── operations/     # Operación, Docker, firma y despliegue
├── product-design/ # Producto, negocio, dominios y análisis funcional
├── rfc/            # Propuestas, RFCs y planes aún no absorbidos como canon
├── keygo-ui/       # Documentación de integración frontend
├── postman/        # Colección y environment de pruebas manuales
└── archive/        # Histórico, superseded y material de referencia no canónico
```

## Fuente de verdad por categoría

| Categoría | Fuente de verdad | Notas |
|---|---|---|
| Visión general del repo | [`/README.md`](../README.md) | Entrada pública |
| Política de ubicación documental | `docs/README.md` | Este archivo |
| Quick-start técnico para agentes | [`/AGENTS.md`](../AGENTS.md) | Resumen operativo |
| Operación compartida de agentes | [`ai/AGENT_OPERATIONS.md`](ai/AGENT_OPERATIONS.md) | Canon para wrappers AI |
| Snapshot operativo del proyecto | [`/AI_CONTEXT.md`](../AI_CONTEXT.md) | Estado actual resumido |
| Arquitectura técnica | [`design/ARCHITECTURE.md`](design/ARCHITECTURE.md) | `ARCHITECTURE.md` raíz es resumen |
| Seguridad de rutas/API bootstrap | [`api/BOOTSTRAP_FILTER.md`](api/BOOTSTRAP_FILTER.md) | Bearer-only y rutas públicas |
| Flujos OAuth2/OIDC | [`api/AUTH_FLOW.md`](api/AUTH_FLOW.md) | Guía funcional/técnica |
| Migraciones Flyway | [`data/MIGRATIONS.md`](data/MIGRATIONS.md) | Versiones aplicadas y siguiente `V{n}` |
| Modelo de datos | [`data/DATA_MODEL.md`](data/DATA_MODEL.md) | Diccionario de tablas |
| Setup local y variables | [`development/ENVIRONMENT_SETUP.md`](development/ENVIRONMENT_SETUP.md) | Scripts reales en `docs/scripts/` |
| Testing | [`development/TEST_STRATEGY.md`](development/TEST_STRATEGY.md) | Convenciones y comandos |
| Operación Docker | [`operations/DOCKER.md`](operations/DOCKER.md) | Runtime local y despliegue |
| Inventario humano de endpoints | [`keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`](keygo-ui/FRONTEND_DEVELOPER_GUIDE.md) | Sección de endpoints para integración |
| Contrato runtime de API | `/v3/api-docs` + controllers anotados | OpenAPI generado, no este repo Markdown |
| Propuestas activas/completadas | [`/ROADMAP.md`](../ROADMAP.md) | RFCs amplían o preceden decisiones |
| Memoria AI | [`ai/`](ai/) | Lecciones, propuestas, inconsistencias, registro |
| Histórico / superseded | [`archive/`](archive/) | No usar como canon sin validación |

## Dónde debe ir un nuevo `.md`

- Raíz del repo: solo entrypoints globales o políticas públicas del repositorio.
- `docs/ai/`: memoria operativa, políticas compartidas de agentes, lecciones, inconsistencias.
- `docs/design/`: arquitectura, diseño transversal, decisiones técnicas adoptadas.
- `docs/api/`: guías de uso de API, seguridad HTTP, flujos, OpenAPI humana.
- `docs/data/`: schema, migraciones, relaciones, diccionario de datos.
- `docs/development/`: entorno local, IDE, testing, estilo, herramientas.
- `docs/operations/`: runtime, Docker, despliegue, llaves, observabilidad operativa.
- `docs/product-design/`: producto, negocio, bounded contexts, requerimientos, UX y análisis.
- `docs/rfc/`: propuestas o planes no absorbidos aún como fuente de verdad.
- `docs/archive/`: documentos históricos, superseded o de investigación.
- README de módulo: solo si el contenido es estrictamente específico de ese módulo.

## Reglas de mantenimiento

- Evitar duplicar el mismo detalle en raíz y en `docs/`.
- Si un documento resumido referencia detalle, el detalle es el canon.
- Si un documento deja de ser canónico, moverlo a `docs/archive/` o dejarlo como stub con referencia explícita.
- Corregir enlaces relativos al mover documentos.
- No usar `docs/archive/` como fuente de verdad operativa sin una nota explícita que lo reactive.

## Navegación recomendada

### Nuevo en el repo

1. [`../README.md`](../README.md)
2. [`design/ARCHITECTURE.md`](design/ARCHITECTURE.md)
3. [`development/ENVIRONMENT_SETUP.md`](development/ENVIRONMENT_SETUP.md)
4. [`api/AUTH_FLOW.md`](api/AUTH_FLOW.md)

### Trabajando en backend

1. [`../AGENTS.md`](../AGENTS.md)
2. [`../AI_CONTEXT.md`](../AI_CONTEXT.md)
3. [`design/ARCHITECTURE.md`](design/ARCHITECTURE.md)
4. [`data/MIGRATIONS.md`](data/MIGRATIONS.md)
5. [`api/BOOTSTRAP_FILTER.md`](api/BOOTSTRAP_FILTER.md)

### Trabajando en integración frontend

1. [`keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`](keygo-ui/FRONTEND_DEVELOPER_GUIDE.md)
2. [`api/AUTH_FLOW.md`](api/AUTH_FLOW.md)
3. [`api/OPENAPI.md`](api/OPENAPI.md)
4. [`postman/KeyGo-Server.postman_collection.json`](postman/KeyGo-Server.postman_collection.json)

### Buscando material histórico

- [`archive/README.md`](archive/README.md)
