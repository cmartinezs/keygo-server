# Documentacion - KeyGo Server

Indice documental canonico del proyecto.

## Objetivo

Esta carpeta organiza la documentacion activa por categoria y define la fuente de verdad de cada tema.
Si dos documentos parecen contradecirse, este indice y las fuentes de verdad listadas aqui prevalecen.

## Estructura activa

```text
docs/
├── ai/             # Operacion y memoria de agentes AI
├── api/            # Flujos y guias humanas de API
├── data/           # Schema, relaciones y migraciones Flyway
├── design/         # Arquitectura y diseno tecnico transversal
├── development/    # Setup local, IDE, testing y estilo
├── operations/     # Operacion, Docker, firma y despliegue
├── product-design/ # Producto, negocio, dominios y analisis funcional
├── rfc/            # Propuestas, RFCs y planes aun no absorbidos como canon
├── keygo-ui/       # Documentacion de integracion frontend
├── postman/        # Coleccion y environment de pruebas manuales
└── archive/        # Historico, superseded y material de referencia no canonico
```

## Fuente de verdad por categoria

| Categoria | Fuente de verdad | Notas |
|---|---|---|
| Vision general del repo | [`/README.md`](../README.md) | Entrada publica |
| Politica de ubicacion documental | `docs/README.md` | Este archivo |
| Quick-start tecnico para agentes | [`/AGENTS.md`](../AGENTS.md) | Resumen operativo |
| Operacion compartida de agentes | [`ai/AGENT_OPERATIONS.md`](ai/AGENT_OPERATIONS.md) | Canon para wrappers AI |
| Snapshot operativo del proyecto | [`/AI_CONTEXT.md`](../AI_CONTEXT.md) | Estado actual resumido |
| Arquitectura tecnica | [`design/ARCHITECTURE.md`](design/ARCHITECTURE.md) | `ARCHITECTURE.md` raiz es resumen |
| Seguridad de rutas/API bootstrap | [`api/BOOTSTRAP_FILTER.md`](api/BOOTSTRAP_FILTER.md) | Bearer-only y rutas publicas |
| Flujos OAuth2/OIDC | [`api/AUTH_FLOW.md`](api/AUTH_FLOW.md) | Guia funcional/tecnica |
| Migraciones Flyway | [`data/MIGRATIONS.md`](data/MIGRATIONS.md) | Versiones aplicadas y siguiente `V{n}` |
| Modelo de datos | [`data/DATA_MODEL.md`](data/DATA_MODEL.md) | Diccionario de tablas |
| Setup local y variables | [`development/ENVIRONMENT_SETUP.md`](development/ENVIRONMENT_SETUP.md) | Scripts reales en `docs/scripts/` |
| Testing | [`development/TEST_STRATEGY.md`](development/TEST_STRATEGY.md) | Convenciones y comandos |
| Operacion Docker | [`operations/DOCKER.md`](operations/DOCKER.md) | Runtime local y despliegue |
| Inventario humano de endpoints | [`keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`](keygo-ui/FRONTEND_DEVELOPER_GUIDE.md) | Seccion de endpoints para integracion |
| Contrato runtime de API | `/v3/api-docs` + controllers anotados | OpenAPI generado, no este repo Markdown |
| Propuestas activas/completadas | [`/ROADMAP.md`](../ROADMAP.md) | RFCs amplian o preceden decisiones |
| Memoria AI | [`ai/`](ai/) | Lecciones, propuestas, inconsistencias, registro |
| Historico / superseded | [`archive/`](archive/) | No usar como canon sin validacion |

## Donde debe ir un nuevo `.md`

- Raiz del repo: solo entrypoints globales o politicas publicas del repositorio.
- `docs/ai/`: memoria operativa, politicas compartidas de agentes, lecciones, inconsistencias.
- `docs/design/`: arquitectura, diseno transversal, decisiones tecnicas adoptadas.
- `docs/api/`: guias de uso de API, seguridad HTTP, flujos, OpenAPI humana.
- `docs/data/`: schema, migraciones, relaciones, diccionario de datos.
- `docs/development/`: entorno local, IDE, testing, estilo, herramientas.
- `docs/operations/`: runtime, Docker, despliegue, llaves, observabilidad operativa.
- `docs/product-design/`: producto, negocio, bounded contexts, requerimientos, UX y analisis.
- `docs/rfc/`: propuestas o planes no absorbidos aun como fuente de verdad.
- `docs/archive/`: documentos historicos, superseded o de investigacion.
- README de modulo: solo si el contenido es estrictamente especifico de ese modulo.

## Reglas de mantenimiento

- Evitar duplicar el mismo detalle en raiz y en `docs/`.
- Si un documento resumido referencia detalle, el detalle es el canon.
- Si un documento deja de ser canónico, moverlo a `docs/archive/` o dejarlo como stub con referencia explicita.
- Corregir enlaces relativos al mover documentos.
- No usar `docs/archive/` como fuente de verdad operativa sin una nota explicita que lo reactive.

## Navegacion recomendada

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

### Trabajando en integracion frontend

1. [`keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`](keygo-ui/FRONTEND_DEVELOPER_GUIDE.md)
2. [`api/AUTH_FLOW.md`](api/AUTH_FLOW.md)
3. [`api/OPENAPI.md`](api/OPENAPI.md)
4. [`postman/KeyGo-Server.postman_collection.json`](postman/KeyGo-Server.postman_collection.json)

### Buscando material historico

- [`archive/README.md`](archive/README.md)

