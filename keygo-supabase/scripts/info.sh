#!/bin/bash
# =========================================================
# Get Flyway migration info
# Obtener información de migraciones de Flyway
# =========================================================
set -e
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$( cd "$SCRIPT_DIR/.." && pwd )"
echo "ℹ️  Flyway migration info / Información de migraciones de Flyway"
echo "================================================================"
cd "$PROJECT_DIR"
mvn flyway:info \
    -Dsupabase.url="${SUPABASE_URL}" \
    -Dsupabase.user="${SUPABASE_USER:-postgres}" \
    -Dsupabase.password="${SUPABASE_PASSWORD}"
