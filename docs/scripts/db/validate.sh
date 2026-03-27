#!/bin/bash
# =========================================================
# db/validate.sh — Validar migraciones Flyway
# =========================================================
set -e
# shellcheck source=_load-env.sh
source "$(dirname "${BASH_SOURCE[0]}")/_load-env.sh"

load_env || true
verify_db_vars || exit 1

echo "🔍 Validando migraciones Flyway..."
cd "$SUPABASE_DIR"
mvn flyway:validate \
    -Dsupabase.url="${SUPABASE_URL}" \
    -Dsupabase.user="${SUPABASE_USER:-postgres}" \
    -Dsupabase.password="${SUPABASE_PASSWORD}"
echo "✅ Validación exitosa"

