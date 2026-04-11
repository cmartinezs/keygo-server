# keygo-server

Backend IAM multi-tenant para autenticación, autorización y gestión de identidades sobre Java 21 + Spring Boot 4.x.

> Fuente de verdad documental: [`doc/README.md`](doc/README.md)

## Estado actual

- Monorepo Maven multi-módulo con arquitectura hexagonal.
- Seguridad vigente con `Authorization: Bearer <jwt>` para rutas protegidas.
- Prioridades de dominio: multi-tenant, cuenta única por tenant, memberships por app, roles y permisos, OAuth2/OIDC y consola admin.

## Estructura principal

```text
keygo-server/
|-- keygo-domain
|-- keygo-app
|-- keygo-infra
|-- keygo-api
|-- keygo-supabase
|-- keygo-run
|-- keygo-bom
`-- doc
```

## Documentación clave

- Índice maestro: [`doc/README.md`](doc/README.md)
- Arquitectura: [`doc/03-architecture/architecture.md`](doc/03-architecture/architecture.md)
- Roadmap: [`doc/05-delivery/roadmap.md`](doc/05-delivery/roadmap.md)
- Operación y entornos: [`doc/07-operations/README.md`](doc/07-operations/README.md)
- Referencia API y datos: [`doc/08-reference/README.md`](doc/08-reference/README.md)
- Guías de agentes: [`doc/09-ai/README.md`](doc/09-ai/README.md)

## URLs locales

- Base URL: `http://localhost:8080/keygo-server`
- Swagger UI: `http://localhost:8080/keygo-server/swagger-ui/index.html`
- OpenAPI: `http://localhost:8080/keygo-server/v3/api-docs`
- Health: `http://localhost:8080/keygo-server/actuator/health`

## Contribución y seguridad

- [`CONTRIBUTING.md`](CONTRIBUTING.md)
- [`SECURITY.md`](SECURITY.md)
- [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md)
- [`CHANGELOG.md`](CHANGELOG.md)
