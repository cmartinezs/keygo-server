# KeyGo Supabase Module

Módulo de integración de KeyGo con Supabase, incluyendo migraciones de base de datos y scripts de gestión.

## 📋 Descripción / Description

Este módulo proporciona:
- **Migraciones de base de datos** con Flyway
- **Scripts de gestión** para Supabase
- **Esquema inicial** de autenticación y autorización
- **Datos semilla** con usuario administrador por defecto

This module provides:
- **Database migrations** with Flyway
- **Management scripts** for Supabase
- **Initial schema** for authentication and authorization
- **Seed data** with default admin user

## 🚀 Inicio Rápido / Quick Start

### Prerrequisitos / Prerequisites

1. Cuenta de Supabase / Supabase account
2. Proyecto creado en Supabase / Project created in Supabase
3. Credenciales de conexión / Connection credentials

### Configuración / Configuration

Configura las variables de entorno / Set environment variables:

```bash
export SUPABASE_URL="postgresql://postgres:[PASSWORD]@db.[PROJECT].supabase.co:5432/postgres"
export SUPABASE_USER="postgres"
export SUPABASE_PASSWORD="your-database-password"
```

### Ejecutar Migraciones / Run Migrations

```bash
# Opción 1: Usar script de configuración / Option 1: Use setup script
cd keygo-supabase
./scripts/setup-supabase.sh

# Opción 2: Usar Maven directamente / Option 2: Use Maven directly
mvn flyway:migrate \
    -Dsupabase.url="$SUPABASE_URL" \
    -Dsupabase.user="$SUPABASE_USER" \
    -Dsupabase.password="$SUPABASE_PASSWORD"
```

## 📂 Estructura / Structure

```
keygo-supabase/
├── pom.xml                          # Configuración Maven
├── README.md                        # Este archivo
├── scripts/                         # Scripts de gestión
│   ├── setup-supabase.sh           # Configuración inicial
│   ├── migrate.sh                  # Ejecutar migraciones
│   ├── clean.sh                    # Limpiar base de datos
│   └── info.sh                     # Información de migraciones
└── src/main/resources/
    └── db/migration/               # Migraciones Flyway
        ├── V1__initial_schema.sql  # Esquema inicial
        └── V2__seed_data.sql       # Datos iniciales
```

## 📊 Esquema de Base de Datos / Database Schema

### Tablas Principales / Main Tables

1. **users** - Usuarios del sistema / System users
2. **roles** - Roles de usuario / User roles
3. **permissions** - Permisos del sistema / System permissions
4. **user_roles** - Asignación de roles / Role assignments
5. **role_permissions** - Permisos por rol / Permissions per role
6. **sessions** - Sesiones activas / Active sessions
7. **audit_logs** - Registro de auditoría / Audit trail

### Características / Features

- ✅ UUIDs como claves primarias / UUIDs as primary keys
- ✅ Timestamps automáticos / Automatic timestamps
- ✅ Índices optimizados / Optimized indexes
- ✅ Políticas RLS (Row Level Security)
- ✅ Triggers para updated_at
- ✅ Validaciones a nivel de base de datos / Database-level validations

## 🔐 Usuario Administrador por Defecto / Default Admin User

**⚠️ IMPORTANTE / IMPORTANT**: Cambia estas credenciales después del primer login / Change these credentials after first login!

```
Username: admin
Email: admin@keygo.local
Password: admin123
```

## 📜 Scripts Disponibles / Available Scripts

### setup-supabase.sh
Configuración inicial completa del módulo / Complete initial module setup

```bash
./scripts/setup-supabase.sh
```

### migrate.sh
Ejecuta las migraciones pendientes / Run pending migrations

```bash
./scripts/migrate.sh
```

### clean.sh
⚠️ **PELIGROSO** - Elimina todos los objetos de la base de datos / **DANGEROUS** - Drops all database objects

```bash
./scripts/clean.sh
```

### info.sh
Muestra el estado de las migraciones / Show migration status

```bash
./scripts/info.sh
```

## 🔧 Comandos Maven / Maven Commands

```bash
# Ver información de migraciones / View migration info
mvn flyway:info

# Ejecutar migraciones / Run migrations
mvn flyway:migrate

# Validar migraciones / Validate migrations
mvn flyway:validate

# Reparar metadatos / Repair metadata
mvn flyway:repair

# Limpiar base de datos (⚠️ cuidado!) / Clean database (⚠️ careful!)
mvn flyway:clean
```

## 📝 Crear Nueva Migración / Create New Migration

1. Crea un nuevo archivo en `src/main/resources/db/migration/`
2. Usa el formato: `V{version}__{description}.sql`
   - Ejemplo: `V3__add_oauth_support.sql`
3. Escribe tu SQL
4. Ejecuta `./scripts/migrate.sh`

Example:
```sql
-- V3__add_oauth_support.sql
CREATE TABLE oauth_providers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL,
    client_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

## 🧪 Testing

```bash
# Ejecutar tests del módulo / Run module tests
mvn test
```

## 📚 Recursos / Resources

- [Supabase Documentation](https://supabase.com/docs)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

## 🤝 Contribuir / Contributing

Ver [CONTRIBUTING.md](../CONTRIBUTING.md) en la raíz del proyecto.

## 📄 Licencia / License

AGPL-3.0 - Ver [LICENSE](../LICENSE) para más detalles.

## ⚠️ Notas de Seguridad / Security Notes

1. **Nunca** commitees credenciales en el código / **Never** commit credentials in code
2. Usa variables de entorno para configuración sensible / Use environment variables for sensitive config
3. Cambia el password del admin inmediatamente / Change admin password immediately
4. Habilita 2FA en producción / Enable 2FA in production
5. Revisa los logs de auditoría regularmente / Review audit logs regularly

## 📞 Soporte / Support

Para problemas o preguntas / For issues or questions:
- Abre un issue en GitHub / Open an issue on GitHub
- Consulta la documentación / Check the documentation

