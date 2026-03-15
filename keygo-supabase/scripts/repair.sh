#!/bin/bash
# =========================================================
# Repair Flyway metadata
# Reparar metadatos de Flyway
# =========================================================
set -e
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$( cd "$SCRIPT_DIR/.." && pwd )"
echo "🔧 Repairing Flyway metadata / Reparando metadatos de Flyway..."
cd "$PROJECT_DIR"
mvn flyway:repair \
    -Dsupabase.url="${SUPABASE_URL}" \
    -Dsupabase.user="${SUPABASE_USER:-postgres}" \
    -Dsupabase.password="${SUPABASE_PASSWORD}"
echo "✅ Repair completed / Reparación completada"
