#!/bin/bash
# =========================================================
# Clean Flyway migration history
# Limpiar historial de migraciones de Flyway
# =========================================================
set -e
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$( cd "$SCRIPT_DIR/.." && pwd )"
echo "🧹 Cleaning Flyway migration history / Limpiando historial de migraciones..."
cd "$PROJECT_DIR"
mvn flyway:clean \
    -Dsupabase.url="${SUPABASE_URL}" \
    -Dsupabase.user="${SUPABASE_USER:-postgres}" \
    -Dsupabase.password="${SUPABASE_PASSWORD}"
echo "✅ Clean completed / Limpieza completada"
echo "⚠️  WARNING: All database objects have been dropped!"
echo "⚠️  ADVERTENCIA: ¡Todos los objetos de la base de datos han sido eliminados!"
