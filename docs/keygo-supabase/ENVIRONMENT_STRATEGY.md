# Environment Strategy Guide
# Guía de Estrategia de Ambientes

## 📋 Overview / Resumen

Este documento describe la estrategia para gestionar múltiples ambientes (local, desarrollo, producción) en KeyGo Supabase utilizando archivos `.env` y variables de entorno.

This document describes the strategy for managing multiple environments (local, development, production) in KeyGo Supabase using `.env` files and environment variables.

---

## 🎯 Objetivos / Objectives

1. **Separación clara** entre ambientes
2. **Configuración desde variables de entorno** únicamente
3. **Fácil cambio** entre ambientes
4. **Seguridad** - nunca commitear credenciales
5. **Compatible con IDE** - cargar automáticamente desde `.env`

---

## 📁 Estructura de Archivos / File Structure

```
keygo-supabase/
├── .env                    # ⚠️ Active environment (git ignored)
├── .env.example            # ✅ Template (committed to git)
├── .env-local              # ⚠️ Local config (git ignored)
├── .env-desa               # ⚠️ Development config (git ignored)
├── .env-prod               # ⚠️ Production config (git ignored)
└── scripts/
    └── switch-env.sh       # ✅ Environment switcher script
```

### Files Explanation / Explicación de Archivos

| File | Purpose | Git |
|------|---------|-----|
| `.env` | Currently active environment configuration | ❌ Ignored |
| `.env.example` | Template with all available variables | ✅ Committed |
| `.env-local` | Local development configuration | ❌ Ignored |
| `.env-desa` | Development/Staging configuration | ❌ Ignored |
| `.env-prod` | Production configuration | ❌ Ignored |

---

## 🚀 Quick Start / Inicio Rápido

### 1. Initial Setup / Configuración Inicial

```bash
cd keygo-supabase

# Copy example to create environment files
cp .env.example .env-local
cp .env.example .env-desa
cp .env.example .env-prod

# Edit each file with appropriate values
vim .env-local   # Configure for local Docker
vim .env-desa    # Configure for Supabase development instance
vim .env-prod    # Configure for Supabase production instance
```

### 2. Switch to Environment / Cambiar a un Ambiente

```bash
# Switch to local environment
./scripts/switch-env.sh local

# Switch to development environment
./scripts/switch-env.sh desa

# Switch to production environment
./scripts/switch-env.sh prod

# List available environments
./scripts/switch-env.sh list
```

### 3. Load in IDE / Cargar en el IDE

#### IntelliJ IDEA

1. Install **EnvFile** plugin:
   - Go to `Settings/Preferences` → `Plugins`
   - Search for "EnvFile"
   - Install and restart IDE

2. Configure Run Configuration:
   - Go to `Run` → `Edit Configurations`
   - Select your Spring Boot configuration
   - Go to `EnvFile` tab
   - Enable "Enable EnvFile"
   - Add `.env` file from `keygo-supabase` directory
   - Apply and OK

#### VS Code

1. Install **DotENV** extension:
   - Open Extensions (`Ctrl+Shift+X`)
   - Search for "DotENV"
   - Install

2. The extension will automatically load `.env` files

#### Eclipse

1. Install **Dotenv** plugin from Eclipse Marketplace

2. Configure in Run Configuration:
   - Right-click project → `Run As` → `Run Configurations`
   - Select your configuration
   - Go to `Environment` tab
   - Click `Import...` → Select `.env` file

---

## 🔧 Environment Variables / Variables de Entorno

### Core Database Configuration

| Variable | Description | Example |
|----------|-------------|---------|
| `SUPABASE_URL` | JDBC connection URL | `jdbc:postgresql://localhost:5432/keygo` |
| `SUPABASE_USER` | Database user | `postgres` |
| `SUPABASE_PASSWORD` | Database password | `your-secure-password` |
| `SUPABASE_DB_HOST` | Database host | `localhost` |
| `SUPABASE_DB_PORT` | Database port | `5432` |
| `SUPABASE_DB_NAME` | Database name | `keygo` |

### Supabase Project Configuration

| Variable | Description | Example |
|----------|-------------|---------|
| `SUPABASE_PROJECT_ID` | Project ID | `xxxxxxxxxxxxx` |
| `SUPABASE_API_URL` | Main API URL | `https://xxx.supabase.co` |
| `SUPABASE_REST_URL` | REST API URL | `https://xxx.supabase.co/rest/v1` |
| `SUPABASE_GRAPHQL_URL` | GraphQL URL | `https://xxx.supabase.co/graphql/v1` |
| `SUPABASE_REALTIME_URL` | Realtime WebSocket | `wss://xxx.supabase.co/realtime/v1` |
| `SUPABASE_STORAGE_URL` | Storage API | `https://xxx.supabase.co/storage/v1` |

### Authentication Keys

| Variable | Description | Security |
|----------|-------------|----------|
| `SUPABASE_ANON_KEY` | Public anonymous key | ⚠️ Public - Safe to expose |
| `SUPABASE_SERVICE_KEY` | Service role key | 🔒 Secret - NEVER expose! |
| `SUPABASE_JWT_SECRET` | JWT signing secret | 🔒 Secret - Keep secure! |

### Application Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `KEYGO_ENV` | Environment identifier | `local` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profiles | `supabase,local` |
| `SERVER_PORT` | Server port | `8080` |
| `SERVER_CONTEXT_PATH` | Context path | `/keygo-server` |
| `LOG_LEVEL` | Root log level | `INFO` |
| `LOG_LEVEL_KEYGO` | KeyGo log level | `DEBUG` |

---

## 🔄 Workflow / Flujo de Trabajo

### Daily Development / Desarrollo Diario

```bash
# 1. Start your day - check current environment
cd keygo-supabase
./scripts/switch-env.sh list

# 2. Switch to local for development
./scripts/switch-env.sh local

# 3. Start local database
./scripts/dev-start.sh

# 4. Run migrations
./scripts/migrate.sh

# 5. Start application from IDE
# (IDE will automatically load .env)

# 6. Develop and test...

# 7. When done, stop database
./scripts/dev-stop.sh
```

### Testing in Development Environment / Pruebas en Ambiente de Desarrollo

```bash
# 1. Switch to development environment
./scripts/switch-env.sh desa

# 2. Verify configuration
cat .env | grep SUPABASE_DB_HOST

# 3. Test database connection
./scripts/info.sh

# 4. Run migrations if needed
./scripts/migrate.sh

# 5. Start application from IDE
# (IDE loads .env-desa through .env)

# 6. Test features...

# 7. Switch back to local
./scripts/switch-env.sh local
```

### Production Deployment / Despliegue a Producción

```bash
# ⚠️ CAREFUL - PRODUCTION!

# 1. Switch to production environment
./scripts/switch-env.sh prod

# 2. BACKUP DATABASE FIRST!
# (Use Supabase dashboard or pg_dump)

# 3. Verify configuration
./scripts/info.sh

# 4. Run migrations
./scripts/migrate.sh

# 5. Verify migrations applied correctly
./scripts/info.sh

# 6. Test application

# 7. Switch back to local
./scripts/switch-env.sh local
```

---

## 🛡️ Security Best Practices / Mejores Prácticas de Seguridad

### ✅ DO / HACER

1. **Always use `.env` files** for configuration
   - ✅ Never hardcode credentials in code

2. **Keep `.env` files out of git**
   - ✅ Already configured in `.gitignore`

3. **Use strong passwords** for production
   - ✅ At least 32 characters with special chars

4. **Rotate credentials regularly**
   - ✅ Change passwords every 90 days

5. **Use different credentials** per environment
   - ✅ Never reuse production credentials in dev/local

6. **Backup before production changes**
   - ✅ Always backup database before migrations

7. **Review `.env` before committing**
   - ✅ Double-check no secrets are accidentally added

### ❌ DON'T / NO HACER

1. **Never commit `.env` files to git**
   - ❌ Even accidentally

2. **Never share credentials** via Slack/Email
   - ❌ Use secure password managers instead

3. **Never use production DB** for development
   - ❌ Always use separate instances

4. **Never expose service keys** in client code
   - ❌ Only use anon keys in frontend

5. **Never skip backups**
   - ❌ Always backup before migrations

---

## 📝 Environment Configuration Examples / Ejemplos de Configuración

### Local Environment (.env-local)

```bash
# Local Docker PostgreSQL
KEYGO_ENV=local
SPRING_PROFILES_ACTIVE=supabase,local

SUPABASE_URL=jdbc:postgresql://localhost:5432/keygo
SUPABASE_USER=postgres
SUPABASE_PASSWORD=postgres
SUPABASE_DB_HOST=localhost
SUPABASE_DB_PORT=5432
SUPABASE_DB_NAME=keygo

# Local mock keys
SUPABASE_ANON_KEY=demo-anon-key
SUPABASE_SERVICE_KEY=demo-service-key
SUPABASE_JWT_SECRET=local-jwt-secret

# Development settings
LOG_LEVEL=DEBUG
SWAGGER_ENABLED=true
```

### Development Environment (.env-desa)

```bash
# Supabase Development Instance
KEYGO_ENV=desa
SPRING_PROFILES_ACTIVE=supabase,desa

SUPABASE_URL=jdbc:postgresql://db.xxxxx.supabase.co:6543/postgres
SUPABASE_USER=postgres
SUPABASE_PASSWORD=<from-supabase-dashboard>
SUPABASE_PROJECT_ID=xxxxx

# Supabase URLs
SUPABASE_API_URL=https://xxxxx.supabase.co
SUPABASE_REST_URL=https://xxxxx.supabase.co/rest/v1

# Real Supabase keys (from dashboard)
SUPABASE_ANON_KEY=<from-supabase-dashboard>
SUPABASE_SERVICE_KEY=<from-supabase-dashboard>
SUPABASE_JWT_SECRET=<from-supabase-dashboard>

# Staging settings
LOG_LEVEL=INFO
SWAGGER_ENABLED=true
```

### Production Environment (.env-prod)

```bash
# Supabase Production Instance
KEYGO_ENV=prod
SPRING_PROFILES_ACTIVE=supabase,prod

SUPABASE_URL=jdbc:postgresql://db.yyyyy.supabase.co:6543/postgres
SUPABASE_USER=postgres
SUPABASE_PASSWORD=<strong-production-password>
SUPABASE_PROJECT_ID=yyyyy

# Supabase URLs
SUPABASE_API_URL=https://yyyyy.supabase.co
SUPABASE_REST_URL=https://yyyyy.supabase.co/rest/v1

# Production Supabase keys
SUPABASE_ANON_KEY=<production-anon-key>
SUPABASE_SERVICE_KEY=<production-service-key>
SUPABASE_JWT_SECRET=<production-jwt-secret>

# Production settings
LOG_LEVEL=WARN
SWAGGER_ENABLED=false
FLYWAY_BASELINE_ON_MIGRATE=false
```

---

## 🔍 Troubleshooting / Solución de Problemas

### Issue: Variables not loading in IDE

**Solution:**
1. Verify `.env` file exists in `keygo-supabase/` directory
2. Check IDE plugin is installed and enabled
3. Restart IDE after changing environment
4. Verify file permissions: `chmod 644 .env`

### Issue: Wrong environment loaded

**Solution:**
```bash
# Check current environment
cat keygo-supabase/.env | head -n 5

# Switch to correct environment
cd keygo-supabase
./scripts/switch-env.sh local  # or desa, prod
```

### Issue: Database connection fails

**Solution:**
```bash
# Verify database is running (local)
docker ps | grep keygo

# Test database connection
psql "${SUPABASE_CONNECTION_STRING}"

# Check environment variables
source .env
echo $SUPABASE_URL
```

### Issue: Migrations fail

**Solution:**
```bash
# Check migration status
./scripts/info.sh

# Repair if needed
./scripts/repair.sh

# Validate configuration
./scripts/validate.sh
```

---

## 📚 Additional Resources / Recursos Adicionales

- [Supabase Documentation](https://supabase.com/docs)
- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [12 Factor App - Config](https://12factor.net/config)

---

## ✅ Checklist / Lista de Verificación

Before committing code / Antes de commitear código:

- [ ] No `.env` files in git status
- [ ] `.env.example` is up to date
- [ ] All new variables documented
- [ ] No hardcoded credentials in code
- [ ] Tested in local environment
- [ ] Migrations tested

Before deploying to production / Antes de desplegar a producción:

- [ ] Backup database
- [ ] Test in desa environment first
- [ ] Review migration scripts
- [ ] Update production credentials
- [ ] Verify `.env-prod` configuration
- [ ] Plan rollback strategy

---

**Last Updated:** 2026-03-15  
**Version:** 1.0

