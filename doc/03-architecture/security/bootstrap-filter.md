# Bootstrap Filter

Referencia vigente para el filtro bootstrap y sus rutas públicas.

- Base protegida: `keygo.bootstrap.api-path-prefix=/api/`
- Las rutas protegidas requieren Bearer JWT salvo que coincidan con un prefijo o sufijo público configurado.

### Rutas públicas por prefijo

| Propiedad | Valor actual | Uso |
|---|---|---|
| `keygo.bootstrap.actuator-path-prefix` | `/actuator/` | health y actuator |
| `keygo.bootstrap.swagger-ui-path-prefix` | `/swagger-ui` | Swagger UI |
| `keygo.bootstrap.api-docs-path-prefix` | `/v3/api-docs` | OpenAPI |
| `keygo.bootstrap.well-known-path-prefix` | `/.well-known` | OIDC discovery + JWKS |
| `keygo.bootstrap.platform-login-path-prefix` | `/api/v1/platform/account/login` | login plataforma |
| `keygo.bootstrap.platform-token-path-prefix` | `/api/v1/platform/oauth2/token` | token plataforma |
| `keygo.bootstrap.platform-revoke-path-prefix` | `/api/v1/platform/oauth2/revoke` | revoke plataforma |
| `keygo.bootstrap.platform-authorize-path-prefix` | `/api/v1/platform/oauth2/authorize` | authorize plataforma |
| `keygo.bootstrap.platform-direct-login-path-prefix` | `/api/v1/platform/account/direct-login` | login API/CLI plataforma |
| `keygo.bootstrap.platform-forgot-password-path-prefix` | `/api/v1/platform/account/forgot-password` | forgot password plataforma |
| `keygo.bootstrap.platform-check-email-path-prefix` | `/api/v1/platform/account/check-email` | pre-check de email para hosted login |
| `keygo.bootstrap.platform-recover-password-path-prefix` | `/api/v1/platform/account/recover-password` | recover password plataforma |
| `keygo.bootstrap.platform-reset-password-path-prefix` | `/api/v1/platform/account/reset-password` | reset password plataforma |

### Rutas públicas por sufijo

| Propiedad | Valor actual | Uso |
|---|---|---|
| `keygo.bootstrap.service-info-path-prefix` | `/service/info` | service info |
| `keygo.bootstrap.userinfo-path-suffix` | `/userinfo` | OIDC userinfo |
| `keygo.bootstrap.revocation-path-suffix` | `/oauth2/revoke` | RFC 7009 |
| `keygo.bootstrap.register-path-suffix` | `/register` | self-registration |
| `keygo.bootstrap.verify-email-path-suffix` | `/verify-email` | verificación email |
| `keygo.bootstrap.resend-verification-path-suffix` | `/resend-verification` | reenvío código |
| `keygo.bootstrap.account-profile-path-suffix` | `/account/profile` | self-service con Bearer propio validado dentro del flujo |
| `keygo.bootstrap.account-change-password-path-suffix` | `/account/change-password` | cambio de password propio |
| `keygo.bootstrap.account-sessions-path-suffix` | `/account/sessions` | sesiones propias |
| `keygo.bootstrap.account-notification-preferences-path-suffix` | `/account/notification-preferences` | preferencias propias |
| `keygo.bootstrap.account-access-path-suffix` | `/account/access` | acceso propio |
| `keygo.bootstrap.account-reset-password-path-suffix` | `/account/reset-password` | reset de password |
| `keygo.bootstrap.account-forgot-password-path-suffix` | `/account/forgot-password` | forgot password |
| `keygo.bootstrap.account-recover-password-path-suffix` | `/account/recover-password` | recover password |
| `keygo.bootstrap.authorize-path-suffix` | `/oauth2/authorize` | inicio OAuth2 tenant |
| `keygo.bootstrap.login-path-suffix` | `/account/login` | login tenant |
| `keygo.bootstrap.token-path-suffix` | `/oauth2/token` | token tenant |
| `keygo.bootstrap.billing-catalog-path-suffix` | `/billing/catalog` | catálogo público |
| `keygo.bootstrap.billing-contracts-path-suffix` | `/billing/contracts` | onboarding billing |

## Configuración relevante

En `application.yml`:

```yaml
keygo:
  bootstrap:
    enabled: true
    bypass-roles: ${KEYGO_BOOTSTRAP_BYPASS_ROLES:ADMIN,ADMIN_TENANT,USER,KEYGO_ADMIN,KEYGO_TENANT_ADMIN,KEYGO_USER}
    api-path-prefix: "/api/"
```

### Overrides útiles

- Desactivar completamente el filtro:

```bash
export KEYGO_BOOTSTRAP_ENABLED=false
```

- Ajustar los roles que el filtro reconoce como bypass:

```bash
export KEYGO_BOOTSTRAP_BYPASS_ROLES=ADMIN,ADMIN_TENANT,KEYGO_ADMIN,KEYGO_TENANT_ADMIN
```

## Notas de implementación

- El matching debe usar `request.getServletPath()`, no `getRequestURI()`.
- Para rutas con subpaths se usa lógica por segmento, no solo `endsWith`.
- El filtro autentica; la autorización final por endpoint la hace Spring Security con `@PreAuthorize`.
- El alcance tenant para roles acotados se valida fuera del filtro, en evaluadores de autorización.

## Pruebas y verificación

```bash
./mvnw -pl keygo-run test
./docs/scripts/test-service-info.sh
./docs/scripts/test-response-codes.sh
```

## Referencias

- [`../../README.md`](../../README.md)
- [`../architecture.md`](../architecture.md)
- [`../../02-functional/authentication-flow.md`](../../02-functional/authentication-flow.md)
- [`../../08-reference/api/README.md`](../../08-reference/api/README.md)
