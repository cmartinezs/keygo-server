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
cd "$SUPABASE_DIR"
mvn flyway:clean \
    -Dsupabase.url="${SUPABASE_URL}" \
    -Dsupabase.user="${SUPABASE_USER:-postgres}" \
    -Dsupabase.password="${SUPABASE_PASSWORD}"
echo "✅ Limpieza completada — todos los objetos eliminados"

