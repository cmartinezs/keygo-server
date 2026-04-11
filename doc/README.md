# KeyGo Server Documentation

Índice maestro de la documentación del repositorio.

## Cómo navegar

| Sección | Propósito | Punto de entrada |
|---|---|---|
| `01-product` | Visión, alcance, glosario, bounded contexts y necesidades del producto IAM | [01-product/README.md](01-product/README.md) |
| `02-functional` | Flujos funcionales, integración frontend y consola admin | [02-functional/README.md](02-functional/README.md) |
| `03-architecture` | Arquitectura, seguridad, contratos técnicos y patrones | [03-architecture/README.md](03-architecture/README.md) |
| `04-decisions` | ADRs, RFCs y gobierno de decisiones | [04-decisions/README.md](04-decisions/README.md) |
| `05-delivery` | Roadmap, etapas de desarrollo, sprints y releases | [05-delivery/README.md](05-delivery/README.md) |
| `06-quality` | Testing, debugging, seguridad y estándares de calidad | [06-quality/README.md](06-quality/README.md) |
| `07-operations` | Entornos, despliegue, runbooks y operación runtime | [07-operations/README.md](07-operations/README.md) |
| `08-reference` | Catálogos, modelos de datos, módulos, ejemplos y artefactos de referencia | [08-reference/README.md](08-reference/README.md) |
| `09-ai` | Guías de agentes, memoria operativa y trazabilidad AI | [09-ai/README.md](09-ai/README.md) |
| `99-archive` | Material histórico, reemplazado o solo de consulta | [99-archive/README.md](99-archive/README.md) |

## Fuentes de verdad

- Producto y dominio IAM: [01-product/README.md](01-product/README.md)
- Flujos OAuth2/OIDC y superficies funcionales: [02-functional/README.md](02-functional/README.md)
- Arquitectura modular y multi-tenant: [03-architecture/architecture.md](03-architecture/architecture.md)
- Decisiones tomadas: [04-decisions/adr](04-decisions/adr) y [04-decisions/rfcs](04-decisions/rfcs)
- Roadmap y trazabilidad de entrega: [05-delivery/roadmap.md](05-delivery/roadmap.md)
- Operación de agentes: [09-ai/agent-operations.md](09-ai/agent-operations.md)

## Reglas de ubicación

- Todo documento vivo del proyecto debe vivir bajo `doc/`.
- Solo permanecen en raíz los archivos propios de GitHub o de gobernanza pública del repositorio: `README.md`, `CHANGELOG.md`, `CONTRIBUTING.md`, `LICENSE`, `SECURITY.md`, `CODE_OF_CONDUCT.md` y `.github/`.
- Un documento operativo o técnico no debe duplicarse entre secciones. Si un tema evoluciona, la fuente de verdad se actualiza y lo previo se archiva en `99-archive`.
- Los RFCs describen propuestas, gaps o planes de cambio. Las decisiones aceptadas deben consolidarse también como ADR cuando ya forman parte del sistema.
- Los históricos y reportes de trabajo de documentación no compiten con el canon. Van a `99-archive`.

## Terminología base IAM

- `platform`: ámbito transversal de administración de KeyGo.
- `tenant`: organización o espacio aislado dentro de la plataforma.
- `platform user`: usuario con capacidades de administración de plataforma.
- `tenant user`: identidad de usuario dentro de un tenant.
- `membership`: vinculación de un `tenant user` con una `app` específica.
- `role`: rol asignable en plataforma, tenant o app según el contexto documentado.
- `permission`: permiso átomo o derivado de un rol.

## Inventarios y trazabilidad

- Inventario previo de documentación: [99-archive/documentation-inventory-before.md](99-archive/documentation-inventory-before.md)
- Inventario posterior de documentación: [08-reference/documentation-inventory-after.md](08-reference/documentation-inventory-after.md)
- Decisiones de reorganización documental: [04-decisions/adr/adr-005-documentation-under-doc-directory.md](04-decisions/adr/adr-005-documentation-under-doc-directory.md)
