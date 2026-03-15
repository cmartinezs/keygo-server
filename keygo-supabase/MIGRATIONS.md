# KeyGo Supabase - Database Migrations Guide
# Guía de Migraciones de Base de Datos

## 📋 Overview / Resumen

This document describes the database migration strategy for KeyGo Supabase module using Flyway.

Este documento describe la estrategia de migraciones de base de datos para el módulo KeyGo Supabase usando Flyway.

## 🗂️ Migration Files / Archivos de Migración

### Current Migrations / Migraciones Actuales

| Version | File | Description |
|---------|------|-------------|
| V1 | `V1__initial_schema.sql` | Initial database schema with users, roles, permissions, sessions, and audit logs |
| V2 | `V2__seed_data.sql` | Initial seed data with default roles, permissions, and admin user |
| V3 | `V3__add_oauth_support.sql` | OAuth provider integration support |

## 📝 Migration Naming Convention / Convención de Nombres

Flyway uses the following naming pattern:

```
V{version}__{description}.sql
```

**Examples / Ejemplos:**
- `V1__initial_schema.sql`
- `V2__seed_data.sql`
- `V3__add_oauth_support.sql`
- `V4__add_2fa_support.sql`

**Rules / Reglas:**
1. Version must be numeric and incremental
2. Double underscore separates version from description
3. Description uses underscores for spaces
4. File extension must be `.sql`

## 🔄 Migration Workflow / Flujo de Trabajo

### 1. Create New Migration / Crear Nueva Migración

```bash
# Create file in correct directory
cd keygo-supabase/src/main/resources/db/migration

# Create new migration file
touch V4__your_description.sql

# Edit the file with your SQL changes
```

### 2. Validate Migration / Validar Migración

```bash
./scripts/validate.sh
```

### 3. Test Locally / Probar Localmente

```bash
# Start local database
./scripts/dev-start.sh

# Set environment variables
export SUPABASE_URL=jdbc:postgresql://localhost:5432/keygo
export SUPABASE_USER=postgres
export SUPABASE_PASSWORD=postgres

# Run migration
./scripts/migrate.sh

# Check migration status
./scripts/info.sh
```

### 4. Apply to Production / Aplicar a Producción

```bash
# Set production environment variables
export SUPABASE_URL=your-production-url
export SUPABASE_USER=your-production-user
export SUPABASE_PASSWORD=your-production-password

# Run migration
./scripts/migrate.sh
```

## 📊 Migration Best Practices / Mejores Prácticas

### ✅ DO / HACER

1. **Always backup before migration** / Siempre respaldar antes de migrar
2. **Test in development first** / Probar en desarrollo primero
3. **Use transactions** / Usar transacciones
4. **Add comments** / Agregar comentarios
5. **Use IF NOT EXISTS** / Usar IF NOT EXISTS
6. **Create indexes** / Crear índices
7. **Add constraints** / Agregar restricciones
8. **Document changes** / Documentar cambios

### ❌ DON'T / NO HACER

1. **Don't modify existing migrations** / No modificar migraciones existentes
2. **Don't use DROP TABLE without backup** / No usar DROP TABLE sin respaldo
3. **Don't skip versions** / No saltar versiones
4. **Don't hardcode sensitive data** / No hardcodear datos sensibles

## 🎯 Migration Templates / Plantillas de Migración

### Add New Table / Agregar Nueva Tabla

```sql
-- V{X}__add_{table_name}.sql
CREATE TABLE IF NOT EXISTS {table_name} (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_{table_name}_name ON {table_name}(name);

CREATE TRIGGER {table_name}_updated_at
    BEFORE UPDATE ON {table_name}
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE {table_name} IS 'Description here';
```

### Add Column / Agregar Columna

```sql
-- V{X}__add_{column_name}_to_{table_name}.sql
ALTER TABLE {table_name}
ADD COLUMN IF NOT EXISTS {column_name} VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_{table_name}_{column_name} 
ON {table_name}({column_name});
```

### Modify Column / Modificar Columna

```sql
-- V{X}__modify_{column_name}_in_{table_name}.sql
ALTER TABLE {table_name}
ALTER COLUMN {column_name} TYPE TEXT;
```

### Add Foreign Key / Agregar Clave Foránea

```sql
-- V{X}__add_{fk_name}_to_{table_name}.sql
ALTER TABLE {table_name}
ADD CONSTRAINT fk_{table_name}_{column_name}
FOREIGN KEY ({column_name}) 
REFERENCES {reference_table}(id) 
ON DELETE CASCADE;
```

## 🔧 Troubleshooting / Solución de Problemas

### Migration Failed / Migración Falló

```bash
# Check migration status
./scripts/info.sh

# Repair if needed
./scripts/repair.sh

# Try migration again
./scripts/migrate.sh
```

### Reset Database (Development Only) / Resetear Base de Datos (Solo Desarrollo)

```bash
# ⚠️ WARNING: This will delete all data!
./scripts/clean.sh
./scripts/migrate.sh
```

### Check Migration Checksum / Verificar Checksum de Migración

```bash
mvn flyway:validate \
    -Dsupabase.url="${SUPABASE_URL}" \
    -Dsupabase.user="${SUPABASE_USER}" \
    -Dsupabase.password="${SUPABASE_PASSWORD}"
```

## 📚 Additional Resources / Recursos Adicionales

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Supabase Documentation](https://supabase.com/docs)

## 🔐 Security Notes / Notas de Seguridad

1. Never commit credentials to version control
2. Always use environment variables for sensitive data
3. Test migrations in isolated environment first
4. Keep migration scripts in version control
5. Document all schema changes

---

**Last Updated:** 2026-03-15
**Version:** 1.0

