# Estrategia de Testing - KeyGo Server

Fuente de verdad para tipos de prueba, comandos y convenciones.

## Tipos de prueba

| Tipo | Módulos comunes | Herramientas |
|---|---|---|
| Unit | `keygo-domain`, `keygo-app`, `keygo-api`, `keygo-run`, `keygo-infra` | JUnit 5, AssertJ, Mockito |
| Integración | `keygo-supabase` | Spring + PostgreSQL/Testcontainers cuando aplique |
| API / smoke | repo | Postman + scripts en `docs/scripts/` |

## Comandos base

```bash
./mvnw test
./mvnw verify
./mvnw -pl keygo-api test
./mvnw -pl keygo-run test
./mvnw -pl keygo-supabase test
```

## Convenciones

- Unit tests no deben levantar Spring context salvo necesidad real.
- Usar estructura `Given / When / Then`.
- Para requests HTTP protegidas, asumir seguridad Bearer actual.
- Para el filtro bootstrap, probar `setServletPath()` y no `setRequestURI()`.

## Pruebas del filtro bootstrap

- El filtro protege rutas bajo `/api/` según prefijos/sufijos públicos configurados.
- El mecanismo actual es Bearer JWT, no admin key header.
- Si necesitas desactivarlo en pruebas:

```yaml
keygo:
  bootstrap:
    enabled: false
```

## Pruebas manuales / smoke

```bash
./docs/scripts/test-service-info.sh
./docs/scripts/test-response-codes.sh
```

## Postman

- Colección: `docs/postman/KeyGo-Server.postman_collection.json`
- Environment local: `docs/postman/KeyGo-Server-Local.postman_environment.json`

Al cambiar endpoints o contratos, la colección debe mantenerse sincronizada.

## Referencias

- [`../07-operations/environment-setup.md`](../07-operations/environment-setup.md)
- [`../03-architecture/security/bootstrap-filter.md`](../03-architecture/security/bootstrap-filter.md)
- [`../README.md`](../README.md)
