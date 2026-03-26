#!/bin/bash
# =========================================================
# db/repair.sh — Reparar metadatos de Flyway
# =========================================================
set -e
# shellcheck source=_load-env.sh
source "$(dirname "${BASH_SOURCE[0]}")/_load-env.sh"

load_env || true
verify_db_vars || exit 1

echo "🔧 Reparando metadatos de Flyway..."
cd "$SUPABASE_DIR"
mvn flyway:repair \
    -Dsupabase.url="${SUPABASE_URL}" \
    -Dsupabase.user="${SUPABASE_USER:-postgres}" \
    -Dsupabase.password="${SUPABASE_PASSWORD}"
echo "✅ Reparación completada"

