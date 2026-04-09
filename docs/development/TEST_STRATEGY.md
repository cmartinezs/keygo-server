# Estrategia de Testing - KeyGo Server

Fuente de verdad para tipos de prueba, comandos y convenciones.

## Tipos de prueba

| Tipo | Modulos comunes | Herramientas |
|---|---|---|
| Unit | `keygo-domain`, `keygo-app`, `keygo-api`, `keygo-run`, `keygo-infra` | JUnit 5, AssertJ, Mockito |
| Integracion | `keygo-supabase` | Spring + PostgreSQL/Testcontainers cuando aplique |
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

- El filtro protege rutas bajo `/api/` segun prefijos/sufijos publicos configurados.
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

- Coleccion: `docs/postman/KeyGo-Server.postman_collection.json`
- Environment local: `docs/postman/KeyGo-Server-Local.postman_environment.json`

Al cambiar endpoints o contratos, la coleccion debe mantenerse sincronizada.

## Referencias

- [`ENVIRONMENT_SETUP.md`](ENVIRONMENT_SETUP.md)
- [`../api/BOOTSTRAP_FILTER.md`](../api/BOOTSTRAP_FILTER.md)
- [`../README.md`](../README.md)
