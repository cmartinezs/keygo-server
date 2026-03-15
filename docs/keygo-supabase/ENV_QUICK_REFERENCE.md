# Environment Management Strategy - Summary
# Estrategia de Gestión de Ambientes - Resumen

## 🎯 Quick Reference / Referencia Rápida

### Cambiar de Ambiente / Switch Environment

```bash
cd keygo-supabase

# Cambiar a ambiente local
./scripts/switch-env.sh local

# Cambiar a desarrollo/staging
./scripts/switch-env.sh desa

# Cambiar a producción
./scripts/switch-env.sh prod

# Listar ambientes disponibles
./scripts/switch-env.sh list
```

### Ejecutar Migraciones / Run Migrations

```bash
cd keygo-supabase

# El script cargará automáticamente el .env activo
./scripts/migrate.sh
```

### Ver Estado de Ambiente / Check Environment Status

```bash
cd keygo-supabase

# Ver configuración actual
cat .env | head -n 10

# Ver ambiente activo
grep "^KEYGO_ENV=" .env
```

---

## 📁 Archivos de Ambiente / Environment Files

| Archivo | Propósito | Git | Editar |
|---------|-----------|-----|--------|
| `.env` | Ambiente activo actualmente | ❌ | No - usar switch-env.sh |
| `.env-local` | Configuración para desarrollo local | ❌ | ✅ Sí |
| `.env-desa` | Configuración para desa/staging | ❌ | ✅ Sí |
| `.env-prod` | Configuración para producción | ❌ | ✅ Sí |
| `.env.example` | Template con todas las variables | ✅ | ✅ Sí |

---

## 🔧 Variables de Entorno Principales / Main Environment Variables

### Base de Datos / Database
```bash
SUPABASE_URL=jdbc:postgresql://host:port/database
SUPABASE_USER=postgres
SUPABASE_PASSWORD=your-password
SUPABASE_DB_HOST=localhost
SUPABASE_DB_PORT=5432
SUPABASE_DB_NAME=keygo
```

### Proyecto Supabase / Supabase Project
```bash
SUPABASE_PROJECT_ID=xxxxx
SUPABASE_API_URL=https://xxxxx.supabase.co
SUPABASE_ANON_KEY=your-anon-key
SUPABASE_SERVICE_KEY=your-service-key
SUPABASE_JWT_SECRET=your-jwt-secret
```

### Aplicación / Application
```bash
KEYGO_ENV=local|desa|prod
SPRING_PROFILES_ACTIVE=supabase,local
SERVER_PORT=8080
LOG_LEVEL=INFO
```

---

## 🚀 Flujo de Trabajo Típico / Typical Workflow

### 1. Configuración Inicial / Initial Setup

```bash
cd keygo-supabase

# Crear archivos de ambiente desde la plantilla
cp .env.example .env-local
cp .env.example .env-desa
cp .env.example .env-prod

# Editar cada archivo con valores apropiados
vim .env-local  # Configurar para Docker local
vim .env-desa   # Configurar con credenciales de Supabase desa
vim .env-prod   # Configurar con credenciales de Supabase prod

# Activar ambiente local
./scripts/switch-env.sh local
```

### 2. Desarrollo Diario / Daily Development

```bash
# Asegurar que estás en ambiente local
cd keygo-supabase
./scripts/switch-env.sh local

# Iniciar base de datos local
./scripts/dev-start.sh

# Ejecutar migraciones (carga .env automáticamente)
./scripts/migrate.sh

# Abrir IDE (IntelliJ/VS Code/Eclipse)
# El IDE cargará automáticamente las variables desde .env

# Desarrollar y probar...

# Al finalizar, detener base de datos
./scripts/dev-stop.sh
```

### 3. Pruebas en Desarrollo / Testing in Development

```bash
# Cambiar a ambiente de desarrollo
cd keygo-supabase
./scripts/switch-env.sh desa

# Verificar configuración
./scripts/info.sh

# Ejecutar migraciones
./scripts/migrate.sh

# Reiniciar aplicación en IDE
# (Las nuevas variables se cargarán automáticamente)
```

### 4. Despliegue a Producción / Production Deployment

```bash
# ⚠️ PRECAUCIÓN - PRODUCCIÓN

# Cambiar a ambiente de producción
cd keygo-supabase
./scripts/switch-env.sh prod

# IMPORTANTE: Respaldar base de datos primero
# (Usar dashboard de Supabase o pg_dump)

# Verificar estado de migraciones
./scripts/info.sh

# Ejecutar migraciones
./scripts/migrate.sh

# Verificar que se aplicaron correctamente
./scripts/info.sh

# Volver a ambiente local
./scripts/switch-env.sh local
```

---

## 🛡️ Mejores Prácticas de Seguridad / Security Best Practices

### ✅ HACER / DO

1. Usar archivos `.env-*` separados para cada ambiente
2. Nunca commitear archivos `.env` a git
3. Usar contraseñas fuertes (32+ caracteres) en producción
4. Rotar credenciales cada 90 días
5. Diferentes credenciales por ambiente
6. Respaldar antes de cambios en producción
7. Revisar git status antes de commitear

### ❌ NO HACER / DON'T

1. Nunca commitear `.env` files
2. Nunca compartir credenciales por email/Slack
3. Nunca usar BD de producción para desarrollo
4. Nunca exponer service keys en código cliente
5. Nunca saltar backups

---

## 🔍 Verificación / Verification

### Comprobar que .env NO está en git

```bash
# Desde la raíz del proyecto
git status | grep ".env"

# NO debería mostrar ningún archivo .env
# Solo .env.example debe estar tracked
```

### Verificar Variables Cargadas

```bash
# Ver ambiente actual
cat keygo-supabase/.env | grep "^KEYGO_ENV="

# Ver todas las variables (sin passwords)
cat keygo-supabase/.env | grep -v "PASSWORD" | grep -v "SECRET" | grep -v "KEY"

# Desde la aplicación corriendo
curl http://localhost:8080/keygo-server/actuator/env | jq
```

---

## 📚 Documentación Completa / Complete Documentation

- **[ENVIRONMENT_STRATEGY.md](ENVIRONMENT_STRATEGY.md)** - Guía completa de estrategia
- **[INTELLIJ_SETUP.md](INTELLIJ_SETUP.md)** - Configuración para IntelliJ IDEA
- **[README.md](README.md)** - Documentación general del módulo
- **[MIGRATIONS.md](MIGRATIONS.md)** - Guía de migraciones

---

## 🆘 Solución de Problemas / Troubleshooting

### Problema: Variables no se cargan en IDE

**Solución:**
1. Verificar que `.env` existe: `ls -la keygo-supabase/.env`
2. Instalar plugin EnvFile (IntelliJ) o DotENV (VS Code)
3. Reiniciar IDE después de cambiar ambiente
4. Verificar permisos: `chmod 644 keygo-supabase/.env`

### Problema: Ambiente incorrecto activo

**Solución:**
```bash
cd keygo-supabase
./scripts/switch-env.sh local  # o desa, prod
```

### Problema: Migraciones fallan

**Solución:**
```bash
# Verificar variables de ambiente
source keygo-supabase/.env
echo $SUPABASE_URL

# Ver estado de migraciones
cd keygo-supabase
./scripts/info.sh

# Reparar si es necesario
./scripts/repair.sh
```

---

## ✅ Checklist de Seguridad / Security Checklist

Antes de commitear / Before committing:
- [ ] Ejecutar `git status` y verificar que NO hay archivos `.env`
- [ ] Solo `.env.example` debe estar en git
- [ ] No hay credenciales hardcodeadas en código
- [ ] Todas las nuevas variables están en `.env.example`
- [ ] `.gitignore` incluye `.env*` (excepto `.env.example`)

Antes de desplegar / Before deploying:
- [ ] Respaldar base de datos
- [ ] Probar primero en desa
- [ ] Revisar scripts de migración
- [ ] Actualizar credenciales si es necesario
- [ ] Verificar configuración de `.env-prod`
- [ ] Tener plan de rollback

---

**Última Actualización / Last Updated:** 2026-03-15  
**Versión / Version:** 1.0  
**Autor / Author:** cmartinezs

