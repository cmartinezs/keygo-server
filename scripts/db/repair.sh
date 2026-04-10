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
cd "$PROJECT_ROOT"
"$PROJECT_ROOT/mvnw" flyway:repair \
    -pl keygo-supabase \
    --no-transfer-progress
echo "✅ Reparación completada"
