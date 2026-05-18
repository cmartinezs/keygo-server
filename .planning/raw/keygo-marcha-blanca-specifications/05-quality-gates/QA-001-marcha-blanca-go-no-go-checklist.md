# QA-001 — Checklist GO/NO-GO para marcha blanca KeyGo

| Campo | Valor |
|---|---|
| Tipo | Quality gate |
| Prioridad | P0 |
| Área | Frontend y Backend |
| Objetivo | Validar si KeyGo está listo para piloto guiado |

## Checklist GO

Marcar cada punto antes de habilitar marcha blanca.

### Apps

- [ ] Crear app muestra `client_id` y `client_secret` una sola vez.
- [ ] El secret se puede copiar.
- [ ] La UI advierte que el secret no volverá a mostrarse.
- [ ] La app permite configurar redirect URIs.
- [ ] La app permite configurar scopes base.
- [ ] Backend rechaza Authorization Code sin redirect URI.
- [ ] Existe detalle de app.
- [ ] Existe gestión básica de roles por app.

### Roles administrativos

- [ ] Backend usa `KEYGO_ACCOUNT_ADMIN` como rol tenant/account admin.
- [ ] Frontend usa la misma nomenclatura.
- [ ] Un usuario `KEYGO_ACCOUNT_ADMIN` puede operar tenant console sin 403 inesperado.
- [ ] Un usuario `KEYGO_USER` no puede operar tenant console.

### Memberships

- [ ] Membership muestra app asociada.
- [ ] Membership muestra estado.
- [ ] Membership muestra roles legibles, no UUIDs.
- [ ] Membership muestra `created_at` válido.
- [ ] Crear membership funciona para usuario y app del mismo tenant.
- [ ] Revocar membership funciona si está expuesto.

### Tokens

- [ ] Access token incluye `sub`.
- [ ] Access token incluye `tid`.
- [ ] Access token incluye `cid`.
- [ ] Access token incluye roles de app desde membership.
- [ ] Access token incluye scopes (`scp` o `scopes`).
- [ ] Token valida contra JWKS.

### Mocks

- [ ] MSW está desactivado por defecto para marcha blanca.
- [ ] Login no usa mock.
- [ ] Apps no usan mock.
- [ ] Users no usan mock.
- [ ] Memberships no usan mock.
- [ ] Roles no usan mock.
- [ ] Logout/revoke no usa mock o está explícitamente documentado como pendiente no bloqueante.

## Condiciones NO-GO

La marcha blanca no debe habilitarse si ocurre cualquiera de estos puntos:

- [ ] El secret se pierde al crear app.
- [ ] `KEYGO_ACCOUNT_ADMIN` recibe 403 por inconsistencia de roles.
- [ ] Memberships muestran UUIDs como roles visibles.
- [ ] Memberships tienen fechas nulas o inválidas.
- [ ] No se pueden configurar roles de app desde UI.
- [ ] Los flujos centrales dependen de MSW sin documentarlo.
- [ ] Una app Authorization Code puede quedar sin redirect URI.
- [ ] Un usuario sin membership activa recibe token para app cerrada.

## Resultado

```text
Estado: GO / NO-GO
Fecha:
Responsable:
Observaciones:
```
