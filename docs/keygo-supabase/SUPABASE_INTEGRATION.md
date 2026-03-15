# KeyGo + Supabase Integration Guide
# Guía de Integración de KeyGo + Supabase

## 🎯 Overview / Resumen

This document provides a complete guide for the KeyGo Supabase integration.

Este documento proporciona una guía completa para la integración de KeyGo con Supabase.

---

## ✅ Integration Complete / Integración Completa

The `keygo-supabase` module has been successfully integrated into the KeyGo application:

- ✅ Module added to parent POM
- ✅ Dependency added to `keygo-run`
- ✅ Component scanning configured
- ✅ Spring profiles configured
- ✅ Full compilation successful

---

## 🚀 Quick Start / Inicio Rápido

### Option 1: Automated Setup (Recommended)

```bash
# Run the quick-start script
./quick-start.sh
```

This script will:
1. Start local PostgreSQL database
2. Set environment variables
3. Run database migrations
4. Build the application
5. Show migration status

### Option 2: Manual Setup

```bash
# 1. Start local database
cd keygo-supabase
./scripts/dev-start.sh

# 2. Set environment variables
export SUPABASE_URL=jdbc:postgresql://localhost:5432/keygo
export SUPABASE_USER=postgres
export SUPABASE_PASSWORD=postgres

# 3. Run migrations
./scripts/migrate.sh
cd ..

# 4. Build the application
./mvnw clean package

# 5. Run the application
./mvnw spring-boot:run -pl keygo-run
```

---

## 📦 Project Structure / Estructura del Proyecto

```
keygo-server/
├── keygo-common/          # Common utilities
├── keygo-domain/          # Domain entities
├── keygo-app/             # Application layer
├── keygo-infra/           # Infrastructure
├── keygo-api/             # REST API
├── keygo-supabase/        # ✅ NEW: Supabase integration
│   ├── scripts/           # Management scripts (8 files)
│   ├── src/main/
│   │   ├── java/          # Java entities, repositories, config
│   │   └── resources/     # Flyway migrations
│   └── docs/              # Documentation
├── keygo-run/             # Runtime module (✅ Updated)
├── keygo-bom/             # Bill of materials
└── quick-start.sh         # ✅ NEW: Quick start script
```

---

## 🔧 Configuration / Configuración

### Environment Variables / Variables de Entorno

```bash
# Required for database connection
export SUPABASE_URL=jdbc:postgresql://localhost:5432/keygo
export SUPABASE_USER=postgres
export SUPABASE_PASSWORD=postgres

# Optional: For Supabase API
export SUPABASE_API_URL=https://your-project.supabase.co
export SUPABASE_ANON_KEY=your-anon-key
export SUPABASE_SERVICE_KEY=your-service-key
```

### Application Profiles / Perfiles de Aplicación

The application now includes the `supabase` profile by default:

```yaml
spring:
  profiles:
    active: "${SPRING_PROFILES_ACTIVE:default}"
    include:
      - supabase
```

---

## 🗄️ Database / Base de Datos

### Local Development / Desarrollo Local

A PostgreSQL database is provided via Docker Compose:

```bash
# Start
cd keygo-supabase
./scripts/dev-start.sh

# Stop
./scripts/dev-stop.sh
```

**Connection Details:**
- Host: `localhost`
- Port: `5432`
- Database: `keygo`
- User: `postgres`
- Password: `postgres`

### PgAdmin

Access PgAdmin at http://localhost:5050

**Credentials:**
- Email: `admin@keygo.local`
- Password: `admin`

### Production with Supabase

For production deployments:

```bash
# Set Supabase production credentials
export SUPABASE_URL=postgresql://postgres:[PASSWORD]@db.[PROJECT].supabase.co:5432/postgres
export SUPABASE_USER=postgres
export SUPABASE_PASSWORD=your-production-password

# Run migrations
cd keygo-supabase
./scripts/setup-supabase.sh
```

---

## 📊 Database Schema / Esquema de Base de Datos

The following tables are created automatically by migrations:

| Table | Purpose |
|-------|---------|
| `users` | User accounts |
| `roles` | System roles (ADMIN, USER, MANAGER, GUEST) |
| `permissions` | System permissions |
| `user_roles` | User-role assignments |
| `role_permissions` | Role-permission assignments |
| `sessions` | Active user sessions |
| `audit_logs` | Audit trail |
| `oauth_providers` | OAuth provider configurations |
| `oauth_tokens` | OAuth tokens for users |

---

## 🔐 Default Credentials / Credenciales por Defecto

### Admin User

```
Username: admin
Email: admin@keygo.local
Password: admin123
```

⚠️ **IMPORTANT:** Change these credentials after first login!

---

## 🛠️ Management Scripts / Scripts de Gestión

All scripts are located in `keygo-supabase/scripts/`:

| Script | Purpose |
|--------|---------|
| `setup-supabase.sh` | Complete initial setup |
| `migrate.sh` | Run pending migrations |
| `clean.sh` | Reset database (dev only) |
| `info.sh` | Show migration status |
| `validate.sh` | Validate migrations |
| `repair.sh` | Repair Flyway metadata |
| `dev-start.sh` | Start local database |
| `dev-stop.sh` | Stop local database |

### Examples

```bash
cd keygo-supabase

# Check migration status
./scripts/info.sh

# Run new migrations
./scripts/migrate.sh

# Validate all migrations
./scripts/validate.sh
```

---

## 🏗️ Building / Construcción

```bash
# Build entire project
./mvnw clean package

# Build specific module
./mvnw clean package -pl keygo-supabase

# Skip tests
./mvnw clean package -DskipTests
```

---

## ▶️ Running / Ejecución

### Development Mode

```bash
# Using Maven wrapper
./mvnw spring-boot:run -pl keygo-run

# With environment variables
SUPABASE_URL=jdbc:postgresql://localhost:5432/keygo \
SUPABASE_USER=postgres \
SUPABASE_PASSWORD=postgres \
./mvnw spring-boot:run -pl keygo-run
```

### Production Mode

```bash
# After building
java -jar keygo-run/target/keygo-run-1.0-SNAPSHOT.jar

# With environment variables
SUPABASE_URL=postgresql://... \
SUPABASE_USER=postgres \
SUPABASE_PASSWORD=... \
java -jar keygo-run/target/keygo-run-1.0-SNAPSHOT.jar
```

---

## 🧪 Testing / Pruebas

```bash
# Run all tests
./mvnw test

# Run Supabase module tests only
./mvnw test -pl keygo-supabase

# Run with coverage
./mvnw clean verify
```

---

## 📚 Documentation / Documentación

- **[keygo-supabase/README.md](keygo-supabase/README.md)** - Module documentation
- **[keygo-supabase/MIGRATIONS.md](keygo-supabase/MIGRATIONS.md)** - Migration guide
- **[keygo-supabase/INTEGRATION.md](keygo-supabase/INTEGRATION.md)** - Integration instructions
- **[keygo-supabase/SUMMARY.md](keygo-supabase/SUMMARY.md)** - Complete summary

---

## 🔄 Development Workflow / Flujo de Desarrollo

### Adding New Migrations

1. Create new file in `keygo-supabase/src/main/resources/db/migration/`
2. Follow naming convention: `V{version}__{description}.sql`
3. Test locally:
   ```bash
   cd keygo-supabase
   ./scripts/migrate.sh
   ./scripts/info.sh
   ```
4. Commit the migration file

### Example Migration

```sql
-- V4__add_user_preferences.sql
CREATE TABLE IF NOT EXISTS user_preferences (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    theme VARCHAR(50) DEFAULT 'light',
    language VARCHAR(10) DEFAULT 'en',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_preferences_user_id ON user_preferences(user_id);
```

---

## 🐛 Troubleshooting / Solución de Problemas

### Database Connection Issues

```bash
# Check if database is running
docker ps | grep keygo

# Check logs
docker logs keygo-supabase-db

# Restart database
cd keygo-supabase
./scripts/dev-stop.sh
./scripts/dev-start.sh
```

### Migration Issues

```bash
# Check migration status
cd keygo-supabase
./scripts/info.sh

# Repair if needed
./scripts/repair.sh

# Validate
./scripts/validate.sh
```

### Build Issues

```bash
# Clean and rebuild
./mvnw clean install -DskipTests

# Check for errors
./mvnw dependency:tree
```

---

## 🎯 Next Steps / Próximos Pasos

1. **Implement User Service** - Create business logic for user management
2. **Add Authentication** - Implement JWT-based authentication
3. **Create REST APIs** - Expose user management endpoints
4. **Add Security** - Integrate Spring Security
5. **Deploy to Production** - Connect to real Supabase instance

---

## 📞 Support / Soporte

For issues or questions:
- Check the documentation in `keygo-supabase/`
- Review the scripts in `keygo-supabase/scripts/`
- Open an issue on GitHub

---

## ✅ Integration Checklist / Lista de Verificación

- [x] Module created and compiled successfully
- [x] Added to parent POM
- [x] Integrated with keygo-run
- [x] Component scanning configured
- [x] Spring profiles configured
- [x] Database migrations created
- [x] Management scripts created
- [x] Documentation complete
- [x] Quick start script ready
- [ ] Tests passing (to be implemented)
- [ ] Production deployment (to be done)

---

**Last Updated:** 2026-03-15  
**Version:** 1.0-SNAPSHOT  
**Status:** ✅ Ready for Development

