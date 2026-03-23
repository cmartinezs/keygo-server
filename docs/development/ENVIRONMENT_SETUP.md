# Configuración de entornos — KeyGo Server

> **Última actualización:** 2026-03-23  
> Fusiona: `ENVIRONMENT_STRATEGY.md` y `ENV_QUICK_REFERENCE.md` de `docs/keygo-supabase/`

---

## 1. Variables de entorno requeridas

### Mínimo para arrancar sin DB

```bash
export KEYGO_ADMIN_KEY="changeMe"          # clave del header X-KEYGO-ADMIN (dev)
# SPRING_PROFILES_ACTIVE no se necesita si solo se usa el perfil default
```

### Con base de datos (perfil `supabase`)

```bash
export SPRING_PROFILES_ACTIVE="supabase,local"
export SUPABASE_URL="jdbc:postgresql://localhost:5432/keygo"
export SUPABASE_USER="postgres"
export SUPABASE_PASSWORD="postgres"
export KEYGO_ADMIN_KEY="changeMe"
```

### Con envío de email (registro + verificación)

```bash
# Opción A: MailHog local (sin autenticación)
export SMTP_HOST=localhost
export SMTP_PORT=1025
# UI de MailHog → http://localhost:8025
# docker run -p 1025:1025 -p 8025:8025 mailhog/mailhog

# Opción B: Mailtrap (sandbox cloud)
export SMTP_HOST=sandbox.smtp.mailtrap.io
export SMTP_PORT=587
export SMTP_USERNAME=tu-usuario-mailtrap
export SMTP_PASSWORD=tu-password-mailtrap

# Sender y nombre de la app
export KEYGO_MAIL_FROM=noreply@keygo.local
export KEYGO_MAIL_APP_NAME=KeyGo
```

### Tabla completa de variables

| Variable | Requerida | Default dev | Descripción |
|---|---|---|---|
| `KEYGO_ADMIN_KEY` | Si `bootstrap.enabled=true` | `changeMe` | Header `X-KEYGO-ADMIN` |
| `KEYGO_ISSUER_BASE_URL` | No | `http://localhost:8080/keygo-server` | URL base del emisor OAuth2 (claim `iss` en JWT) |
| `SPRING_PROFILES_ACTIVE` | No | `default` | Ej: `supabase,local` |
| `PORT` | No | `8080` | Puerto HTTP del servidor |
| `SUPABASE_URL` | Solo perfil `supabase` | — | JDBC PostgreSQL URL |
| `SUPABASE_USER` | Solo perfil `supabase` | `postgres` | Usuario de BD |
| `SUPABASE_PASSWORD` | Solo perfil `supabase` | `postgres` | Password de BD |
| `SUPABASE_DB_HOST` | No | `localhost` | Host de la BD |
| `SUPABASE_DB_PORT` | No | `5432` | Puerto de la BD |
| `SUPABASE_DB_NAME` | No | `keygo` | Nombre de la BD |
| `SMTP_HOST` | Solo si se usa email | `localhost` | Host del servidor SMTP |
| `SMTP_PORT` | No | `587` | Puerto SMTP (587=STARTTLS, 1025=MailHog) |
| `SMTP_USERNAME` | Solo si SMTP requiere auth | `""` | Usuario SMTP |
| `SMTP_PASSWORD` | Solo si SMTP requiere auth | `""` | Contraseña / app password SMTP |
| `KEYGO_MAIL_FROM` | No | `noreply@keygo.example.com` | Dirección remitente de emails |
| `KEYGO_MAIL_APP_NAME` | No | `KeyGo` | Nombre de la app en emails |

> ⚠️ **Nunca commitear credenciales.** Usar siempre variables de entorno o archivos `.env` en `.gitignore`.

---

## 2. Estructura de archivos `.env` (keygo-supabase)

```
keygo-supabase/
├── .env            # ⚠️ Ambiente activo (git ignored)
├── .env.example    # ✅ Template con todas las variables (committed)
├── .env-local      # ⚠️ Config para Docker local (git ignored)
├── .env-desa       # ⚠️ Config para desarrollo/staging (git ignored)
├── .env-prod       # ⚠️ Config para producción (git ignored)
└── scripts/
    └── switch-env.sh
```

### Setup inicial

```bash
cd keygo-supabase
cp .env.example .env-local
cp .env.example .env-desa
cp .env.example .env-prod

# Editar cada archivo con los valores correctos
# .env-local → apunta a Docker local (localhost:5432)
# .env-desa  → apunta a instancia Supabase de desarrollo
# .env-prod  → apunta a instancia Supabase de producción
```

---

## 3. Cambiar entre ambientes

```bash
cd keygo-supabase

./scripts/switch-env.sh local   # activa .env-local
./scripts/switch-env.sh desa    # activa .env-desa
./scripts/switch-env.sh prod    # activa .env-prod
./scripts/switch-env.sh list    # lista ambientes disponibles
```

El script copia el archivo elegido a `.env`, que es el que IntelliJ (EnvFile) y los scripts de Flyway leen.

---

## 4. Arrancar la base de datos local (Docker)

```bash
# Levantar Postgres + PgAdmin
cd keygo-supabase && ./scripts/dev-start.sh

# Detener
cd keygo-supabase && ./scripts/dev-stop.sh
```

Servicios:
- **Postgres 15** → `localhost:5432` (BD: `keygo`, user: `postgres`, pass: `postgres`)
- **PgAdmin 4** → `http://localhost:5050`

---

## 5. Script de inicio rápido

```bash
# Desde la raíz del repo — levanta DB + exporta variables + corre la app
./scripts/quick-start.sh
```

---

## 6. Cargar variables en IntelliJ IDEA

### Opción A: manualmente en el runner

En la configuración de Spring Boot (Run → Edit Configurations...):
- Campo **Environment variables**: pegar las variables del paso 1

### Opción B: plugin EnvFile (recomendado)

1. Instalar plugin **EnvFile** (Borys Pierov) desde Marketplace
2. En la configuración del runner → pestaña **EnvFile**:
   - ✅ Enable EnvFile
   - Agregar `keygo-supabase/.env`
   - ✅ Substitute environment variables
3. Usar `./scripts/switch-env.sh local` antes de arrancar

Ver detalles en [`INTELLIJ_SETUP.md`](INTELLIJ_SETUP.md).

---

## 7. Verificación de la configuración activa

```bash
# Ver qué perfil está activo al arrancar
grep "profiles" keygo-run/src/main/resources/application.yml

# Ver el ambiente del .env activo
grep "^KEYGO_ENV\|^SPRING_PROFILES" keygo-supabase/.env 2>/dev/null || echo "No hay .env activo"

# Verificar que el servidor responde
curl http://localhost:8080/keygo-server/actuator/health
```

---

## 8. Configuración en `application.yml` (keygo-run)

```yaml
server:
  port: "${PORT:8080}"
  servlet:
    context-path: "/${keygo.info.name}"   # → /keygo-server

keygo:
  bootstrap:
    enabled: true
    admin-key: "${KEYGO_ADMIN_KEY:changeMe}"
    api-path-prefix: "/api/"
    actuator-path-prefix: "/actuator/"
    well-known-path-prefix: "/.well-known"
    swagger-ui-path-prefix: "/swagger-ui"
    api-docs-path-prefix: "/v3/api-docs"
    userinfo-path-suffix: "/userinfo"
    revocation-path-suffix: "/oauth2/revoke"
    register-path-suffix: "/register"
    verify-email-path-suffix: "/verify-email"
    resend-verification-path-suffix: "/resend-verification"
  info:
    issuer-base-url: "${KEYGO_ISSUER_BASE_URL:http://localhost:8080/keygo-server}"
  mail:
    from: "${KEYGO_MAIL_FROM:noreply@keygo.example.com}"
    app-name: "${KEYGO_MAIL_APP_NAME:KeyGo}"

spring:
  mail:
    host: "${SMTP_HOST:localhost}"
    port: "${SMTP_PORT:587}"
    username: "${SMTP_USERNAME:}"
    password: "${SMTP_PASSWORD:}"
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
```

### Perfil `supabase` (`application-supabase.yml` en keygo-supabase)

```yaml
spring:
  datasource:
    url: "${SUPABASE_URL}"
    username: "${SUPABASE_USER}"
    password: "${SUPABASE_PASSWORD}"
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration
```

---

## 9. Variables en CI/CD

En GitHub Actions (o equivalente), declarar como secrets:

```yaml
env:
  KEYGO_ADMIN_KEY: ${{ secrets.KEYGO_ADMIN_KEY }}
  KEYGO_ISSUER_BASE_URL: ${{ secrets.KEYGO_ISSUER_BASE_URL }}
  SUPABASE_URL: ${{ secrets.SUPABASE_URL }}
  SUPABASE_USER: ${{ secrets.SUPABASE_USER }}
  SUPABASE_PASSWORD: ${{ secrets.SUPABASE_PASSWORD }}
  SPRING_PROFILES_ACTIVE: "supabase"
  # Email / SMTP (usar servicio transaccional en CI)
  SMTP_HOST: ${{ secrets.SMTP_HOST }}
  SMTP_PORT: "587"
  SMTP_USERNAME: ${{ secrets.SMTP_USERNAME }}
  SMTP_PASSWORD: ${{ secrets.SMTP_PASSWORD }}
  KEYGO_MAIL_FROM: ${{ secrets.KEYGO_MAIL_FROM }}
  KEYGO_MAIL_APP_NAME: "KeyGo"
```

---

## Referencias

- [`docs/development/INTELLIJ_SETUP.md`](INTELLIJ_SETUP.md) — configuración completa del IDE
- [`docs/data/MIGRATIONS.md`](../data/MIGRATIONS.md) — migraciones Flyway
- [`docs/api/BOOTSTRAP_FILTER.md`](../api/BOOTSTRAP_FILTER.md) — filtro de seguridad

