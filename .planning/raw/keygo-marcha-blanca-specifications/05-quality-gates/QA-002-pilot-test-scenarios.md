# QA-002 — Escenarios de prueba para piloto guiado KeyGo

| Campo | Valor |
|---|---|
| Tipo | Test scenarios |
| Prioridad | P0/P1 |
| Área | Frontend y Backend |
| Objetivo | Validar comportamiento funcional extremo a extremo antes de marcha blanca |

## Datos base sugeridos

- Tenant: `acme`
- Admin global: `KEYGO_ADMIN`
- Admin tenant: `KEYGO_ACCOUNT_ADMIN`
- Usuario final: `KEYGO_USER`
- App pública: `Portal Clientes`
- App confidential: `Backoffice API`
- Roles de app: `ADMIN`, `USER`, `VIEWER`

## Escenario 1 — Crear app pública integrable

### Pasos

1. Entrar como `KEYGO_ACCOUNT_ADMIN`.
2. Crear app `Portal Clientes` tipo `PUBLIC`.
3. Habilitar `AUTHORIZATION_CODE` + PKCE.
4. Agregar redirect URI `https://cliente.cl/callback`.
5. Seleccionar scopes `openid`, `profile`, `email`.
6. Guardar.
7. Copiar secret/client id si corresponde según tipo.

### Resultado esperado

- App creada.
- `client_id` visible.
- Si backend devuelve secret, UI lo muestra una sola vez.
- La app queda lista para iniciar Authorization Code.

## Escenario 2 — Bloquear app sin redirect URI

### Pasos

1. Crear app pública con `AUTHORIZATION_CODE`.
2. No ingresar redirect URI.
3. Intentar guardar.

### Resultado esperado

- UI bloquea por validación local o backend responde 400.
- Mensaje claro: Authorization Code requiere redirect URI.

## Escenario 3 — Crear roles de app

### Pasos

1. Entrar a detalle de `Portal Clientes`.
2. Abrir tab `Roles`.
3. Crear rol `USER`.
4. Crear rol `VIEWER`.
5. Listar roles.

### Resultado esperado

- Roles aparecen con código y nombre legible.
- No se muestran solo UUIDs.

## Escenario 4 — Crear usuario y asignar app

### Pasos

1. Crear usuario `ana@empresa.cl`.
2. Ir a memberships.
3. Buscar usuario `ana`.
4. Buscar app `Portal`.
5. Asignar membership con rol `USER`.

### Resultado esperado

- Membership queda `ACTIVE` o estado definido por política.
- Se muestra app, estado, rol legible y fecha válida.

## Escenario 5 — Login de usuario con membership

### Pasos

1. Iniciar login desde app `Portal Clientes`.
2. Autenticarse como `ana@empresa.cl`.
3. Completar Authorization Code + token exchange.
4. Validar access token.

### Resultado esperado

- Token válido.
- Claims incluyen `sub`, `tid`, `cid`, `roles`, `scp/scopes`.
- `roles` contiene `USER`.

## Escenario 6 — Denegar usuario sin membership

### Pasos

1. Crear usuario `bob@empresa.cl` sin membership para `Portal Clientes`.
2. Intentar login desde app `Portal Clientes`.

### Resultado esperado

- No recibe token para esa app si política es cerrada.
- UI muestra pantalla “No tienes acceso a esta aplicación”.
- Código funcional esperado: `KG-NO-MEMBERSHIP`.

## Escenario 7 — Validar roles administrativos

### Pasos

1. Entrar como `KEYGO_ACCOUNT_ADMIN`.
2. Acceder a apps, users, memberships y roles.
3. Entrar como `KEYGO_USER`.
4. Intentar acceder a las mismas rutas administrativas.

### Resultado esperado

- `KEYGO_ACCOUNT_ADMIN` puede operar sin 403 inesperado.
- `KEYGO_USER` no puede acceder a tenant console.

## Escenario 8 — Desactivar mocks

### Pasos

1. Ejecutar frontend con `VITE_ENABLE_MSW=false`.
2. Crear app.
3. Crear usuario.
4. Crear membership.
5. Login.

### Resultado esperado

- Network tab muestra llamadas reales a backend.
- No hay handlers MSW interceptando flujos centrales.

## Escenario 9 — Suspender y activar usuario

### Pasos

1. Suspender usuario activo.
2. Verificar estado visible.
3. Intentar suspender de nuevo.
4. Activar usuario.
5. Intentar activar de nuevo.

### Resultado esperado

- Estados y mensajes son coherentes con contrato real.
- No hay error por campos inexistentes en frontend.

## Escenario 10 — Logout/revoke

### Pasos

1. Iniciar sesión.
2. Ejecutar logout desde UI.
3. Verificar llamada a backend de logout/revoke.
4. Intentar usar refresh token revocado.

### Resultado esperado

- UI queda deslogueada.
- Refresh token no permite renovar sesión.
- No depende de mock.
