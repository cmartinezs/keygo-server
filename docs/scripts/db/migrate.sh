#!/bin/bash
# =========================================================
# db/migrate.sh — Ejecutar migraciones Flyway
# =========================================================
set -e
# shellcheck source=_load-env.sh
source "$(dirname "${BASH_SOURCE[0]}")/_load-env.sh"

load_env || true
verify_db_vars || exit 1

echo "📦 Ejecutando migraciones Flyway..."
echo "   Ambiente: ${KEYGO_ENV:-desconocido}"
echo "   DB:       ${SUPABASE_DB_HOST:-localhost}:${SUPABASE_DB_PORT:-5432}"
echo ""

cd "$SUPABASE_DIR"
mvn clean compile flyway:migrate \
    -Dsupabase.url="${SUPABASE_URL}" \
    -Dsupabase.user="${SUPABASE_USER}" \
    -Dsupabase.password="${SUPABASE_PASSWORD}"

echo ""
echo "✅ Migraciones completadas"

