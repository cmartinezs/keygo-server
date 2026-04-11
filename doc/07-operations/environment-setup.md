# Configuración de entornos - KeyGo Server

Fuente de verdad para ambiente local, `.env` y scripts operativos.

## Flujo recomendado

### 1. Seleccionar ambiente

```bash
./docs/scripts/switch-env.sh local
```

Esto copia `envs/.env-local` a `.env` en la raíz del proyecto.

### 2. Cargar variables en la shell

```bash
set -a; source .env; set +a
```

### 3. Levantar la base local

```bash
./docs/scripts/db/start.sh
```

### 4. Ejecutar migraciones

```bash
./docs/scripts/db/migrate.sh
```

### 5. Ejecutar la app

```bash
./mvnw spring-boot:run -pl keygo-run
```

## Estructura de archivos `.env`

```text
envs/
├── .env.example
├── .env-local
├── .env-desa
└── .env-prod

.env                # ambiente activo en la raíz del repo
```

Regla: `docs/scripts/switch-env.sh` siempre opera sobre `envs/.env-*` y deja el ambiente activo en `.env` en la raíz.

## Variables más relevantes

| Variable | Uso |
|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfiles activos, por ejemplo `supabase,local` |
| `SUPABASE_URL` | JDBC URL PostgreSQL |
| `SUPABASE_USER` | Usuario DB |
| `SUPABASE_PASSWORD` | Password DB |
| `PORT` | Puerto HTTP |
| `KEYGO_ISSUER_BASE_URL` | Issuer base URL |
| `KEYGO_CORS_ALLOWED_ORIGINS` | Orígenes CORS permitidos |
| `KEYGO_UI_BASE_URL` | URL base del frontend |
| `KEYGO_PLATFORM_REDIRECT_URI` | Redirect URI de plataforma |
| `SMTP_HOST` / `SMTP_PORT` | SMTP |
| `SMTP_USERNAME` / `SMTP_PASSWORD` | Credenciales SMTP |
| `KEYGO_MAIL_FROM` | Remitente |
| `KEYGO_MAIL_APP_NAME` | Nombre de app en correos |
| `KEYGO_BOOTSTRAP_ENABLED` | Override para desactivar el filtro bootstrap |
| `KEYGO_BOOTSTRAP_BYPASS_ROLES` | Override del set de roles que el filtro reconoce |

## Nota de seguridad

- La autenticación admin vigente es Bearer JWT.
- `KEYGO_ADMIN_KEY` y `X-KEYGO-ADMIN` son legacy y no deben documentarse como mecanismo actual.

## Script all-in-one

Si quieres preparar todo en una sola pasada:

```bash
./docs/scripts/quick-start.sh
```

Este script:

1. inicia la DB local,
2. carga variables desde `.env` si existe,
3. corre migraciones,
4. compila el proyecto.

## IntelliJ / IDE

- El archivo recomendable para EnvFile es `.env` en la raíz del repo.
- La configuración IDE específica se considera secundaria y no tiene guía separada en el canon activo.

## Referencias

- [`../../README.md`](../../README.md)
- [`../03-architecture/security/bootstrap-filter.md`](../03-architecture/security/bootstrap-filter.md)
- [`../08-reference/data/migrations.md`](../08-reference/data/migrations.md)
