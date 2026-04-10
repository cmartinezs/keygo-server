#!/bin/bash
# =========================================================
# db/clean.sh — ⚠️  Limpiar schema completo (DESTRUCTIVO)
# =========================================================
set -e
# shellcheck source=_load-env.sh
source "$(dirname "${BASH_SOURCE[0]}")/_load-env.sh"

load_env || true
verify_db_vars || exit 1

echo ""
echo "⚠️  ¡ADVERTENCIA! Esta operación elimina TODOS los objetos de la BD."
echo "   Ambiente: ${KEYGO_ENV:-desconocido}"
echo "   DB:       ${SUPABASE_DB_HOST:-localhost}:${SUPABASE_DB_PORT:-5432}"
echo ""
read -r -p "   Escribe 'CONFIRMAR' para continuar: " CONFIRM
if [ "$CONFIRM" != "CONFIRMAR" ]; then
    echo "❌ Operación cancelada"
    exit 1
fi

echo ""
echo "🧹 Limpiando schema..."
cd "$PROJECT_ROOT"
"$PROJECT_ROOT/mvnw" flyway:clean \
    -pl keygo-supabase \
    --no-transfer-progress
echo "✅ Limpieza completada — todos los objetos eliminados"
