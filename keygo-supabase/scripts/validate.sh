#!/bin/bash
# =========================================================
# Validate Flyway migrations
# Validar migraciones de Flyway
# =========================================================
set -e
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$( cd "$SCRIPT_DIR/.." && pwd )"
echo "✅ Validating Flyway migrations / Validando migraciones de Flyway..."
cd "$PROJECT_DIR"
mvn flyway:validate \
    -Dsupabase.url="${SUPABASE_URL}" \
    -Dsupabase.user="${SUPABASE_USER:-postgres}" \
    -Dsupabase.password="${SUPABASE_PASSWORD}"
echo "✅ Validation successful / Validación exitosa"
